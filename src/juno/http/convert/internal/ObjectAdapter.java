
package juno.http.convert.internal;

import java.util.HashMap;
import java.util.Map;

public class ObjectAdapter<T> {

    /** Cache de adaptadores para los objectos. */
    private static final Map<Class, ObjectAdapter> objectAdapters = 
            new HashMap<Class, ObjectAdapter>();
    
    public final Class<T> classOf;
    public final FieldAdapter[] fields;

    public ObjectAdapter(Class<T> classOf) throws Exception {
        this.classOf = classOf;
        this.fields = FieldAdapter.fields(classOf);
    }
        
    public static <V> ObjectAdapter<V> get(Class<V> classOf) throws Exception {
        ObjectAdapter<V> adapter = objectAdapters.get(classOf);
        if (adapter == null) {
            adapter = new ObjectAdapter<V>(classOf);
            set(classOf, adapter);
        }
        return adapter;
    }
    
    public static <V> ObjectAdapter<V> set(Class<V> classOf, ObjectAdapter<V> adapter) {
        objectAdapters.put(classOf, adapter);
        return adapter;
    }
    
    public Map<String, Object> toMap(T src) throws Exception {
        if (src == null) return null;

        final Map<String, Object> result = new HashMap<String, Object>(fields.length);
        fill(src, result);
                    
        return result;
    }
    
    public <V> void fill(V src, Map<String, Object> dest) throws Exception {        
        for (int i = 0; i < fields.length; i++) {
            FieldAdapter field = fields[i];
            
            String key = field.getName();
            Object value = field.get(src);
            
            dest.put(key, value);
        }
    }
    
}