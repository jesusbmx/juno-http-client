package juno.http.convert.generic;

import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;

public class BytesResponseBodyConvert implements ResponseBodyConvert<byte[]> {

    @Override
    public byte[] parse(ResponseBody respBody) throws Exception {
      try {
        return respBody.readBytes();
      } finally {
        respBody.close();
      }
    }
    
}
