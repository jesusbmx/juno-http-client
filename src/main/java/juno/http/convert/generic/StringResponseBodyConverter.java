package juno.http.convert.generic;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConverter;

public class StringResponseBodyConverter implements ResponseBodyConverter<String> {
    
    public static final StringResponseBodyConverter INSTANCE = new StringResponseBodyConverter();

    @Override public String convert(HttpResponse response) throws Exception {
        try {
            return response.readString();
        } finally {
            response.close();
        }
    }
}
