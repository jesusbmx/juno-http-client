
import juno.content.DataStorage;
import juno.content.MemoryDataStorage;
import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.auth.AuthInterceptor;
import juno.http.auth.JwtToken;
import juno.http.auth.JwtTokenManager;
import juno.http.auth.Token;
import juno.http.auth.TokenManager;

public class AuthTest implements Token.OnAuth {

    HttpClient client;

    public AuthTest() {
        // You can create you own storage
        DataStorage tokenStorage = new MemoryDataStorage();
        TokenManager tokenManager = new JwtTokenManager(tokenStorage, this);

        // Add the JWT Manager to interceptor
        client = new HttpClient()
                .setAuthorization(new AuthInterceptor(tokenManager, "Authorization", "Bearer "))
                .setDebug(true);
    }

    @Override public Token auth(Token oldToken) throws Exception {
        return new JwtToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
    }

    private HttpResponse request() throws Exception {
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post");

        return client.execute(request);
    }

    public static void main(String[] args) throws Exception {
        final AuthTest test = new AuthTest();
        HttpResponse response = test.request();
        System.out.println(response.readString());
    }
}
