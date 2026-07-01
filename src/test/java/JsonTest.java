
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResult;
import juno.http.HttpTask;
import juno.http.RequestBody;
import org.json.JSONObject;

public class JsonTest {

    HttpClient client = HttpClient.getInstance();

    public HttpTask<JSONObject> insert(
            String name, int age, boolean active) {

        // application-www-www-form-urlencoded
        FormBody reqBody = new FormBody()
                .add("name", name)
                .add("age", age)
                .add("active", active);

        HttpRequest request = HttpRequest.post(
                "https://postman-echo.com/post", reqBody);

        return client.newTask(request, JSONObject.class);
    }

    public void async() {
        HttpTask<JSONObject> insert = insert(
            "John Doe", 22, true);

        try {
            HttpResult<JSONObject> result = insert.sync();
            System.out.println(result.data.toString(1));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HttpResult<JSONObject> jsonRequest() throws Exception {
        JSONObject data = new JSONObject();
        data.put("user_id", 7);
        data.put("name", "jesus");

        RequestBody reqBody = RequestBody.create(
                "application/json", data.toString());
//        RequestBody reqBody = client.createRequestBody(data);

        HttpRequest request = HttpRequest.post(
                "https://postman-echo.com/post", reqBody);

        return client.execute(request, JSONObject.class);
    }

    public static void main(String[] args) throws Exception {
        JsonTest test = new JsonTest();
        //test.async();
        System.out.println(test.jsonRequest().data.toString(1));
    }
}
