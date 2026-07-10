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

    /** Se recibió un frame de cierre del servidor; el socket se cerrará justo después. Solo aplica al cierre limpio. */
    void onClosing(WebSocket ws, int code, String reason);

    /**
     * El socket ya está cerrado — señal universal de "la conexión terminó",
     * igual que {@code onclose} en el navegador/React Native. Se dispara
     * SIEMPRE al final de la vida del socket, sea un cierre limpio (código
     * {@link WebSocket#NORMAL_CLOSURE}, tras {@link #onClosing}) o anómalo
     * (código {@link WebSocket#ABNORMAL_CLOSURE}, precedido por {@link #onFailure}).
     * Este es el lugar correcto para implementar reconexión — no {@code onFailure}.
     */
    void onClosed(WebSocket ws, int code, String reason);

    /**
     * Diagnóstico opcional: la conexión se cayó por una causa anómala (error de
     * handshake, IOException durante lectura/escritura, timeout de ping, frame
     * inválido). Siempre va seguido de {@link #onClosed} con
     * {@link WebSocket#ABNORMAL_CLOSURE} — no lo uses para decidir si reconectar,
     * úsalo solo para loguear/diagnosticar el motivo.
     */
    void onFailure(WebSocket ws, Exception e);
}
