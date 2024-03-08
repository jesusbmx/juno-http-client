package juno.http.convert.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import juno.http.convert.ConvertFactory;
import juno.http.convert.RequestBodyConvert;
import juno.http.convert.ResponseBodyConvert;

public class GsonConvertFactory extends ConvertFactory {

    public final Gson gson;

    public GsonConvertFactory(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public <V> RequestBodyConvert<V> createRequestBodyConvert(final Class<V> classOf) {
        TypeAdapter<V> adapter = gson.getAdapter(classOf);
        return new GsonRequestBodyConvert<V>(gson, adapter);
    }
    
    @Override
    public <V> ResponseBodyConvert<V> createResponseBodyConvert(final Class<V> classOf) {
        TypeAdapter<V> adapter = gson.getAdapter(classOf);
        return new GsonResponseBodyConvert<V>(gson, adapter);
    }

    @Override
    public <V> Map<String, Object> convertObjectToMap(V src) {
        if (src == null) return null;
        Class<V> classOf = (Class<V>) src.getClass();
        
        TypeAdapter<V> adapter = gson.getAdapter(classOf);
        String json = adapter.toJson(src);
        
        Type empMapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, empMapType);
    }   
}
