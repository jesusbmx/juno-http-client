package current;

import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.ResponseBody;
import juno.concurrent.Async;
import juno.concurrent.Callback;

public class SimpleAsync {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.getInstance()
                .setDebug(true);
        
        HttpRequest request = new HttpRequest(
                "GET", "http://127.0.0.1/test.php");
        
        Async<ResponseBody> call = client.newAsyncRequest(request);
        
        call.execute(new Callback<ResponseBody>() {
            @Override
            public void onResponse(ResponseBody result) throws Exception {
              String str = result.string();
              System.out.println(str);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
