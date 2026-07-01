
import juno.concurrent.OnError;
import juno.concurrent.OnResponse;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResult;
import org.json.JSONObject;

/**
 *
 * @author Jesus
 */
public class Test {

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.getInstance()
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")
                //.setDebug(true)
        ;

        HttpRequest request = HttpRequest.get(
            "https://postman-echo.com/get")
        ;

        // axios: lanza HttpException si la respuesta no es 2xx
        try {
          JSONObject body = request.send(JSONObject.class);
          System.out.println("Axios OK: " + body.toString(1));

        } catch(Exception err) {
            System.out.println("Axios Err: " + err);
        }

        // fetch: nunca lanza por status HTTP, revisa result.isSuccessful()/result.error
        HttpResult<JSONObject> result = request.fetch(JSONObject.class);
        if (result.isSuccessful()) {
            System.out.println("Fetch OK: " + result.body.toString(1));
        } else {
            System.err.println("Fetch ERR: " + result.error);
        }
        
        client.newTask(request, JSONObject.class).async(new OnResponse<JSONObject>() {
            @Override
            public void onResponse(JSONObject body) throws Exception {
                System.out.println("Axios Async OK: " + body.toString(1));
            }
        }, new OnError() {
            @Override
            public void onFailure(Exception err) {
                System.out.println("Axios Async Err: " + err);
            }
        });
        
        client.newFetchTask(request, JSONObject.class).async(new OnResponse<HttpResult<JSONObject>>() {
            @Override
            public void onResponse(HttpResult<JSONObject> result) throws Exception {
                if (result.isSuccessful()) {
                    System.out.println("Fetch Async OK: " + result.body.toString(1));
                } else  {
                    System.err.println("Fetch Async ERR: " + result.error);
                }
            }
        }, new OnError() {
            @Override
            public void onFailure(Exception err) {
                System.out.println("Fetch Async Err: " + err);
            }
        });
    }
}
