package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConvert;

public class StringResponseBodyConvert implements ResponseBodyConvert<String> {

    @Override public String parse(HttpResponse response) throws Exception {
        try {
            return response.readString();
        } finally {
            response.close();
        }
    }
}
