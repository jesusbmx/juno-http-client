package juno.http.convert.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.HashMap;
import java.util.Map;
import juno.http.convert.ConvertFactory;
import juno.http.convert.RequestBodyConvert;
import juno.http.convert.ResponseBodyConvert;

public class JacksonConvertFactory extends ConvertFactory {

    public final ObjectMapper mapper;

    public JacksonConvertFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    public JacksonConvertFactory() {
        this(createObjectMapper());
    }
    
    public static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
         // Ignoramos las varaiables del json que no hacen mach con el modelo.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Configuramos la visibilidad
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE);
        
        return mapper;
    }
    
    @Override
    public <V> RequestBodyConvert<V> createRequestBodyConvert(final Class<V> classOf) {
        ObjectWriter writer = mapper.writerFor(classOf);
        return new JacksonRequestBodyConvert<V>(writer);
    }
    
    @Override
    public <V> ResponseBodyConvert<V> createResponseBodyConvert(final Class<V> classOf) {
        ObjectReader reader = mapper.readerFor(classOf);
        return new JacksonResponseBodyConvert<V>(reader);
    }

    @Override
    public <V> Map<String, Object> convertObjectToMap(V src) {
        if (src == null) return null;
        
        try {
            TypeReference<HashMap<String, Object>> typeRef 
                    = new TypeReference<HashMap<String, Object>>() {};
            
            return mapper.convertValue(src, typeRef);
            
        } catch(IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }   
}
