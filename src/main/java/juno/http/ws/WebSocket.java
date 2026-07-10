package juno.http.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import juno.http.Headers;
import juno.util.Base64;

/**
 * Cliente WebSocket (RFC 6455) implementado directamente sobre {@link Socket}/
 * {@link SSLSocket}, sin dependencias externas — mismo criterio que
 * {@link juno.http.URLConnectionTransport} para HTTP. Expone el mismo modelo de
 * eventos que el WebSocket nativo del navegador/React Native.
 *
 * <p>Solo lado cliente, pero cubre lo que un cliente real necesita: verificación
 * de hostname TLS, keepalive con detección de conexión perdida, fragmentación de
 * mensajes grandes al enviar, validación estricta de UTF-8 en frames de texto
 * (falla la conexión con 1007 si el servidor manda UTF-8 inválido, como exige el
 * RFC), negociación de subprotocolo, {@code SSLSocketFactory} personalizado
 * (mTLS, trust store propio) y proxy HTTP vía {@code CONNECT}.
 *
 * <p>{@link #connect()} es asíncrono (no bloquea, seguro de llamar desde el hilo
 * principal de Android); usa {@link #connectBlocking()} si necesitas esperar el
 * resultado sincrónicamente en tu propio hilo de fondo.
 *
 * <pre>{@code
 * WebSocket ws = new WebSocket("wss://example.com/chat?token=" + token,
 *     new WebSocketAdapter() {
 *       @Override public void onOpen(WebSocket ws, ServerHandshake handshake) {
 *         System.out.println("connected: " + handshake.getHttpStatus() + " " + handshake.getHttpStatusMessage());
 *       }
 *       @Override public void onMessage(WebSocket ws, String text) {
 *         System.out.println("message: " + text);
 *       }
 *       @Override public void onClosed(WebSocket ws, int code, String reason) {
 *         // reconectar aquí, igual que el onclose del hook de React Native —
 *         // se dispara siempre, sea cierre limpio o caída de red (code == ABNORMAL_CLOSURE)
 *       }
 *     });
 * ws.connect();
 * ...
 * ws.close(WebSocket.NORMAL_CLOSURE, null);
 * }</pre>
 *
 * No es thread-safe para llamadas concurrentes a {@link #connect()}, pero
 * {@link #send(String)}/{@link #send(byte[])}/{@link #close}/{@link #cancel} sí
 * pueden llamarse desde cualquier hilo mientras el socket está abierto.
 */
public class WebSocket {

    /** Cierre normal, iniciado por cualquiera de las dos partes. */
    public static final int NORMAL_CLOSURE = 1000;

    /** Cierre anómalo: el socket se cortó sin un frame de cierre previo. */
    public static final int ABNORMAL_CLOSURE = 1006;

    /** El payload de un frame de texto no era UTF-8 válido (RFC 6455 §8.1). */
    public static final int INVALID_PAYLOAD_DATA = 1007;

    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private static final int OP_CONTINUATION = 0x0;
    private static final int OP_TEXT = 0x1;
    private static final int OP_BINARY = 0x2;
    private static final int OP_CLOSE = 0x8;
    private static final int OP_PING = 0x9;
    private static final int OP_PONG = 0xA;

    /** Límite defensivo contra frames con una longitud declarada absurda (servidor malicioso/bugueado). */
    private static final long MAX_PAYLOAD_LENGTH = 16L * 1024 * 1024;

    private URI uri;
    private final Headers requestHeaders;
    private final WebSocketListener listener;
    private final SecureRandom random = new SecureRandom();
    private final Object writeLock = new Object();

    private int connectTimeoutMs = 10000;
    private int maxRedirects = 5;
    private String[] subprotocols;
    private String acceptedProtocol;
    private SSLSocketFactory sslSocketFactory;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private long pingIntervalMs;
    private long pingTimeoutMs = 10000;
    private int maxOutgoingFrameSize;

    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private Thread pingThread;

    private volatile boolean open;
    private volatile boolean pongPending;
    private boolean closeSent;

    // Estado de reensamblado de mensajes fragmentados (solo tocado por el hilo lector).
    private int fragmentedOpcode = -1;
    private ByteArrayOutputStream fragmentBuffer;

    public WebSocket(String url, WebSocketListener listener) {
        this(url, null, listener);
    }

    /**
     * @param url ws://host/path o wss://host/path, con query params ya incluidos.
     * @param requestHeaders encabezados adicionales para el handshake (auth, etc.), o {@code null}.
     * @param listener receptor de los eventos del socket.
     */
    public WebSocket(String url, Headers requestHeaders, WebSocketListener listener) {
        if (url == null) throw new NullPointerException("url == null");
        if (listener == null) throw new NullPointerException("listener == null");

        try {
            this.uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed WebSocket URL: " + url, e);
        }

        final String scheme = uri.getScheme();
        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Unsupported scheme (expected ws:// or wss://): " + scheme);
        }

        this.requestHeaders = requestHeaders != null ? requestHeaders : new Headers();
        this.listener = listener;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public WebSocket setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    /** Máximo de redirecciones 3xx a seguir durante el handshake antes de fallar. Default: 5. */
    public WebSocket setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
        return this;
    }

    /** Lista de subprotocolos a ofrecer (header {@code Sec-WebSocket-Protocol}), en orden de preferencia. */
    public WebSocket setSubprotocols(String... subprotocols) {
        this.subprotocols = subprotocols;
        return this;
    }

    /** Subprotocolo aceptado por el servidor tras {@link #connect()}, o {@code null} si no se negoció ninguno. */
    public String getAcceptedProtocol() {
        return acceptedProtocol;
    }

    /** Factory TLS personalizado (mTLS, trust store propio, pinning, etc.). Por default usa {@link SSLSocketFactory#getDefault()}. */
    public WebSocket setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    /** Conecta a través de un proxy HTTP (túnel {@code CONNECT}), sin autenticación. */
    public WebSocket setProxy(String host, int port) {
        return setProxy(host, port, null, null);
    }

    /** Conecta a través de un proxy HTTP (túnel {@code CONNECT}) con autenticación Basic. */
    public WebSocket setProxy(String host, int port, String username, String password) {
        this.proxyHost = host;
        this.proxyPort = port;
        this.proxyUsername = username;
        this.proxyPassword = password;
        return this;
    }

    /**
     * Activa el keepalive: cada {@code intervalMs} manda un ping; si no llega el pong
     * correspondiente dentro de {@code timeoutMs}, se trata como conexión perdida y
     * dispara {@link WebSocketListener#onFailure}. Desactivado por default (intervalMs &lt;= 0).
     */
    public WebSocket setPingInterval(long intervalMs, long timeoutMs) {
        this.pingIntervalMs = intervalMs;
        this.pingTimeoutMs = timeoutMs;
        return this;
    }

    /**
     * Si un mensaje saliente supera este tamaño, se fragmenta en varios frames de
     * como máximo {@code maxOutgoingFrameSize} bytes. {@code 0} (default) desactiva
     * la fragmentación: cada mensaje se manda en un único frame.
     */
    public WebSocket setMaxOutgoingFrameSize(int maxOutgoingFrameSize) {
        this.maxOutgoingFrameSize = maxOutgoingFrameSize;
        return this;
    }

    public boolean isOpen() {
        return open;
    }

    // ------------------------------------------------------------------
    // Conexión
    // ------------------------------------------------------------------

    /**
     * Conecta en segundo plano — no bloquea el hilo que la llama (imprescindible
     * en Android, donde abrir sockets en el hilo principal está prohibido). El
     * resultado llega por los callbacks de {@link WebSocketListener}:
     * {@code onOpen} si el handshake tuvo éxito, o {@code onFailure} seguido de
     * {@code onClosed(ABNORMAL_CLOSURE, ...)} si falló.
     *
     * @see #connectBlocking() si necesitas esperar el resultado sincrónicamente.
     */
    public synchronized void connect() {
        if (socket != null) throw new IllegalStateException("connect() already called");

        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                connectBlocking();
            }
        }, "WebSocket-Connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * Igual que {@link #connect()} pero bloqueante: espera a que el handshake
     * termine (o falle) antes de retornar, en el mismo hilo que la llama. No
     * lanza excepciones — el motivo de una falla, si la hay, llega por
     * {@code onFailure}/{@code onClosed} igual que con {@link #connect()}.
     *
     * @return {@code true} si el handshake tuvo éxito ({@code onOpen} ya se disparó),
     *         {@code false} si falló.
     */
    public synchronized boolean connectBlocking() {
        if (socket != null) throw new IllegalStateException("connect() already called");

        URI target = uri;
        for (int attempt = 0; ; attempt++) {
            try {
                doConnect(target);
                return true;
            } catch (WebSocketRedirectException e) {
                if (attempt >= maxRedirects) {
                    reportConnectFailure(new IOException("Too many redirects (" + maxRedirects + ")"));
                    return false;
                }
                try {
                    target = resolveRedirect(target, e.location);
                } catch (IOException redirectError) {
                    reportConnectFailure(redirectError);
                    return false;
                }
            } catch (IOException e) {
                reportConnectFailure(e);
                return false;
            }
        }
    }

    /** Reporta por los callbacks una falla del handshake inicial (nunca llegó a abrir). */
    private void reportConnectFailure(Exception e) {
        closeSocketQuietly();
        listener.onFailure(this, e);
        listener.onClosed(this, ABNORMAL_CLOSURE, e.getMessage());
    }

    private void doConnect(URI target) throws IOException {
        this.uri = target;

        final boolean secure = "wss".equalsIgnoreCase(target.getScheme());
        final String host = target.getHost();
        final int port = target.getPort() != -1 ? target.getPort() : (secure ? 443 : 80);

        final Socket rawSocket = new Socket();
        Socket connectedSocket = rawSocket;
        try {
            final InetSocketAddress connectAddress = proxyHost != null
                    ? new InetSocketAddress(proxyHost, proxyPort)
                    : new InetSocketAddress(host, port);
            rawSocket.connect(connectAddress, connectTimeoutMs);
            rawSocket.setTcpNoDelay(true);

            if (proxyHost != null) {
                tunnelThroughProxy(rawSocket, host, port);
            }

            if (secure) {
                final SSLSocketFactory factory = sslSocketFactory != null
                        ? sslSocketFactory
                        : (SSLSocketFactory) SSLSocketFactory.getDefault();
                final SSLSocket sslSocket = (SSLSocket) factory.createSocket(rawSocket, host, port, true);
                connectedSocket = sslSocket;

                // Un SSLSocket "crudo" NO valida por sí solo que el certificado
                // corresponda al host al que nos conectamos — hay que pedirlo explícitamente.
                SSLParameters params = sslSocket.getSSLParameters();
                if (params == null) params = new SSLParameters();
                params.setEndpointIdentificationAlgorithm("HTTPS");
                sslSocket.setSSLParameters(params);

                sslSocket.startHandshake();
            }

            socket = connectedSocket;
            out = socket.getOutputStream();
            in = socket.getInputStream();

            final String key = generateWebSocketKey();
            writeHandshakeRequest(host, port, key);
            final ServerHandshake handshake = readHandshakeResponse(key);
            acceptedProtocol = handshake.getValue("Sec-WebSocket-Protocol");

            open = true;
            startReaderThread();
            startPingThreadIfNeeded();

            listener.onOpen(this, handshake);

        } catch (IOException e) {
            try {
                connectedSocket.close();
            } catch (IOException ignored) {
            }
            socket = null;
            out = null;
            in = null;
            throw e;
        }
    }

    private void tunnelThroughProxy(Socket rawSocket, String host, int port) throws IOException {
        final OutputStream proxyOut = rawSocket.getOutputStream();
        final InputStream proxyIn = rawSocket.getInputStream();

        final StringBuilder sb = new StringBuilder();
        sb.append("CONNECT ").append(host).append(':').append(port).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(host).append(':').append(port).append("\r\n");
        if (proxyUsername != null) {
            final String credentials = proxyUsername + ":" + (proxyPassword != null ? proxyPassword : "");
            sb.append("Proxy-Authorization: Basic ")
                    .append(Base64.getEncoder().encodeToString(credentials.getBytes(UTF8)))
                    .append("\r\n");
        }
        sb.append("\r\n");

        proxyOut.write(sb.toString().getBytes(UTF8));
        proxyOut.flush();

        final String statusLine = readLine(proxyIn);
        if (statusLine == null) {
            throw new IOException("Proxy closed the connection during CONNECT");
        }

        final String[] parts = statusLine.split(" ", 3);
        final int code;
        try {
            code = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
        } catch (NumberFormatException e) {
            throw new IOException("Malformed proxy CONNECT response: " + statusLine);
        }

        String line;
        while ((line = readLine(proxyIn)) != null && !line.isEmpty()) {
            // drena los headers de la respuesta del proxy, no nos interesan
        }

        if (code != 200) {
            throw new IOException("Proxy CONNECT to " + host + ":" + port + " failed: " + statusLine);
        }
    }

    private void startReaderThread() {
        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                readLoop();
            }
        }, "WebSocket-Reader");
        reader.setDaemon(true);
        reader.start();
    }

    private void startPingThreadIfNeeded() {
        if (pingIntervalMs <= 0) return;
        pingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                pingLoop();
            }
        }, "WebSocket-Ping");
        pingThread.setDaemon(true);
        pingThread.start();
    }

    private String generateWebSocketKey() {
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }

    private static String computeAcceptKey(String key) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest((key + GUID).getBytes(UTF8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e); // SHA-1 siempre está disponible en la JVM.
        }
    }

    private void writeHandshakeRequest(String host, int port, String key) throws IOException {
        String path = uri.getRawPath();
        if (path == null || path.isEmpty()) path = "/";
        if (uri.getRawQuery() != null) path = path + "?" + uri.getRawQuery();

        final boolean secure = "wss".equalsIgnoreCase(uri.getScheme());
        final boolean defaultPort = secure ? port == 443 : port == 80;

        StringBuilder sb = new StringBuilder();
        sb.append("GET ").append(path).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(host);
        if (!defaultPort) sb.append(':').append(port);
        sb.append("\r\n");
        sb.append("Upgrade: websocket\r\n");
        sb.append("Connection: Upgrade\r\n");
        sb.append("Sec-WebSocket-Key: ").append(key).append("\r\n");
        sb.append("Sec-WebSocket-Version: 13\r\n");
        if (subprotocols != null && subprotocols.length > 0) {
            sb.append("Sec-WebSocket-Protocol: ");
            for (int i = 0; i < subprotocols.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(subprotocols[i]);
            }
            sb.append("\r\n");
        }
        for (int i = 0; i < requestHeaders.size(); i++) {
            sb.append(requestHeaders.getName(i)).append(": ").append(requestHeaders.getValue(i)).append("\r\n");
        }
        sb.append("\r\n");

        out.write(sb.toString().getBytes(UTF8));
        out.flush();
    }

    private ServerHandshake readHandshakeResponse(String key) throws IOException {
        final String statusLine = readLine(in);
        if (statusLine == null) {
            throw new IOException("Server closed the connection during handshake");
        }

        final String[] parts = statusLine.split(" ", 3);
        final int statusCode;
        try {
            statusCode = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;
        } catch (NumberFormatException e) {
            throw new IOException("Malformed handshake status line: " + statusLine);
        }
        final String statusMessage = parts.length > 2 ? parts[2] : "";

        final Headers headers = new Headers();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            headers.add(line);
        }

        if (statusCode >= 300 && statusCode < 400) {
            final String location = headers.getValue("Location");
            if (location == null) {
                throw new IOException("Redirect response without Location header: " + statusLine);
            }
            throw new WebSocketRedirectException(location);
        }

        if (statusCode != 101) {
            throw new IOException("Unexpected handshake response: " + statusLine);
        }

        final String accept = headers.getValue("Sec-WebSocket-Accept");
        if (!computeAcceptKey(key).equals(accept)) {
            throw new IOException("Invalid Sec-WebSocket-Accept header: " + accept);
        }

        return new ServerHandshake(statusCode, statusMessage, headers);
    }

    /** Lee una línea CRLF cruda (handshake o túnel de proxy) sin tocar el framing binario posterior. */
    private static String readLine(InputStream stream) throws IOException {
        final StringBuilder sb = new StringBuilder();
        int c;
        boolean any = false;
        while ((c = stream.read()) != -1) {
            any = true;
            if (c == '\n') break;
            if (c != '\r') sb.append((char) c);
        }
        return any ? sb.toString() : null;
    }

    private static final class WebSocketRedirectException extends IOException {
        final String location;

        WebSocketRedirectException(String location) {
            super("Redirect to " + location);
            this.location = location;
        }
    }

    private static URI resolveRedirect(URI base, String location) throws IOException {
        final URI resolved;
        try {
            resolved = base.resolve(location);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid redirect Location: " + location, e);
        }

        final String scheme = resolved.getScheme();
        if (scheme == null) {
            throw new IOException("Redirect Location missing scheme: " + location);
        }
        if ("http".equalsIgnoreCase(scheme)) {
            return withScheme(resolved, "ws");
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return withScheme(resolved, "wss");
        }
        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IOException("Unsupported redirect scheme: " + scheme);
        }
        return resolved;
    }

    private static URI withScheme(URI uri, String scheme) throws IOException {
        try {
            return new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    // ------------------------------------------------------------------
    // Envío
    // ------------------------------------------------------------------

    public boolean send(String text) {
        if (text == null) throw new NullPointerException("text == null");
        return sendMessage(OP_TEXT, text.getBytes(UTF8));
    }

    public boolean send(byte[] data) {
        if (data == null) throw new NullPointerException("data == null");
        return sendMessage(OP_BINARY, data);
    }

    public boolean sendPing(byte[] payload) {
        return sendFrame(OP_PING, payload != null ? payload : EMPTY_PAYLOAD);
    }

    private boolean sendMessage(int opcode, byte[] payload) {
        if (!open) return false;
        try {
            if (maxOutgoingFrameSize > 0 && payload.length > maxOutgoingFrameSize) {
                writeFragmented(opcode, payload);
            } else {
                writeFrame(opcode, payload, true);
            }
            return true;
        } catch (IOException e) {
            failConnection(e);
            return false;
        }
    }

    private boolean sendFrame(int opcode, byte[] payload) {
        if (!open) return false;
        try {
            writeFrame(opcode, payload, true);
            return true;
        } catch (IOException e) {
            failConnection(e);
            return false;
        }
    }

    /** Divide un mensaje grande en frames de a lo sumo {@link #maxOutgoingFrameSize} bytes. */
    private void writeFragmented(int opcode, byte[] payload) throws IOException {
        int offset = 0;
        boolean first = true;
        while (offset < payload.length) {
            final int chunkLen = Math.min(maxOutgoingFrameSize, payload.length - offset);
            final byte[] chunk = new byte[chunkLen];
            System.arraycopy(payload, offset, chunk, 0, chunkLen);
            offset += chunkLen;

            final boolean fin = offset >= payload.length;
            writeFrame(first ? opcode : OP_CONTINUATION, chunk, fin);
            first = false;
        }
    }

    /** Escribe un frame, siempre enmascarado — obligatorio para frames cliente->servidor. */
    private void writeFrame(int opcode, byte[] payload, boolean fin) throws IOException {
        synchronized (writeLock) {
            final int length = payload.length;

            out.write((fin ? 0x80 : 0x00) | opcode);

            if (length <= 125) {
                out.write(0x80 | length);
            } else if (length <= 0xFFFF) {
                out.write(0x80 | 126);
                out.write((length >>> 8) & 0xFF);
                out.write(length & 0xFF);
            } else {
                out.write(0x80 | 127);
                for (int i = 7; i >= 0; i--) {
                    out.write((int) (((long) length >>> (8 * i)) & 0xFF));
                }
            }

            final byte[] maskKey = new byte[4];
            random.nextBytes(maskKey);
            out.write(maskKey);

            final byte[] masked = new byte[length];
            for (int i = 0; i < length; i++) {
                masked[i] = (byte) (payload[i] ^ maskKey[i % 4]);
            }
            out.write(masked);
            out.flush();
        }
    }

    // ------------------------------------------------------------------
    // Keepalive (ping/pong automático)
    // ------------------------------------------------------------------

    private void pingLoop() {
        try {
            while (true) {
                Thread.sleep(pingIntervalMs);
                if (!open) return;

                pongPending = true;
                writeFrame(OP_PING, EMPTY_PAYLOAD, true);

                Thread.sleep(pingTimeoutMs);
                if (open && pongPending) {
                    throw new IOException("Ping timeout: no pong received within " + pingTimeoutMs + " ms");
                }
            }
        } catch (InterruptedException expected) {
            // cancel()/close() interrumpe este hilo intencionalmente al cerrar
        } catch (IOException e) {
            failConnection(e);
        }
    }

    private void interruptPingThread() {
        if (pingThread != null) pingThread.interrupt();
    }

    /**
     * Termina la conexión por una causa anómala (red caída, timeout de ping,
     * frame inválido, escritura fallida, etc.): cierra el socket y dispara
     * {@code onFailure} seguido de {@code onClosed(ABNORMAL_CLOSURE, ...)} —
     * igual que el navegador/React Native, que siempre disparan {@code onclose}
     * al morir la conexión, sea limpia o no. {@code onClosed} es la señal
     * universal de "la conexión terminó"; úsala para reconectar.
     */
    private void failConnection(Exception e) {
        if (!open) return;
        open = false;
        interruptPingThread();
        closeSocketQuietly();
        listener.onFailure(this, e);
        listener.onClosed(this, ABNORMAL_CLOSURE, e.getMessage());
    }

    // ------------------------------------------------------------------
    // Cierre
    // ------------------------------------------------------------------

    private static byte[] buildClosePayload(int code, String reason) {
        final byte[] reasonBytes = reason != null ? reason.getBytes(UTF8) : EMPTY_PAYLOAD;
        final byte[] payload = new byte[2 + reasonBytes.length];
        payload[0] = (byte) ((code >>> 8) & 0xFF);
        payload[1] = (byte) (code & 0xFF);
        System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
        return payload;
    }

    /**
     * Inicia el cierre "limpio": manda el frame de cierre y espera a que el
     * hilo lector reciba la confirmación del servidor para cerrar el socket y
     * disparar {@link WebSocketListener#onClosed}. Si el servidor nunca
     * responde, el socket queda abierto — usa {@link #cancel()} para forzarlo.
     */
    public synchronized boolean close(int code, String reason) {
        if (!open || closeSent) return false;
        try {
            closeSent = true;
            writeFrame(OP_CLOSE, buildClosePayload(code, reason), true);
            return true;
        } catch (IOException e) {
            failConnection(e);
            return false;
        }
    }

    public boolean close() {
        return close(NORMAL_CLOSURE, null);
    }

    /** Corta el socket de inmediato, sin esperar el handshake de cierre. No dispara {@code onClosed} ni {@code onFailure}. */
    public void cancel() {
        open = false;
        interruptPingThread();
        closeSocketQuietly();
    }

    private void closeSocketQuietly() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    // ------------------------------------------------------------------
    // Lectura (hilo lector)
    // ------------------------------------------------------------------

    private void readLoop() {
        try {
            while (true) {
                RawFrame frame = readFrame();
                switch (frame.opcode) {
                    case OP_PING:
                        writeFrame(OP_PONG, frame.payload, true);
                        break;

                    case OP_PONG:
                        pongPending = false;
                        break;

                    case OP_CLOSE:
                        handleCloseFrame(frame);
                        return;

                    case OP_TEXT:
                    case OP_BINARY:
                        if (frame.fin) {
                            dispatchMessage(frame.opcode, frame.payload);
                        } else {
                            fragmentedOpcode = frame.opcode;
                            fragmentBuffer = new ByteArrayOutputStream();
                            fragmentBuffer.write(frame.payload, 0, frame.payload.length);
                        }
                        break;

                    case OP_CONTINUATION:
                        if (fragmentBuffer == null) {
                            throw new IOException("Unexpected continuation frame");
                        }
                        fragmentBuffer.write(frame.payload, 0, frame.payload.length);
                        if (frame.fin) {
                            final byte[] complete = fragmentBuffer.toByteArray();
                            final int opcode = fragmentedOpcode;
                            fragmentBuffer = null;
                            fragmentedOpcode = -1;
                            dispatchMessage(opcode, complete);
                        }
                        break;

                    default:
                        throw new IOException("Unsupported opcode: " + frame.opcode);
                }
            }
        } catch (IOException e) {
            failConnection(e);
        }
    }

    private void dispatchMessage(int opcode, byte[] payload) throws IOException {
        if (opcode == OP_TEXT) {
            final String text;
            try {
                text = decodeStrictUtf8(payload);
            } catch (CharacterCodingException e) {
                sendCloseFrameQuietly(INVALID_PAYLOAD_DATA, "Invalid UTF-8 payload");
                throw new IOException("Invalid UTF-8 payload received from server", e);
            }
            listener.onMessage(this, text);
        } else {
            listener.onMessage(this, payload);
        }
    }

    private static String decodeStrictUtf8(byte[] bytes) throws CharacterCodingException {
        final CharsetDecoder decoder = UTF8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }

    private void sendCloseFrameQuietly(int code, String reason) {
        if (closeSent) return;
        try {
            closeSent = true;
            writeFrame(OP_CLOSE, buildClosePayload(code, reason), true);
        } catch (IOException ignored) {
        }
    }

    private void handleCloseFrame(RawFrame frame) {
        int code = NORMAL_CLOSURE;
        String reason = "";
        if (frame.payload.length >= 2) {
            code = ((frame.payload[0] & 0xFF) << 8) | (frame.payload[1] & 0xFF);
            reason = new String(frame.payload, 2, frame.payload.length - 2, UTF8);
        }

        listener.onClosing(this, code, reason);

        if (!closeSent) {
            try {
                closeSent = true;
                writeFrame(OP_CLOSE, frame.payload, true);
            } catch (IOException ignored) {
                // el socket se cierra de inmediato de todas formas
            }
        }

        open = false;
        interruptPingThread();
        closeSocketQuietly();
        listener.onClosed(this, code, reason);
    }

    private static final class RawFrame {
        final boolean fin;
        final int opcode;
        final byte[] payload;

        RawFrame(boolean fin, int opcode, byte[] payload) {
            this.fin = fin;
            this.opcode = opcode;
            this.payload = payload;
        }
    }

    private RawFrame readFrame() throws IOException {
        final int b1 = readByteOrThrow();
        final int b2 = readByteOrThrow();

        final boolean fin = (b1 & 0x80) != 0;
        final int opcode = b1 & 0x0F;
        final boolean masked = (b2 & 0x80) != 0;
        long length = b2 & 0x7F;

        if (length == 126) {
            length = (readByteOrThrow() << 8) | readByteOrThrow();
        } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; i++) {
                length = (length << 8) | readByteOrThrow();
            }
        }

        if (length < 0 || length > MAX_PAYLOAD_LENGTH) {
            throw new IOException("Frame too large: " + length);
        }

        byte[] maskKey = null;
        if (masked) {
            maskKey = new byte[4];
            readFully(maskKey);
        }

        final byte[] payload = new byte[(int) length];
        readFully(payload);

        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= maskKey[i % 4];
            }
        }

        return new RawFrame(fin, opcode, payload);
    }

    private int readByteOrThrow() throws IOException {
        final int b = in.read();
        if (b == -1) throw new IOException("Connection closed by peer");
        return b;
    }

    private void readFully(byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            final int n = in.read(buffer, offset, buffer.length - offset);
            if (n == -1) throw new IOException("Connection closed by peer");
            offset += n;
        }
    }
}
