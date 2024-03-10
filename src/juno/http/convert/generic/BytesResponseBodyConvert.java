package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConvert;

public class BytesResponseBodyConvert implements ResponseBodyConvert<byte[]> {

    @Override
    public byte[] parse(HttpResponse response) throws Exception {
      try {
        return response.readBytes();
      } finally {
        response.close();
      }
    }
    
}
