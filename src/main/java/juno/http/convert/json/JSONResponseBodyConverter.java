package juno.http.convert.json;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConverter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONResponseBodyConverter {

    private static String readAndValidateJson(HttpResponse response) throws Exception {
        String json = null;
        try {
            json = response.readString();
            if (json == null || json.isEmpty()) {
                throw new JSONException("Response body is null or empty");
            }
            return json;
        } finally {
            response.close();
        }
    }

    public static class Obj implements ResponseBodyConverter<JSONObject> {

        public static final Obj INSTANCE = new Obj();

        @Override
        public JSONObject convert(HttpResponse response) throws Exception {
            String json = readAndValidateJson(response);
            try {
                return new JSONObject(json);
            } catch (JSONException e) {
                throw new Exception("Failed to parse JSONObject from response: " + json, e);
            }
        }
    }

    public static class Array implements ResponseBodyConverter<JSONArray> {

        public static final Array INSTANCE = new Array();

        @Override
        public JSONArray convert(HttpResponse response) throws Exception {
            String json = readAndValidateJson(response);
            try {
                return new JSONArray(json);
            } catch (JSONException e) {
                throw new Exception("Failed to parse JSONArray from response: " + json, e);
            }
        }
    }
}
