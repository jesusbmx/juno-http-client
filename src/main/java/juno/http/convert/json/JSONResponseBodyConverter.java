package juno.http.convert.json;

import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConverter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONResponseBodyConverter {

    private static String readAndValidateJson(
            HttpResponse response
    ) throws JsonConversionException {

        try {
            String json = response.readString();

            if (json == null)
                throw new JsonConversionException(
                        "Response body is null",
                        response,
                        null,
                        null
                );

            json = json.trim();

            if (json.isEmpty())
                throw new JsonConversionException(
                        "Response body is empty",
                        response,
                        json,
                        null
                );

            return json;

        } catch (Exception e) {

            if (e instanceof JsonConversionException)
                throw (JsonConversionException)e;

            throw new JsonConversionException(
                    "Failed to read response body",
                    response,
                    null,
                    e
            );
        }
    }

    public static class Obj implements ResponseBodyConverter<JSONObject> {

        public static final Obj INSTANCE = new Obj();

        @Override
        public JSONObject convert(
                HttpResponse response
        ) throws Exception {

            String json = readAndValidateJson(response);

            try {
                return new JSONObject(json);

            } catch (JSONException e) {

                throw new JsonConversionException(
                        "Failed parsing JSONObject",
                        response,
                        json,
                        e
                );
            }
        }
    }

    public static class Array implements ResponseBodyConverter<JSONArray> {

        public static final Array INSTANCE = new Array();

        @Override
        public JSONArray convert(
                HttpResponse response
        ) throws Exception {

            String json = readAndValidateJson(response);

            try {
                return new JSONArray(json);

            } catch (JSONException e) {

                throw new JsonConversionException(
                        "Failed parsing JSONArray",
                        response,
                        json,
                        e
                );
            }
        }
    }
}