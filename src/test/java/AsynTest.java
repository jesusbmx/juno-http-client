
import juno.concurrent.Callback;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResult;
import juno.http.HttpTask;
import juno.http.HttpUrl;

public class AsynTest {

    HttpClient client = HttpClient.getInstance()
              .setDebug(true)
    ;

    public HttpTask<String> getIpLocation() {
      HttpUrl url = new HttpUrl("http://ip-api.com/")
                .addPath("json")
                .addPath("24.48.0.1")
                .addQueryParameter("fields", "status,message,query,country,city")
                .addQueryParameter("lang", "en")
      ;
      HttpRequest request = HttpRequest.get(url)
      ;
      return client.newTask(request, String.class);
    }

    public void async() {
        HttpTask<String> task = getIpLocation();

        task.async(new Callback<HttpResult<String>>() {
            @Override
            public void onResponse(HttpResult<String> result) throws Exception {
                String str = result.data;
                System.out.println(str);
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
   }

  public void sync() throws Exception {
    HttpResult<String> result = getIpLocation().sync();
    String str = result.data;
    System.out.println(str);
  }

  public static void main(String[] args) throws Exception {
    AsynTest test = new AsynTest();
    //test.async();
    test.sync();
  }
}
