

import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpUrl;
import juno.http.ResponseBody;

public class AsynTest {
    
    HttpClient client = HttpClient.getInstance()
              .setDebug(true)
    ;

    public Async<ResponseBody> getIpLocation() {
      HttpUrl url = new HttpUrl("http://ip-api.com/{returnType}/{ip}")
              .setPath("returnType", "json")
              .setPath("ip", "24.48.0.1")
      ;
      FormBody body = new FormBody()
          .add("fields", "status,message,query,country,city")
          .add("lang", "en")
      ;
      HttpRequest request = new HttpRequest(
          "GET", url, body)
      ;
      return client.newAsyncRequest(request, ResponseBody.class);
    }

    public void async() {
        Async<ResponseBody> async = getIpLocation();

        async.execute(new Callback<ResponseBody>() {
            @Override
            public void onResponse(ResponseBody response) throws Exception {
                String result = response.readString();
                System.out.println(result);
            }
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
   }
  
  public void sync() {
    Async<ResponseBody> ipLocation = getIpLocation();
    
    try (ResponseBody response = ipLocation.await()) {
      String result = response.readString();
      System.out.println(result);
    
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    AsynTest test = new AsynTest();
    //test.async();
    test.sync();
  }
}
