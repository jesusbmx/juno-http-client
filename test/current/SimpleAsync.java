package current;

import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.Response;

public class SimpleAsync {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.getInstance()
                .setDebug(true);
        
        HttpRequest request = new HttpRequest(
                "GET", "http://127.0.0.1/test.php");
        
        Async<Response<String>> call = client.newAsyncRequest(request);
        
        call.execute(new Callback<Response<String>>() {
            @Override
            public void onResponse(Response<String> response) throws Exception {
              System.out.println(response.result);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
