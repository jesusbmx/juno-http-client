package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConvert;
import juno.io.IOUtils;

public class SimpleResponseBodyConvert implements ResponseBodyConvert<HttpResponse> {

    @Override
    public HttpResponse parse(HttpResponse response) throws Exception {
        try {
            byte[] content = IOUtils.readByteArray(response.content);

            final HttpResponse result = new HttpResponse(
                    response.code, response.status, response.headers, content);
            result.charset = response.charset;
            return result;
            
        } finally {
            response.close();
        }
    }
    
}
