package juno.http.convert;

import juno.http.HttpResponse;

public interface ResponseBodyConverter<T> {
    
    public T convert(HttpResponse response) throws Exception;
}
