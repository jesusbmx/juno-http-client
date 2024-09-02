package juno.http.convert.gson;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.InputStreamReader;
import java.io.Reader;
import juno.http.HttpResponse;
import juno.io.IOUtils;
import juno.http.convert.ResponseBodyConverter;

public class GsonResponseBodyConverter<T> implements ResponseBodyConverter<T> {

    public final Gson gson;
    public final TypeAdapter<T> adapter;

    public GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }
    
    @Override
    public T convert(HttpResponse response) throws Exception {
      Reader reader = null;
      try {
        reader = new InputStreamReader(
                response.content, response.getCharsetFromContentType());
        
        final JsonReader jsonReader = gson.newJsonReader(reader);
        
        final T result = adapter.read(jsonReader);
        if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
          throw new JsonIOException("JSON document was not fully consumed.");
        }
        return result;
        
      } finally {
        IOUtils.closeQuietly(reader);
        response.close();
      }
    }
    
}
