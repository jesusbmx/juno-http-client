package juno.http.convert.json;

import juno.http.convert.ConverterFactory;
import juno.http.convert.RequestBodyConverter;
import juno.http.convert.ResponseBodyConverter;

public class JSONConverterFactory implements ConverterFactory {

    @Override
    public <V> RequestBodyConverter<V> requestBodyConverter(Class<V> type) {
        if (isJSONObjectType(type)) {
            return (RequestBodyConverter<V>) JSONRequestBodyConverter.Obj.INSTANCE;
        } else if (isJSONArrayType(type)) {
            return (RequestBodyConverter<V>) JSONRequestBodyConverter.Array.INSTANCE;
        }
        return null;
    }

    @Override
    public <V> ResponseBodyConverter<V> responseBodyConverter(Class<V> type) {
        if (isJSONObjectType(type)) {
            return (ResponseBodyConverter<V>) JSONResponseBodyConverter.Obj.INSTANCE;
        } else if (isJSONArrayType(type)) {
            return (ResponseBodyConverter<V>) JSONResponseBodyConverter.Array.INSTANCE;
        }
        return null;
    }
    
    public static boolean isJSONSupportAvailable() {
        return isClassAvailable("org.json.JSONObject");
    }

    private static boolean isJSONObjectType(Class<?> type) {
        return "org.json.JSONObject".equals(type.getCanonicalName());
    }

    private static boolean isJSONArrayType(Class<?> type) {
        return "org.json.JSONArray".equals(type.getCanonicalName());
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
