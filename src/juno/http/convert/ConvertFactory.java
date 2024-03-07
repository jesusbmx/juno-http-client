package juno.http.convert;

import juno.http.FormBody;
import juno.http.MultipartBody;
import juno.http.RequestBody;
import juno.http.ResponseBody;
import juno.http.convert.json.JSON;
import juno.http.convert.json.JSONRequestBodyConvert;
import juno.http.convert.json.JSONResponseBodyConvert;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class ConvertFactory {
 
    /** Cache de adaptadores para las peticiones. */
    protected final Map<Class<?>, RequestBodyConvert> requestBodyConverts = 
            new HashMap<Class<?>, RequestBodyConvert>();   
    
    /** Cache de adaptadores para las respuestas. */
    protected final Map<Class<?>, ResponseBodyConvert> responseBodyConverts = 
            new HashMap<Class<?>, ResponseBodyConvert>();
    
    public ConvertFactory() {
        
    }
    
    ////////////////////////////////////////////////////////////////////////////

    public <V> RequestBodyConvert<V> getRequestBodyConvert(Class<V> classOf) {
      RequestBodyConvert<V> convert = requestBodyConverts.get(classOf);
      if (convert == null) {
        convert = createaAuxResponseBodyConverter(classOf);        
        setRequestBodyConvert(classOf, convert);
      }
      return convert;
    }
    
    public <V> ConvertFactory setRequestBodyConvert(Class<V> classOf, RequestBodyConvert<V> adapter) {
        requestBodyConverts.put(classOf, adapter);
        return this;
    }
    
    public <V> RequestBodyConvert<V> createaAuxResponseBodyConverter(Class<V> classOf) {
      final String name = classOf.getCanonicalName();
      
      if (name.equals("org.json.JSONObject")) 
          return (RequestBodyConvert<V>) new JSONRequestBodyConvert.Obj();
      if (name.equals("org.json.JSONArray")) 
          return (RequestBodyConvert<V>) new JSONResponseBodyConvert.Array();
      if (classOf == JSON.class) 
          return (RequestBodyConvert<V>) new JSONRequestBodyConvert();
      
      return createRequestBodyConvert(classOf);
    }
    
    public abstract <V> RequestBodyConvert<V> createRequestBodyConvert(Class<V> classOf);
    
    ////////////////////////////////////////////////////////////////////////////
    
    public <V> ResponseBodyConvert<V> getResponseBodyConvert(Class<V> classOf) {
      ResponseBodyConvert<V> convert = responseBodyConverts.get(classOf);
      if (convert == null) {
        convert = createAuxResponseBodyConvert(classOf);        
        setResponseBodyConvert(classOf, convert);
      }
      return convert;
    }
    
    public <V> ConvertFactory setResponseBodyConvert(Class<V> classOf, ResponseBodyConvert<V> convert) {
      responseBodyConverts.put(classOf, convert);
      return this;
    }
        
    public <V> ResponseBodyConvert<V> createAuxResponseBodyConvert(Class<V> classOf) {
      final String name = classOf.getCanonicalName();

      if (classOf == String.class) 
          return (ResponseBodyConvert<V>) new StringResponseBodyConvert();
      if (name.equals("org.json.JSONObject")) 
          return (ResponseBodyConvert<V>) new JSONResponseBodyConvert.Obj();
      if (name.equals("org.json.JSONArray")) 
          return (ResponseBodyConvert<V>) new JSONResponseBodyConvert.Array();
      if (classOf == JSON.class) 
          return (ResponseBodyConvert<V>) new JSONResponseBodyConvert();
      if (classOf == File.class)
          return (ResponseBodyConvert<V>) new FileResponseBodyConvert();
      if (classOf == ResponseBody.class || classOf == BytesResponseBody.class)
          return (ResponseBodyConvert<V>) new BytesResponseBodyConvert();
      
      return createResponseBodyConvert(classOf);
    }
    
    public abstract <V> ResponseBodyConvert<V> createResponseBodyConvert(Class<V> classOf);
    
    ////////////////////////////////////////////////////////////////////////////

    public <V> RequestBody createRequestBody(V src) {
        if (src == null) return null;
        if (src instanceof RequestBody) return (RequestBody) src;
        
        try {
          final Class<V> classOf = (Class<V>) src.getClass();
          final RequestBodyConvert<V> adapter = getRequestBodyConvert(classOf);
          return adapter.parse(src);
          
        } catch(Exception e) {
          throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <V> FormBody createFormBody(V src) {
        if (src == null) return null;
        if (src instanceof FormBody) return (FormBody) src;
        return new FormBody(convertToMap(src));
    }
    
    public <V> MultipartBody createMultipartBody(V src) {
        if (src == null) return null;
        if (src instanceof MultipartBody) return (MultipartBody) src;
        return new MultipartBody(convertToMap(src));
    }

    public <V> Map<String, Object> convertToMap(V src) {
        if (src instanceof Map) {
            Map map = (Map) src;
            Iterator itkeys = map.keySet().iterator();
            Map<String, Object> r = new HashMap<String, Object>(map.size());
            while (itkeys.hasNext()) {
                Object key = itkeys.next();
                r.put(String.valueOf(key), map.get(key));
            }
            return r;
        }
        return convertObjectToMap(src);
     }
    
    public abstract <V> Map<String, Object> convertObjectToMap(V src);
}
