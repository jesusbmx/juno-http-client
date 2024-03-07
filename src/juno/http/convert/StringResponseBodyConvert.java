package juno.http.convert;

import juno.http.ResponseBody;

public class StringResponseBodyConvert implements ResponseBodyConvert<String> {

    @Override public String parse(ResponseBody respBody) throws Exception {
        try {
            return respBody.string();
        } finally {
            respBody.close();
        }
    }
}
