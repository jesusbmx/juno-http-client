package juno.http.convert.generic;

import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;
import juno.io.IOUtils;

public class StringResponseBodyConvert implements ResponseBodyConvert<String> {

    @Override public String parse(ResponseBody respBody) throws Exception {
        try {
            byte[] data = IOUtils.readByteArray(respBody.in);
            return new String(data, respBody.charset);
        } finally {
            respBody.close();
        }
    }
}
