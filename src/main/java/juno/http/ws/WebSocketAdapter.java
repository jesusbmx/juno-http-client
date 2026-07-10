package juno.http.ws;

/**
 * Implementación vacía de {@link WebSocketListener} para sobreescribir solo los
 * métodos que interesan, igual que {@code juno.concurrent.CallbackAdapter}.
 */
public abstract class WebSocketAdapter implements WebSocketListener {

    @Override
    public void onOpen(WebSocket ws, ServerHandshake handshake) {
    }

    @Override
    public void onMessage(WebSocket ws, String text) {
    }

    @Override
    public void onMessage(WebSocket ws, byte[] bytes) {
    }

    @Override
    public void onClosing(WebSocket ws, int code, String reason) {
    }

    @Override
    public void onClosed(WebSocket ws, int code, String reason) {
    }

    @Override
    public void onFailure(WebSocket ws, Exception e) {
    }
}
