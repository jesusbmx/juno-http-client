package juno.http.convert;

import juno.http.HttpResponse;

public interface ResponseBodyConvert<T> {
    
    public T parse(HttpResponse response) throws Exception;
}
