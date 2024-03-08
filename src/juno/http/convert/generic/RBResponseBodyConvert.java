package juno.http.convert.generic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;
import juno.io.IOUtils;

public class RBResponseBodyConvert implements ResponseBodyConvert<ResponseBody> {

    @Override
    public ResponseBody parse(ResponseBody respBody) throws Exception {
        try {
            byte[] data = IOUtils.readByteArray(respBody.in);
            final InputStream in = new ByteArrayInputStream(data);
            
            final ResponseBody response = new ResponseBody(in);
            response.charset = respBody.charset;
            response.code = respBody.code;
            response.status = respBody.status;
            response.headers.addHeaders(respBody.headers);
            return response;
            
        } finally {
            respBody.close();
        }
    }
    
}
