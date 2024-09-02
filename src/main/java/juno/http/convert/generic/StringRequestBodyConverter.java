package juno.http.convert.generic;

import juno.http.RequestBody;
import juno.http.convert.RequestBodyConverter;

public class StringRequestBodyConverter implements RequestBodyConverter<String> {
    
    private static final String MEDIA_TYPE = "text/plain; charset=UTF-8";

    public static final StringRequestBodyConverter INSTANCE = new StringRequestBodyConverter();
    
    @Override
    public RequestBody convert(String obj) throws Exception {
        return RequestBody.create(MEDIA_TYPE, String.valueOf(obj));
    }
    
}
