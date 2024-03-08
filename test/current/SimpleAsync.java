package current;

import juno.concurrent.Async;
import juno.concurrent.Callback;
import juno.http.HttpClient;
import juno.http.HttpRequest;

public class SimpleAsync {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.getInstance()
                .setDebug(true);
        
        HttpRequest request = new HttpRequest(
                "GET", "http://127.0.0.1/test.php");
        
        Async<String> call = client.newAsyncRequest(request, String.class);
        
        call.execute(new Callback<String>() {
            @Override
            public void onResponse(String response) throws Exception {
              System.out.println(response);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
