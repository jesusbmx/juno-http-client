package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpExecutor {

    public <T> HttpResult<T> fetch(
        HttpRequest request,
        ResponseBodyConverter<T> converter
    ) throws Exception;

    public <T> T send(
        HttpRequest request,
        ResponseBodyConverter<T> converter
    ) throws Exception;
}
