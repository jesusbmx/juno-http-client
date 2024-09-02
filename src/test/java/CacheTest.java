
import java.io.File;
import java.util.Calendar;
import juno.concurrent.Async;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpUrl;
import juno.http.cache.CacheInterceptor;

public class CacheTest {
    
    final HttpClient client = HttpClient.getInstance()
            .setDebug(true);
    
    final File cacheStorage = new File(
            System.getProperty("user.home") + "\\Downloads\\cache\\HttpCacheInterceptor.xml");

    /*
    GET https://postman-echo.com/get HTTP/1.1
    */
    Async<String> get() throws Exception {
        final HttpRequest request = new HttpRequest(
                "GET", "https://postman-echo.com/get");

        final Calendar nextExpireAt = Calendar.getInstance();
        // Sumar un día
        nextExpireAt.add(Calendar.DAY_OF_YEAR, 1);

        return client.createAsync(request, String.class)
                .setInterceptor(new CacheInterceptor(cacheStorage, nextExpireAt));
    }
    
    /*
    GET http://ip-api.com/json/24.48.0.1?fields=status%2Cmessage%2Cquery%2Ccountry%2Ccity&lang=en HTTP/1.1
    */
    Async<String> get2() throws Exception {
      final HttpUrl url = new HttpUrl("http://ip-api.com/")
        .addPath("json")
        .addPath("24.48.0.1")
        .addQueryParameter("fields", "status,message,query,country,city")
        .addQueryParameter("lang", "en")
      ;
      final HttpRequest request = new HttpRequest("GET", url);
      
      return client.createAsync(request, String.class)
              .setInterceptor(new CacheInterceptor(cacheStorage));
    }
    
    public static void main(String[] args) throws Exception {
        CacheTest cacheTest = new CacheTest();
        System.out.println(cacheTest.get().await());
        System.out.println(cacheTest.get2().await());
    }
}
