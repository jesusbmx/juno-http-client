package juno.http.convert.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import juno.http.RequestBody;
import juno.http.convert.RequestBodyConvert;

public class JacksonRequestBodyConvert<T> implements RequestBodyConvert<T> {
    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";
    //private static final Charset UTF_8 = Charset.forName("UTF-8");
    
    public final ObjectWriter adapter;

    public JacksonRequestBodyConvert(ObjectWriter adapter) {
        this.adapter = adapter;
    }
  
    @Override
    public RequestBody parse(T value) throws Exception {
        byte[] bytes = adapter.writeValueAsBytes(value);
        return RequestBody.create(MEDIA_TYPE, bytes);
    }
      
}
