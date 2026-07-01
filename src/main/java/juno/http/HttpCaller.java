package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpCaller {

    /**
     * Comportamiento "axios": lanza {@link HttpException} si la respuesta
     * no fue 2xx, igual que {@link java.util.concurrent.Callable#call()}
     * devuelve un valor o lanza.
     */
    public <T> T call(
        HttpRequest request,
        ResponseBodyConverter<T> converter
    ) throws Exception;
}
