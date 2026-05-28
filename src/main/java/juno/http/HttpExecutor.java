package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpExecutor {

    public <V> V send(HttpRequest httpRequest, ResponseBodyConverter<V> converter) throws Exception;

    public <V> V send(HttpRequest request, Class<V> cast) throws Exception;

    public <V> HttpResult<V> sendResult(HttpRequest request, ResponseBodyConverter<V> converter) throws Exception;

    public <V> HttpResult<V> sendResult(HttpRequest request, Class<V> cast) throws Exception;
}
