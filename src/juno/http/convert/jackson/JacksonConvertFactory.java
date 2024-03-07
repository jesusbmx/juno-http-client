package juno.http.convert.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import juno.http.convert.ConvertFactory;
import java.util.HashMap;
import java.util.Map;
import juno.http.convert.RequestBodyConvert;
import juno.http.convert.ResponseBodyConvert;

public class JacksonConvertFactory extends ConvertFactory {

    public final ObjectMapper mapper;

    public JacksonConvertFactory(ObjectMapper mapper) {
        this.mapper = mapper;
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
