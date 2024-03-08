package juno.http;

import juno.http.convert.ResponseBodyConvert;

public interface HttpExecutor {
    
    public ResponseBody execute(HttpRequest request) throws Exception;
 
    public <V> V execute(HttpRequest request, ResponseBodyConvert<V> convert) throws Exception;
    
    public <V> V execute(HttpRequest request, Class<V> convert) throws Exception;
}
