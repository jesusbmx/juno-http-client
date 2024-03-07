package juno.http.convert;

import juno.http.ResponseBody;

public class BytesResponseBodyConvert implements ResponseBodyConvert<ResponseBody> {

    @Override
    public ResponseBody parse(ResponseBody respBody) throws Exception {
      try {
        final byte[] data = respBody.bytes();
        return new BytesResponseBody(respBody, data);
      } finally {
        respBody.close();
      }
    }
    
}
