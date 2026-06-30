
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
        HttpClient.getInstance()
                //.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")
                .setDebug(true);
        
        HttpRequest request = HttpRequest.get(
            "https://postman-echo.com/get")
        ;

        HttpResult<JSONObject> result = request.sendResult(JSONObject.class);
        
        if (result.isSuccessful()) {
            System.out.println("OK:" + result.body.toString(1));
        } else {
            System.err.println("ERR: " + result.code);
        }
    }
}
