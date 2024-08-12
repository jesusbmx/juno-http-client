
import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpUrl;

public class AsynTest {
    
    HttpClient client = HttpClient.getInstance()
              .setDebug(true)
    ;

    public Async<HttpResponse> getIpLocation() {
      HttpUrl url = new HttpUrl("http://ip-api.com/")
                .addPath("json")
                .addPath("24.48.0.1")
      ;
      FormBody body = new FormBody()
          .add("fields", "status,message,query,country,city")
          .add("lang", "en")
      ;
      HttpRequest request = new HttpRequest(
          "GET", url, body)
      ;
      return client.createAsync(request);
    }

    public void async() {
        Async<HttpResponse> async = getIpLocation();

        async.execute(new Callback<HttpResponse>() {
            @Override
            public void onResponse(HttpResponse response) throws Exception {
                String result = response.readString();
                System.out.println(result);
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
   }
  
  public void sync() throws Exception {
    HttpResponse response = getIpLocation().await();
    String result = response.readString();
    System.out.println(result);
    response.close();
  }
  
  public static void main(String[] args) throws Exception {
    AsynTest test = new AsynTest();
    //test.async();
    test.sync();
  }
}
