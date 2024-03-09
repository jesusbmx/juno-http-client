
import juno.concurrent.Async;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.RequestBody;
import org.json.JSONObject;

public class JsonTest {

    HttpClient client = HttpClient.getInstance();

    public Async<JSONObject> insert(
            String name, int age, boolean active) {

        // application-www-www-form-urlencoded
        FormBody reqBody = new FormBody()
                .add("name", name)
                .add("age", age)
                .add("active", active);

        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", reqBody);

        return client.newAsyncRequest(request, JSONObject.class);
    }

    public void async() {
        Async<JSONObject> insert = insert(
            "John Doe", 22, true);

        try {
            JSONObject response = insert.await();
            System.out.println(response.toString(1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    String jsonRequest() throws Exception {
        JSONObject data = new JSONObject();
        data.put("user_id", 7);
        data.put("name", "jesus");

        RequestBody reqBody = RequestBody.create(
                "application/json", data.toString());

        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", reqBody);

        return client.execute(request, String.class);
    }

    public static void main(String[] args) throws Exception {
        JsonTest test = new JsonTest();
        //test.async();
        test.jsonRequest();
    }
}
