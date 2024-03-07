package juno.http.cache;

import juno.http.AsyncRequest;
import juno.http.HttpRequest;
import juno.http.ResponseBody;
import juno.http.convert.ResponseBodyConvert;
import java.io.File;
import java.util.Calendar;
import java.util.UUID;
import juno.http.HttpClient;

public class CacheInterceptor<V> implements AsyncRequest.OnInterceptor<V> {
    
    public final String name;
    public final File dirStorage;
    private CacheSource _cacheSource;
    public final Calendar nextExpireAt;

    public CacheInterceptor(String name, File dirStorage, Calendar nextExpireAt) {
        this.name = name;
        this.dirStorage = dirStorage;
        this.nextExpireAt = nextExpireAt;
    }
    
    public CacheInterceptor(File dirStorage, Calendar nextExpireAt) {
        this("HttpCacheInterceptor.json", dirStorage, nextExpireAt);
    }
    
    public File getDirStorage() {
        if (!dirStorage.exists()) {
            dirStorage.mkdirs();
        }
        return dirStorage;
    }
    
    public CacheSource getCacheSource() {
        if (_cacheSource == null) {
            _cacheSource = CacheSource.get(new File(getDirStorage(), name));
        }
        return _cacheSource;
    }
   
    @Override
    public V intercept(HttpClient client, HttpRequest request, ResponseBodyConvert<V> convert) throws Exception {
        ResponseBody body = getResponseBody(client, request);
        return convert.parse(body);
    }
    
    public ResponseBody getResponseBody(HttpClient client, HttpRequest request) throws Exception {
        final CacheModel cache = getCacheSource().find(request);
        if (cache == null) {
            return executeRequest(client, request, null);
        }
        
        final long now = System.currentTimeMillis();

        // Expiro la cache
        if (now > cache.expireAt) {
            System.err.println("expire '" + cache.request()+ "'");
            return executeRequest(client, request, cache);
        }
        
        // Obtiene la ultima respuesta desde la cache
        try {
            System.out.println("get cache '" + cache.request() + "'");
            return cache.getResponseBody(); 
             
        } catch(Exception e) {
            return executeRequest(client, request, cache);
        }
    }
    
    public ResponseBody executeRequest(HttpClient client, HttpRequest request, CacheModel cache) throws Exception {
        if (cache == null)  {
            cache = new CacheModel();
            cache.uuid = UUID.randomUUID().toString();
        }
        
        // Executa la solicituda el el servidor
        final ResponseBody response = client.execute(request, ResponseBody.class);

        if (response.code == 200) {
            cache.expireAt = getNextExpireAt().getTimeInMillis();
            cache.setRequest(request);
                        
            final File tmpData = new File(getDirStorage(), cache.uuid + ".data");
            cache.setResponse(response, tmpData);

            // Guarda los datos de la respuesta en la cache
            getCacheSource().save(cache);
        }

        return response;
    }

    public Calendar getNextExpireAt() {
        return nextExpireAt;
    }
    
//    static String stringToHex(String string) {
//        StringBuilder buf = new StringBuilder(200);
//        for (char ch: string.toCharArray()) {
//          if (buf.length() > 0)
//            buf.append(' ');
//          buf.append(String.format("%04x", (int) ch));
//        }
//        return buf.toString();
//    }
}
