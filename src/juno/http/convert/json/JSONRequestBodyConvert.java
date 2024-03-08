package juno.http.convert.json;

import java.util.Map;
import juno.http.RequestBody;
import juno.http.convert.RequestBodyConvert;
import juno.http.convert.internal.ObjectAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONRequestBodyConvert implements RequestBodyConvert<JSON> {
    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    @Override
    public RequestBody parse(JSON obj) throws Exception {
      String json = obj.toString();
      return RequestBody.create(MEDIA_TYPE, json);
    }
    
    public static class Obj implements RequestBodyConvert<JSONObject> {

        @Override
        public RequestBody parse(JSONObject obj) throws Exception {
            String json = obj.toString();
            return RequestBody.create(MEDIA_TYPE, json);
        }
    }

    public static class Array implements RequestBodyConvert<JSONArray> {

        @Override
        public RequestBody parse(JSONArray obj) throws Exception {
            String json = obj.toString();
            return RequestBody.create(MEDIA_TYPE, json);
        }
    }
    
    public static class Any<T> implements RequestBodyConvert<T> {
        public final Class<T> classOf;

        public Any(Class<T> classOf) {
            this.classOf = classOf;
        }
        
        @Override
        public RequestBody parse(T obj) throws Exception {
            ObjectAdapter<T> adapter = ObjectAdapter.get(classOf);
            
            Map<String, Object> vars = adapter.toMap(obj);
            JSONObject json = new JSONObject();
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                json.putOpt(entry.getKey(), entry.getValue());
            }
            
            String content = json.toString();
            return RequestBody.create(MEDIA_TYPE, content);
        }
    }
}
