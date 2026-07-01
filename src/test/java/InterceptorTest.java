
import juno.concurrent.Task;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpResult;
import juno.http.HttpUrl;
import juno.http.OnInterceptor;
import juno.http.HttpTransport;


public class InterceptorTest {

    HttpClient client = HttpClient.getInstance()
            .setInterceptor(new OnInterceptor() {
                @Override
                public HttpResponse intercept(HttpRequest request, HttpTransport stack) throws Exception {
                    HttpResponse response = stack.send(request);
                    if (response.code >= 200 && response.code <= 299) {
                        return response;
                    }
                    throw new Exception("Unknown error code: " + response.code);
                }
             })
            .setDebug(true)
    ;

    Task<HttpResult<String>> getIpLocation() {
      HttpUrl url = new HttpUrl("http://ip-api.com/")
        .addPath("json")
        .addPath("24.48.0.1")
        .addQueryParameter("fields", "status,message,query,country,city")
        .addQueryParameter("lang", "en")
      ;
      HttpRequest request = HttpRequest.get(url);

      return client.newTask(request, String.class);
    }

    public static void main(String[] args) throws Exception {
        InterceptorTest test = new InterceptorTest();

        final HttpResult<String> result = test.getIpLocation().sync();
        System.out.println(result.data);
    }
}
