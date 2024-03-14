package juno.http.auth;

import java.io.IOException;

public class JWTManager implements Token.OnAuth {
    
    public final DataStorage storage;
    public final Token.OnAuth onAuth;
    protected Token accessToken;
    
    public JWTManager(DataStorage storage, Token.OnAuth onAuth) {
        this.storage = storage;
        this.onAuth = onAuth;
    }
        
    @Override
    public Token auth() throws Exception {
        return onAuth.auth();
    }

    public void saveToken(Token token) throws IOException {
        storage.save("accessToken", token.getToken());
        this.accessToken = token;
    }
    
    public Token readToken() throws IOException {
        if (accessToken == null) {
            String token = storage.read("accessToken");
            if (accessToken != null) {
                try {
                    accessToken = new JWT(token);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return accessToken;
    }
    
    public void deleteToken() throws IOException {
        storage.delete("accessToken");
        accessToken = null;
    }
       
    public synchronized Token getAccessToken() throws Exception {
        Token token = readToken();
        
        if (token == null) {
            token = auth();
            saveToken(token);
            return token;
        }
        
        if (token.isValid()) {
            return token;
        }
        
        token = auth();
        saveToken(token);
        
        return token;
    }

}
