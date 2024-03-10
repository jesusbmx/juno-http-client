package juno.http.cache;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;
import juno.http.AsyncRequest;
import juno.http.HttpExecutor;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.convert.ResponseBodyConvert;

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
    public V intercept(HttpExecutor executor, HttpRequest request, ResponseBodyConvert<V> convert) throws Exception {
        final HttpResponse body = getResponseBody(executor, request);
        return convert.parse(body);
    }
    
    public HttpResponse getResponseBody(HttpExecutor executor, HttpRequest request) throws Exception {
        final CacheModel cache = getCacheSource().find(request);
        if (cache == null) {
            return executeRequest(executor, request, null);
        }
        
        final long now = System.currentTimeMillis();

        // Expiro la cache
        if (now > cache.expireAt) {
            System.err.println("expire '" + cache.request()+ "'");
            return executeRequest(executor, request, cache);
        }
        
        // Obtiene la ultima respuesta desde la cache
        try {
            System.out.println("get cache '" + cache.request() + "'");
            return cache.getResponseBody(); 
             
        } catch(Exception e) {
            return executeRequest(executor, request, cache);
        }
    }
    
    public HttpResponse executeRequest(HttpExecutor executor, HttpRequest request, CacheModel cache) throws Exception {
        if (cache == null)  {
            cache = new CacheModel();
            cache.uuid = UUID.randomUUID().toString();
        }
        
        // Executa la solicituda el el servidor
        final HttpResponse response = executor.execute(request);

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
