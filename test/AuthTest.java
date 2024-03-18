

import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.auth.AuthorizationToken;
import juno.content.DataStorage;
import juno.content.MemoryDataStorage;
import juno.http.auth.Token;
import juno.http.auth.TokenManager;

public class AuthTest implements Token.OnAuth {

    HttpClient client;

    public AuthTest() {
        // You can create you own storage
        DataStorage tokenStorage = new MemoryDataStorage();
        TokenManager tokenManager = new MyTokenManager(tokenStorage, this);

        // Add the JWT Manager to interceptor
        client = new HttpClient()
                .setAuthorization(new AuthorizationToken("Bearer", tokenManager))
                .setDebug(true);
    }

    @Override public Token auth() throws Exception {
        return new SimpleToken();
    }

    private HttpResponse request() throws Exception {
        HttpRequest request = new HttpRequest(
                "POST", "https://postman-echo.com/post");

        return client.execute(request, HttpResponse.class);
    }

    public static void main(String[] args) throws Exception {
        final AuthTest test = new AuthTest();
        for (int i = 0; i < 20; i++) {
            HttpResponse response = test.request();
            System.out.println(response.readString());
        }
    }

    public static class MyTokenManager implements TokenManager {
        private final DataStorage storage;
        private final Token.OnAuth onAuth;
        
        public MyTokenManager(DataStorage storage, Token.OnAuth onAuth) {
            this.storage = storage;
            this.onAuth = onAuth;
        }
        
        @Override public Token getAccessToken() throws Exception {
            Token accessToken = readToken();
            if (accessToken == null || !accessToken.isValid()) {
                accessToken = onAuth.auth();
                saveToken(accessToken);
            }
            return accessToken;
        }
        private Token readToken() throws Exception {
            String token = storage.getItem("accessToken", null);
            return token != null ? new SimpleToken(token) : null;
        }
        private void saveToken(Token token) throws Exception {
            storage.setItem("accessToken", token.getToken());
        }
    }

    public static class SimpleToken implements Token {
        public String token;
        
        public SimpleToken(String token) {
            this.token = token;
        }
        public SimpleToken() {
            this("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        }
        
        @Override public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        @Override public boolean isValid() {
            return true;
        }
    }
}
