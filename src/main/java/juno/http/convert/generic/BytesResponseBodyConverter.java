package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConverter;

public class BytesResponseBodyConverter implements ResponseBodyConverter<byte[]> {
    
    public static final BytesResponseBodyConverter INSTANCE = new BytesResponseBodyConverter();

    @Override
    public byte[] convert(HttpResponse response) throws Exception {
      try {
        return response.readBytes();
      } finally {
        response.close();
      }
    }
    
}
