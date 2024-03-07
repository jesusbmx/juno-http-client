package juno.http.convert.jackson;

import com.fasterxml.jackson.databind.ObjectReader;
import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;

public class JacksonResponseBodyConvert<T> implements ResponseBodyConvert<T> {

    public final ObjectReader adapter;

    public JacksonResponseBodyConvert(ObjectReader adapter) {
        this.adapter = adapter;
    }
    
    @Override
    public T parse(ResponseBody respBody) throws Exception {
        try {
            return adapter.readValue(respBody.charStream());
        } finally {
            respBody.close();
        }
    }
    
}
