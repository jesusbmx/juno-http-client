package auth;

import juno.http.HttpClient;
import juno.http.HttpRequest;
import juno.http.HttpResponse;
import juno.http.HttpUrl;
import juno.http.auth.AuthorizationToken;
import juno.http.auth.DataStorage;
import juno.http.auth.DataStorageMap;
import juno.http.auth.JWTManager;
import juno.http.auth.Token;

public class AuthTest implements Token.OnAuth {
    
    HttpClient client;

    public AuthTest() {
        // You can create you own storage
        DataStorage tokenStorage = new DataStorageMap();
        JWTManager tokenManager = new JWTManager(tokenStorage, this);

        // Add the JWT Manager to interceptor
        client = new HttpClient()
                .setAuthorization(new AuthorizationToken("Bearer", tokenManager))
                .setDebug(true);
    }

    @Override
    public Token auth() throws Exception {
        return new MyToken();
    }
    
    private HttpResponse request() throws Exception{
        HttpUrl url = new HttpUrl("http://ip-api.com/")
                .addPath("json")
                .addPath("24.48.0.1")
                .addQueryParameter("fields", "status,message,query,country,city")
                .addQueryParameter("lang", "en")
      ;
      HttpRequest request = new HttpRequest("GET", url);

      return client.execute(request, HttpResponse.class);
    }
    
    
    public static void main(String[] args) throws Exception {
        HttpResponse response = new AuthTest().request();
        System.out.println(response);
        System.out.println(response.readString());
    }
    
    public static class MyToken implements Token {

        @Override
        public String getToken() {
            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        }

        @Override
        public boolean isValid() {
            return true;
        }
        
    }
}
