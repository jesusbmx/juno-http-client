package juno.http.convert.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import juno.http.RequestBody;
import juno.http.convert.RequestBodyConverter;

public class JacksonRequestBodyConvert<T> implements RequestBodyConverter<T> {
    
    public final ObjectWriter adapter;
    public final String contentType;

    public JacksonRequestBodyConvert(ObjectWriter adapter, String contentType) {
        this.adapter = adapter;
        this.contentType = contentType;
    }
  
    @Override
    public RequestBody convert(T value) throws Exception {
        byte[] bytes = adapter.writeValueAsBytes(value);
        return RequestBody.create(contentType, bytes);
    }      
}
