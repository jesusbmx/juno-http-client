package juno.http.ws;

import juno.http.Headers;

/**
 * Callbacks del ciclo de vida de un {@link WebSocket}, equivalentes a los del
 * WebSocket nativo del navegador/React Native ({@code onopen}/{@code onmessage}/
 * {@code onclose}/{@code onerror}).
 *
 * <p>Todos los métodos se invocan desde el hilo lector interno del socket, nunca
 * desde el hilo que llamó a {@link WebSocket#connect()}.
 */
public interface WebSocketListener {

    /** El handshake HTTP de actualización a WebSocket se completó correctamente. */
    void onOpen(WebSocket ws, Headers responseHeaders);

    /** Llega un mensaje de texto (frame o secuencia de frames fragmentados ya reensamblados). */
    void onMessage(WebSocket ws, String text);

    /** Llega un mensaje binario (frame o secuencia de frames fragmentados ya reensamblados). */
    void onMessage(WebSocket ws, byte[] bytes);

    /** Se recibió un frame de cierre del servidor; el socket se cerrará justo después. */
    void onClosing(WebSocket ws, int code, String reason);

    /** El cierre (propio o del servidor) terminó y el socket ya está cerrado. No hay más callbacks tras este. */
    void onClosed(WebSocket ws, int code, String reason);

    /**
     * La conexión terminó de forma anómala (error de handshake, IOException durante
     * la lectura/escritura, cierre inesperado del socket). No hay más callbacks tras este.
     */
    void onFailure(WebSocket ws, Exception e);
}
