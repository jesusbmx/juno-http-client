package juno.http.convert.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import juno.http.convert.ConverterFactory;
import juno.http.convert.RequestBodyConverter;
import juno.http.convert.ResponseBodyConverter;

public class GsonConverterFactory implements ConverterFactory {

    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";
    //private static final Charset UTF_8 = Charset.forName("UTF-8");
    public final Gson gson;

    public GsonConverterFactory(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public <V> RequestBodyConverter<V> requestBodyConverter(final Class<V> type) {
        TypeAdapter<V> adapter = gson.getAdapter(type);
        return new GsonRequestBodyConverter<V>(adapter, MEDIA_TYPE);
    }
    
    @Override
    public <V> ResponseBodyConverter<V> responseBodyConverter(final Class<V> type) {
        TypeAdapter<V> adapter = gson.getAdapter(type);
        return new GsonResponseBodyConverter<V>(gson, adapter);
    } 
}
