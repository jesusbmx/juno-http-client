package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpFetcher {

    /**
     * Comportamiento "fetch": nunca lanza por status HTTP, expone el
     * resultado (éxito o error) en {@link HttpResult}.
     */
    public <T> HttpResult<T> fetch(
        HttpRequest request,
        ResponseBodyConverter<T> converter
    ) throws Exception;
}
