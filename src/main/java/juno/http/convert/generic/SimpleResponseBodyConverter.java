package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConverter;
import juno.io.IOUtils;

public class SimpleResponseBodyConverter implements ResponseBodyConverter<HttpResponse> {
    
    public static final SimpleResponseBodyConverter INSTANCE = new SimpleResponseBodyConverter();

    @Override
    public HttpResponse convert(HttpResponse response) throws Exception {
        try {
            byte[] content = IOUtils.readByteArray(response.content);

            return new HttpResponse(
                    response.code, 
                    response.headers, 
                    content);

        } finally {
            response.close();
        }
    }
    
}
