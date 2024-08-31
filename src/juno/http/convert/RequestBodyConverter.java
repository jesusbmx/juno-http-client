package juno.http.convert;

import juno.http.RequestBody;

public interface RequestBodyConverter<T> {
    
    public RequestBody convert(T obj) throws Exception;
}
