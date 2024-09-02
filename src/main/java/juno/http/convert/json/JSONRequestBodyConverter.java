package juno.http.convert.json;

import juno.http.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import juno.http.convert.RequestBodyConverter;

public class JSONRequestBodyConverter {
    private static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    public static class Obj implements RequestBodyConverter<JSONObject> {

        public static final Obj INSTANCE = new Obj();
        
        @Override
        public RequestBody convert(JSONObject obj) throws Exception {
            String json = obj.toString();
            return RequestBody.create(MEDIA_TYPE, json);
        }
    }

    public static class Array implements RequestBodyConverter<JSONArray> {
        
        public static final Array INSTANCE = new Array();

        @Override
        public RequestBody convert(JSONArray obj) throws Exception {
            String json = obj.toString();
            return RequestBody.create(MEDIA_TYPE, json);
        }
    }
}
