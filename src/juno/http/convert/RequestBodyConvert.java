package juno.http.convert;

import juno.http.RequestBody;

public interface RequestBodyConvert<T> {
    
    public RequestBody parse(T obj) throws Exception;
}
