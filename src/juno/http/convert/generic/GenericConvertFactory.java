package juno.http.convert.generic;

import java.util.Map;
import juno.http.convert.ConvertFactory;
import juno.http.convert.RequestBodyConvert;
import juno.http.convert.ResponseBodyConvert;
import juno.http.convert.json.JSONRequestBodyConvert;

public class GenericConvertFactory extends ConvertFactory {
    
    /** Singleton de la clase. */
    private static ConvertFactory instance;

    public GenericConvertFactory() {
    }
    
    public static ConvertFactory getInstance() {
        if (instance == null)
            instance = new GenericConvertFactory();
        return instance;
    }
    
    @Override
    public <V> RequestBodyConvert<V> createRequestBodyConvert(Class<V> classOf) {
 //       String name = classOf.getCanonicalName();
 //       throw new RuntimeException("No adapter found for class '"+name+"'");
       return new JSONRequestBodyConvert.Any<V>(classOf);
    }
    
    @Override
    public <V> ResponseBodyConvert<V> createResponseBodyConvert(Class<V> classOf) {
        String name = classOf.getCanonicalName();
        throw new RuntimeException("No adapter found for class '"+name+"'");
    }

    @Override
    public <V> Map<String, Object> convertObjectToMap(V src) {
        try {
            Class<V> classOf = (Class<V>) src.getClass();
            ObjectAdapter<V> adapter = ObjectAdapter.get(classOf);
            return adapter.toMap(src);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
