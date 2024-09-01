
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

public class AuthTest implements JwtTokenProvider.OnTokenRefresh {

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

    @Override public void onTokenRefresh(TokenProvider provider) throws Exception {
        System.out.println("AuthTest.onTokenRefresh()");
        
        FormBody body = new FormBody()
            .add("token", provider.getRefreshToken())
        ;
        body
            .add("accessToken", generateRandomToken(0))
            .add("refreshToken", generateRandomToken(2000))
        ;
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", body);

        // Result
        JSONObject response = request.execute(JSONObject.class);
        JSONObject form = response.getJSONObject("form");
        System.out.println(form.toString(1));

        provider.setAccessToken(form.getString("accessToken"));
        provider.setRefreshToken(form.getString("refreshToken"));
    }

    private HttpResponse requestWithToken() throws Exception {
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
        ;
        body
            .add("accessToken", generateRandomToken(0))
            .add("refreshToken", generateRandomToken(2000))
        ;
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post", body);

        // Result
        JSONObject response = request.execute(JSONObject.class);
        JSONObject form = response.getJSONObject("form");
        System.out.println(form.toString(1));

        tokenProvider.setAccessToken(form.getString("accessToken"));
        tokenProvider.setRefreshToken(form.getString("refreshToken"));
    }
    
    public static String generateRandomToken(long millis) throws Exception {
        // Crear el header y payload
        JSONObject header = new JSONObject();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        JSONObject payload = new JSONObject();
        payload.put("sub", UUID.randomUUID());
        payload.put("name", "John Doe");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() / 1000) + 3600 + millis);

        // Generar el token
        String secretKey = Long.toHexString(System.currentTimeMillis());
        return JwtToken.generateToken(header, payload, secretKey);
    }

    public static void main(String[] args) throws Exception {
        final AuthTest test = new AuthTest();
        test.login("john.doe@mail.com", "1234");
        HttpResponse response = test.requestWithToken();
        System.out.println(response.readString());
    }
}