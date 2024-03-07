package juno.http.cache;

import juno.http.HttpRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import juno.io.Files;
import org.json.JSONArray;

public class CacheSource {
    private static final Map<File, CacheSource> INSTANCES = 
            new HashMap<File, CacheSource>();
    
    private final File _src;
    private List<CacheModel> _objects;

    private CacheSource(File src) {
        this._src = src;
    }
    
    public static CacheSource get(File src) {
        CacheSource db = INSTANCES.get(src);
        if (db == null) {
            db = new CacheSource(src);
            INSTANCES.put(src, db);
        }
        return db;
    }
    
    public int indexOf(String uuid, List<CacheModel> list) {
        for (int i = 0; i < list.size(); i++) 
            if (Objects.equals(list.get(i).uuid, uuid)) 
                return i;

        return -1;
    }
    
    public synchronized void writeObjects(List<CacheModel> list) { 
        try {
            JSONArray array = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                array.put(list.get(i).toJson());
            }
            final String json = array.toString(1);
            Files.write(_src, json);
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public synchronized List<CacheModel> readObjects() { 
        try {
            if (!_src.exists()) {
                return new ArrayList<CacheModel>();
            }
            
            final String json = Files.readString(_src);
            final JSONArray array = new JSONArray(json);
            ArrayList<CacheModel> result = new ArrayList<CacheModel>(
                    array.length());
            
            for (int i = 0; i < array.length(); i++) {
                result.add(new CacheModel(array.getJSONObject(i)));
            }

            return result;

        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<CacheModel>();            
        }
    }
    
    public List<CacheModel> getObjects() {
        if (_objects == null) {
            _objects = readObjects();
        }
        return _objects;
    }
    
    public CacheModel find(final String method, final String url) {
        List<CacheModel> list = getObjects();
        for (int i = 0; i < list.size(); i++) {
            final CacheModel it = list.get(i);
            if (method.equals(it.requestMethod) 
                    && url.equals(it.requestUrl)) {
                return it;
            }
        }
        return null;
    }
    
    public CacheModel find(HttpRequest request) {
        return find(request.getMethod(), request.urlAndParams());
    }

    public int save(CacheModel model) {
        List<CacheModel> list = getObjects();
        int i = indexOf(model.uuid, list);

        if (i == -1) { // Insert
            list.add(model);
            writeObjects(list);
            return 1;
        }

        list.set(i, model); // Update
        writeObjects(list);
        return 2;
    } 
}
