package juno.http.convert.generic;

import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;
import juno.io.IOUtils;

public class BytesResponseBodyConvert implements ResponseBodyConvert<byte[]> {

    @Override
    public byte[] parse(ResponseBody respBody) throws Exception {
      try {
        return IOUtils.readByteArray(respBody.in);
      } finally {
        respBody.close();
      }
    }
    
}
