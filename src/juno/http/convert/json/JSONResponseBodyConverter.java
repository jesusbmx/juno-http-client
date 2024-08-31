package juno.http.convert.json;

import juno.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import juno.http.convert.ResponseBodyConverter;

public class JSONResponseBodyConverter {

    public static class Obj implements ResponseBodyConverter<JSONObject> {
        
        public static final Obj INSTANCE = new Obj();

        @Override
        public JSONObject convert(HttpResponse response) throws Exception {
            try {
                String json = response.readString();

                if (json == null) {
                    throw new JSONException("json == null");
                }

                if (json.isEmpty()) {
                    throw new JSONException("json is empty");
                }

                return new JSONObject(json);

            } finally {
                response.close();
            }
        }
    }

    public static class Array implements ResponseBodyConverter<JSONArray> {
        
         public static final Array INSTANCE = new Array();

        @Override
        public JSONArray convert(HttpResponse response) throws Exception {
            try {
                String json = response.readString();

                if (json == null) {
                    throw new JSONException("json == null");
                }

                if (json.isEmpty()) {
                    throw new JSONException("json is empty");
                }

                return new JSONArray(json);

            } finally {
                response.close();
            }
        }
    }

}
