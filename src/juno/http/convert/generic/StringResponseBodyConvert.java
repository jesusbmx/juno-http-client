package juno.http.convert.generic;

import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;

public class StringResponseBodyConvert implements ResponseBodyConvert<String> {

    @Override public String parse(ResponseBody respBody) throws Exception {
        try {
            return respBody.readString();
        } finally {
            respBody.close();
        }
    }
}
