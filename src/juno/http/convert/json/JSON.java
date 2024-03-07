package juno.http.convert.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON {

  public static JSON of(String json) throws JSONException {
    if (json == null) 
      throw new JSONException("json == null");

    if (json.isEmpty()) 
      throw new JSONException("json is empty");

    int c = json.charAt(0);
    switch (c) {
      case '{':
        return new JSON(newJSONObject(json), null);

      case '[':
        return new JSON(null, newJSONArray(json));

      default:
        throw new JSONException("invalidate: " + json);
    }
  }
  
  private final JSONObject object;
  private final JSONArray array;
  private final boolean isObject;
  private final boolean isArray;

  JSON(JSONObject object, JSONArray array) {
    this.object = object;
    this.array = array;
    this.isObject = object != null;
    this.isArray = array != null;
  }

  public JSONObject getObject() {
    return object;
  }

  public JSONObject optObject(JSONObject defaultVal) {
    return isObject() ? getObject() : defaultVal;
  }
  
  public JSONObject optObject() {
    return isObject() ? getObject() : new JSONObject();
  }

  public JSONArray getArray() {
    return array;
  }

  public JSONArray optArray(JSONArray defaultVal) {
    return isArray() ? getArray() : defaultVal;
  }
  
  public JSONArray optArray() {
    return isArray() ? getArray() : new JSONArray();
  }

  public boolean isObject() {
    return isObject;
  }

  public boolean isArray() {
    return isArray;
  }

  public String toString(int indentFactor) {
    try {
        if (isArray()) 
            return array.toString(indentFactor);
        if (isObject()) 
            return object.toString(indentFactor);
        
        return null;
        
    } catch(Exception e) {
        return toString();
    }
  }

  @Override public String toString() {
    if (isArray()) 
        return array.toString();
    if (isObject()) 
        return object.toString();
    
    return null;
  }
  
  public static Object toJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), toJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable)object)) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws JSONException {
        return toMap(object.getJSONObject(key));
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
    
    public static JSONObject newJSONObject(String json) throws JSONException {
        try {
            return new JSONObject(json);
        } catch(JSONException e) {
            throw new JSONException(json);
        }
    }
    
    public static JSONArray newJSONArray(String json) throws JSONException {
        try {
            return new JSONArray(json);
        } catch(JSONException e) {
            throw new JSONException(json);
        }
    }
}