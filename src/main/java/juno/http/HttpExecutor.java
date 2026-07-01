package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpExecutor {

    /**
     * Comportamiento "axios": lanza {@link HttpException} si la respuesta
     * no fue 2xx, igual que {@link java.util.concurrent.Callable#call()}
     * devuelve un valor o lanza.
     */
    public <V> HttpResult<V> execute(
        HttpRequest request,
        ResponseBodyConverter<V> converter
    ) throws Exception;
}
