package juno.http;

import juno.http.convert.ResponseBodyConverter;

public interface HttpRequestExecutor {
    
    public <V> V send(HttpRequest httpRequest, ResponseBodyConverter<V> converter) throws Exception;
     
    public <V> V send(HttpRequest request, Class<V> cast) throws Exception;
}
