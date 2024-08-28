
import java.util.UUID;
import juno.content.DataStorage;
import juno.content.MemoryDataStorage;
import juno.http.FormBody;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.auth.TokenAuthorization;
import juno.http.auth.JwtToken;
import juno.http.auth.JwtTokenProvider;
import juno.http.auth.TokenProvider;
import org.json.JSONObject;

public class AuthTest implements JwtTokenProvider.OnAuth {

    HttpClient client;
    TokenProvider tokenProvider;

    public AuthTest() {
        // You can create you own storage
        DataStorage tokenStorage = new MemoryDataStorage();
        tokenProvider = new JwtTokenProvider(tokenStorage, this);

        // Add the JWT Manager to interceptor
        client = new HttpClient()
                .setAuthorization(new TokenAuthorization("Bearer ", tokenProvider))
                .setDebug(true);
    }

    @Override public JwtToken auth(JwtToken oldToken) throws Exception {
        System.out.println("AuthTest.auth()");
        return new JwtToken(generateRandomToken());
    }

    private HttpResponse request() throws Exception {
        System.out.println("AuthTest.request()");
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post");

        return client.execute(request);
    }
    
    void login(String email, String password) throws Exception {
        System.out.println("AuthTest.login()");
        
        FormBody body = new FormBody()
            .add("email", email)
            .add("password", password)
            .add("accessToken", generateRandomToken())
        ;
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", body);

        // Result
        JSONObject response = request.execute(JSONObject.class);
        //System.out.println(response.toString(1));
        
        String accessToken = response.getJSONObject("form")
                .getString("accessToken");

        tokenProvider.setAccessToken(new JwtToken(accessToken));
    }
    
    public static String generateRandomToken() throws Exception {
        // Crear el header y payload
        JSONObject header = new JSONObject();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        JSONObject payload = new JSONObject();
        payload.put("sub", UUID.randomUUID());
        payload.put("name", "John Doe");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() / 1000) + 3600);

        // Generar el token
        String secretKey = Long.toHexString(System.currentTimeMillis());
        return JwtToken.generateToken(header, payload, secretKey);
    }

    public static void main(String[] args) throws Exception {
        final AuthTest test = new AuthTest();
//        HttpResponse response = test.request();
//        System.out.println(response.readString());

        test.login("john.doe@mail.com", "1234");
        HttpResponse response = test.request();
        System.out.println(response.readString());
    }
}
