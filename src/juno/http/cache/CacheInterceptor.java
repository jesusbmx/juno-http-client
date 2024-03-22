package juno.http.cache;

import java.io.File;
import java.util.Calendar;
import juno.http.Debug;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpStack;
import juno.http.OnInterceptor;

public class CacheInterceptor implements OnInterceptor {

    public final CacheSource source;
    public final Calendar nextExpireAt;

    public CacheInterceptor(CacheSource source, Calendar nextExpireAt) {
        this.source = source;
        this.nextExpireAt = nextExpireAt;
    }

    public CacheInterceptor(File source, Calendar nextExpireAt) {
        this(CacheSource.getCacheSourceFromFile(source), nextExpireAt);
    }

    public CacheInterceptor(CacheSource source) {
        this(source, defaultNextExpireAt());
    }

    public CacheInterceptor(File source) {
        this(CacheSource.getCacheSourceFromFile(source));
    }

    private static Calendar defaultNextExpireAt() {
        Calendar nextExpireAt = Calendar.getInstance();
        nextExpireAt.add(Calendar.DAY_OF_YEAR, 1);
        return nextExpireAt;
    }

    @Override
    public HttpResponse intercept(HttpRequest request, HttpStack stack) throws Exception {
        CacheModel cache = source.find(request);

        if (cache == null || hasCacheExpired(cache)) {
            return handleCacheMiss(request, stack, cache);
        }

        return handleCacheHit(request, cache, stack);
    }

    protected HttpResponse handleCacheMiss(HttpRequest request, HttpStack stack, CacheModel cache) throws Exception {
        Debug.debug("CacheInterceptor", "executeRequest:", request);
        final HttpResponse response = stack.execute(request);

        if (isResponseValid(response)) {
            try {
                cache = updateCache(cache, request, response);
                return cache.getHttpResponseFromFile();

            } catch (Exception e) {
                Debug.debug("CacheInterceptor", "error.writeResponseToFile:", e.getMessage());
            }
        }

        return response;
    }

    protected HttpResponse handleCacheHit(HttpRequest request, CacheModel cache, HttpStack stack) throws Exception {
        try {
            Debug.debug("CacheInterceptor", "getHttpResponseFromFile:", cache.getRequestAsString());
            return cache.getHttpResponseFromFile();

        } catch (Exception e) {
            Debug.debug("CacheInterceptor", "error.getHttpResponseFromFile:", e.getMessage());
        }

        return stack.execute(request);
    }

    protected CacheModel updateCache(CacheModel cache, HttpRequest request, HttpResponse response) throws Exception {
        if (cache == null) {
            cache = new CacheModel();
        }

        cache.expireAt = nextExpireAt.getTimeInMillis();
        cache.setHttpRequest(request);

        final File tmpContent = new File(source.getParentFile(), cache.uuid + ".content");
        Debug.debug("CacheInterceptor", "writeResponseToFile:", tmpContent);
        cache.writeResponseToFile(response, tmpContent);

        // Guarda los datos de la respuesta en la cache
        source.save(cache);

        return cache;
    }

    protected boolean isResponseValid(HttpResponse response) {
        return response.code == 200;
    }

    private boolean hasCacheExpired(CacheModel cache) {
        return System.currentTimeMillis() > cache.expireAt;
    }
}
