package juno.http.cache;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;
import juno.http.Debug;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpStack;
import juno.http.OnInterceptor;

public class CacheInterceptor implements OnInterceptor {
    
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
    public HttpResponse intercept(HttpRequest request, HttpStack stack) throws Exception {
        final CacheModel cache = getCacheSource().find(request);
        if (cache == null) {
            Debug.debug("CacheInterceptor", "executeRequest", request);
            return executeRequest(stack, request, null);
        }
        
        final long now = System.currentTimeMillis();
        Debug.debug("CacheInterceptor", "cache", cache);

        // Expiro la cache
        if (now > cache.expireAt) {
            Debug.debug("CacheInterceptor", "expire", cache.request());
            return executeRequest(stack, request, cache);
        }
        
        // Obtiene la ultima respuesta desde la cache
        try {
            Debug.debug("CacheInterceptor", "get cache", cache.request());
            return cache.getResponseBody(); 
             
        } catch(Exception e) {
            Debug.debug("CacheInterceptor", "error getting cache", cache.request());
            return executeRequest(stack, request, cache);
        }
    }
    
    public HttpResponse executeRequest(HttpStack stack, HttpRequest request, CacheModel cache) throws Exception {
        if (cache == null)  {
            cache = new CacheModel();
            cache.uuid = UUID.randomUUID().toString();
        }
        
        // Executa la solicituda el el servidor
        final HttpResponse response = stack.execute(request);

        if (response.code == 200) {
            cache.expireAt = getNextExpireAt().getTimeInMillis();
            cache.setRequest(request);
                        
            final File tmpData = new File(getDirStorage(), cache.uuid + ".data");
            cache.write(response, tmpData);

            // Guarda los datos de la respuesta en la cache
            getCacheSource().save(cache);
            
            return cache.getResponseBody();
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
