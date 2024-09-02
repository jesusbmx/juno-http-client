package juno.http.convert.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import juno.http.RequestBody;
import juno.http.convert.RequestBodyConverter;

public class GsonRequestBodyConverter<T> implements RequestBodyConverter<T> {

    public final Gson gson;
    public final TypeAdapter<T> adapter;
    public final String contentType;

    public GsonRequestBodyConverter(Gson gson, TypeAdapter<T> adapter, String contentType) {
        this.gson = gson;
        this.adapter = adapter;
        this.contentType = contentType;
    }
    
    @Override
    public RequestBody convert(T value) throws Exception {
//        ByteArrayOutputStream bytes = IOUtils.arrayOutputStream();
//        Writer writer = new OutputStreamWriter(bytes, UTF_8);
//        JsonWriter jsonWriter = gson.newJsonWriter(writer);
//        adapter.write(jsonWriter, value);
//        jsonWriter.close();
//        return RequestBody.create(MEDIA_TYPE, bytes.toByteArray(), true);

        String json = adapter.toJson(value);
        return RequestBody.create(contentType, json);
    }
      
}
