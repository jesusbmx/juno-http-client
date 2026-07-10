package juno.http.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
 * <pre>{@code
 * WebSocket ws = new WebSocket("wss://example.com/chat?token=" + token,
 *     new WebSocketAdapter() {
 *       @Override public void onOpen(WebSocket ws, Headers headers) {
 *         System.out.println("connected");
 *       }
 *       @Override public void onMessage(WebSocket ws, String text) {
 *         System.out.println("message: " + text);
 *       }
 *       @Override public void onFailure(WebSocket ws, Exception e) {
 *         // reconectar aquí, igual que el onclose del hook de React Native
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

    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final int OP_CONTINUATION = 0x0;
    private static final int OP_TEXT = 0x1;
    private static final int OP_BINARY = 0x2;
    private static final int OP_CLOSE = 0x8;
    private static final int OP_PING = 0x9;
    private static final int OP_PONG = 0xA;

    /** Límite defensivo contra frames con una longitud declarada absurda (servidor malicioso/bugueado). */
    private static final long MAX_PAYLOAD_LENGTH = 16L * 1024 * 1024;

    private final URI uri;
    private final Headers requestHeaders;
    private final WebSocketListener listener;
    private final SecureRandom random = new SecureRandom();
    private final Object writeLock = new Object();

    private int connectTimeoutMs = 10000;

    private Socket socket;
    private OutputStream out;
    private InputStream in;

    private volatile boolean open;
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

    public boolean isOpen() {
        return open;
    }

    // ------------------------------------------------------------------
    // Conexión
    // ------------------------------------------------------------------

    /**
     * Abre el socket TCP (TLS si es wss://), realiza el handshake HTTP de
     * actualización a WebSocket y, si tiene éxito, arranca el hilo lector en
     * segundo plano. Llama a {@link WebSocketListener#onOpen} antes de retornar.
     *
     * @throws IOException si falla la conexión o el servidor no acepta el handshake.
     */
    public synchronized void connect() throws IOException {
        if (socket != null) throw new IllegalStateException("connect() already called");

        final boolean secure = "wss".equalsIgnoreCase(uri.getScheme());
        final String host = uri.getHost();
        final int port = uri.getPort() != -1 ? uri.getPort() : (secure ? 443 : 80);

        socket = secure ? SSLSocketFactory.getDefault().createSocket() : new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
            socket.setTcpNoDelay(true);
            if (secure) {
                ((SSLSocket) socket).startHandshake();
            }

            out = socket.getOutputStream();
            in = socket.getInputStream();

            final String key = generateWebSocketKey();
            writeHandshakeRequest(host, port, key);
            final Headers responseHeaders = readHandshakeResponse(key);

            open = true;
            Thread reader = new Thread(new Runnable() {
                @Override
                public void run() {
                    readLoop();
                }
            }, "WebSocket-Reader");
            reader.setDaemon(true);
            reader.start();

            listener.onOpen(this, responseHeaders);

        } catch (IOException e) {
            closeSocketQuietly();
            throw e;
        }
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
        for (int i = 0; i < requestHeaders.size(); i++) {
            sb.append(requestHeaders.getName(i)).append(": ").append(requestHeaders.getValue(i)).append("\r\n");
        }
        sb.append("\r\n");

        out.write(sb.toString().getBytes(UTF8));
        out.flush();
    }

    private Headers readHandshakeResponse(String key) throws IOException {
        final String statusLine = readLine();
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
        if (statusCode != 101) {
            throw new IOException("Unexpected handshake response: " + statusLine);
        }

        final Headers headers = new Headers();
        String line;
        while ((line = readLine()) != null && !line.isEmpty()) {
            headers.add(line);
        }

        final String accept = headers.getValue("Sec-WebSocket-Accept");
        if (!computeAcceptKey(key).equals(accept)) {
            throw new IOException("Invalid Sec-WebSocket-Accept header: " + accept);
        }

        return headers;
    }

    /** Lee una línea CRLF cruda del handshake. Se detiene justo en el límite de los headers, sin tocar el framing binario posterior. */
    private String readLine() throws IOException {
        final StringBuilder sb = new StringBuilder();
        int c;
        boolean any = false;
        while ((c = in.read()) != -1) {
            any = true;
            if (c == '\n') break;
            if (c != '\r') sb.append((char) c);
        }
        return any ? sb.toString() : null;
    }

    // ------------------------------------------------------------------
    // Envío
    // ------------------------------------------------------------------

    public boolean send(String text) {
        if (text == null) throw new NullPointerException("text == null");
        return sendFrame(OP_TEXT, text.getBytes(UTF8));
    }

    public boolean send(byte[] data) {
        if (data == null) throw new NullPointerException("data == null");
        return sendFrame(OP_BINARY, data);
    }

    public boolean sendPing(byte[] payload) {
        return sendFrame(OP_PING, payload != null ? payload : new byte[0]);
    }

    private boolean sendFrame(int opcode, byte[] payload) {
        if (!open) return false;
        try {
            writeFrame(opcode, payload);
            return true;
        } catch (IOException e) {
            listener.onFailure(this, e);
            return false;
        }
    }

    /** Escribe un frame único (sin fragmentar), siempre enmascarado — obligatorio para frames cliente->servidor. */
    private void writeFrame(int opcode, byte[] payload) throws IOException {
        synchronized (writeLock) {
            final int length = payload.length;

            out.write(0x80 | opcode); // FIN=1

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
    // Cierre
    // ------------------------------------------------------------------

    /**
     * Inicia el cierre "limpio": manda el frame de cierre y espera a que el
     * hilo lector reciba la confirmación del servidor para cerrar el socket y
     * disparar {@link WebSocketListener#onClosed}. Si el servidor nunca
     * responde, el socket queda abierto — usa {@link #cancel()} para forzarlo.
     */
    public synchronized boolean close(int code, String reason) {
        if (!open || closeSent) return false;
        try {
            final byte[] reasonBytes = reason != null ? reason.getBytes(UTF8) : new byte[0];
            final byte[] payload = new byte[2 + reasonBytes.length];
            payload[0] = (byte) ((code >>> 8) & 0xFF);
            payload[1] = (byte) (code & 0xFF);
            System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);

            closeSent = true;
            writeFrame(OP_CLOSE, payload);
            return true;
        } catch (IOException e) {
            listener.onFailure(this, e);
            return false;
        }
    }

    public boolean close() {
        return close(NORMAL_CLOSURE, null);
    }

    /** Corta el socket de inmediato, sin esperar el handshake de cierre. No dispara {@code onClosed} ni {@code onFailure}. */
    public void cancel() {
        open = false;
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
                        writeFrame(OP_PONG, frame.payload);
                        break;

                    case OP_PONG:
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
            if (open) {
                open = false;
                closeSocketQuietly();
                listener.onFailure(this, e);
            }
        }
    }

    private void dispatchMessage(int opcode, byte[] payload) {
        if (opcode == OP_TEXT) {
            listener.onMessage(this, new String(payload, UTF8));
        } else {
            listener.onMessage(this, payload);
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
                writeFrame(OP_CLOSE, frame.payload);
            } catch (IOException ignored) {
                // el socket se cierra de inmediato de todas formas
            }
        }

        open = false;
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
