package juno.http.convert.gson;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;

public class GsonResponseBodyConvert<T> implements ResponseBodyConvert<T> {

    public final Gson gson;
    public final TypeAdapter<T> adapter;

    public GsonResponseBodyConvert(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }
    
    @Override
    public T parse(ResponseBody respBody) throws Exception {
      JsonReader jsonReader = gson.newJsonReader(respBody.charStream());
      try {
        T result = adapter.read(jsonReader);
        if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
          throw new JsonIOException("JSON document was not fully consumed.");
        }
        return result;
      } finally {
        respBody.close();
      }
    }
    
}
