
import juno.concurrent.Async;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpStack;
import juno.http.HttpUrl;
import juno.http.OnInterceptor;


public class InterceptorTest {
    
    HttpClient client = HttpClient.getInstance()
            .setInterceptor(new OnInterceptor() {
                @Override
                public HttpResponse intercept(HttpRequest request, HttpStack stack) throws Exception {
                    HttpResponse response = stack.execute(request);
                    if (response.code >= 200 && response.code <= 299) {
                        return response;
                    }
                    throw new Exception("Unknown error code: " + response.code);
                }
             })
            .setDebug(true)
    ;

    Async<String> getIpLocation() { 
      HttpUrl url = new HttpUrl("http://ip-api.com/")
        .addPath("json")
        .addPath("24.48.0.1")
        .addQueryParameter("fields", "status,message,query,country,city")
        .addQueryParameter("lang", "en")
      ;
      HttpRequest request = new HttpRequest("GET", url);
      
      return client.createAsync(request, String.class);
    }
    
    public static void main(String[] args) throws Exception {
        InterceptorTest test = new InterceptorTest();
 
        final String response = test.getIpLocation().await();
        System.out.println(response);
    }
}
