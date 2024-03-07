package juno.http.convert;

import juno.http.ResponseBody;

public interface ResponseBodyConvert<T> {
    
    public T parse(ResponseBody respBody) throws Exception;
}
