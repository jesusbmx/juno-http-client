package current;

import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.ResponseBody;
import juno.concurrent.Async;
import juno.concurrent.Callback;

public class SimpleAsync {

    public static void main(String[] args) throws Exception {
        HttpClient cli = HttpClient.getInstance()
                .setDebug(true);
        
        HttpRequest request = new HttpRequest(
                "GET", "http://127.0.0.1/test.php");
        
        Async<ResponseBody> call = cli.newAsyncRequest(request);
        
        call.execute(new Callback<ResponseBody>() {
            @Override
            public void onResponse(ResponseBody result) throws Exception {
              String str = result.as(String.class);
              System.out.println(str);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
