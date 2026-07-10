package juno.http.ws;

import juno.http.Headers;

/**
 * Respuesta HTTP del handshake de actualización a WebSocket: código/mensaje de
 * status ({@code 101 Switching Protocols} en un handshake exitoso) más los
 * headers de la respuesta. Mismo rol que {@code ServerHandshake} en Java-WebSocket.
 */
public class ServerHandshake extends Headers {

    private final int httpStatus;
    private final String httpStatusMessage;

    public ServerHandshake(int httpStatus, String httpStatusMessage, Headers headers) {
        super(headers);
        this.httpStatus = httpStatus;
        this.httpStatusMessage = httpStatusMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getHttpStatusMessage() {
        return httpStatusMessage;
    }
}
