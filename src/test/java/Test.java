
import juno.concurrent.Callback;
import juno.http.HttpClient;
import juno.http.HttpException;
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
                .setDebug(true)
        ;

        HttpRequest request = HttpRequest.get(
            "https://postman-echo.com/get")
        ;

        // axios: lanza HttpException si la respuesta no es 2xx, si no devuelve el HttpResult
        try {
          HttpResult<JSONObject> result = request.execute(JSONObject.class);
          System.out.println("Sync OK: " + result.data.toString(1));

        } catch(HttpException err) {
            System.out.println("Sync Err: " + err);
        }

        // err.data: siempre crudo (igual que Retrofit), sin intentar convertirlo.
        // Si sabes que el backend devuelve JSON en el error, parséalo tú mismo.
        HttpRequest notFound = HttpRequest.get("https://postman-echo.com/status/404");
        try {
          notFound.execute(JSONObject.class);

        } catch(HttpException err) {
            System.out.println("Error body (raw): " + err.data);
            try {
                JSONObject errJson = new JSONObject(err.data);
                System.out.println("Error body (parsed manually): " + errJson.toString(1));
            } catch (Exception notJson) {
                // el body no era JSON (HTML, texto, etc.) - err.data sigue disponible como texto
            }
        }

        // async: también lanza HttpException hacia onFailure si la respuesta no es 2xx
        client.newTask(request, JSONObject.class)
                .async(new Callback<HttpResult<JSONObject>>() {
                    @Override
                    public void onResponse(HttpResult<JSONObject> result) throws Exception {
                        System.out.println("Async OK: " + result.data.toString(1));
                    }
                    @Override
                    public void onFailure(Exception err) {
                        System.out.println("Async Err: " + err);
                    }
                });

        // newDataTask: en vez de HttpResult<JSONObject>, resuelve JSONObject directo
        client.newDataTask(request, JSONObject.class)
                .async(new Callback<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject body) throws Exception {
                        System.out.println("DataTask OK: " + body.toString(1));
                    }
                    @Override
                    public void onFailure(Exception err) {
                        System.out.println("DataTask Err: " + err);
                    }
                });
    }
}
