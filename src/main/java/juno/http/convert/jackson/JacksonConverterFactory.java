package juno.http.convert.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import juno.http.convert.ConverterFactory;
import juno.http.convert.RequestBodyConverter;
import juno.http.convert.ResponseBodyConverter;

public class JacksonConverterFactory implements ConverterFactory {
    
    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";
    //private static final Charset UTF_8 = Charset.forName("UTF-8");

    public final ObjectMapper mapper;

    public JacksonConverterFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }
   
    @Override
    public <V> RequestBodyConverter<V> requestBodyConverter(final Class<V> type) {
        ObjectWriter writer = mapper.writerFor(type);
        return new JacksonRequestBodyConvert<V>(writer, MEDIA_TYPE);
    }
    
    @Override
    public <V> ResponseBodyConverter<V> responseBodyConverter(final Class<V> type) {
        ObjectReader reader = mapper.readerFor(type);
        return new JacksonResponseBodyConvert<V>(reader);
    }
}
