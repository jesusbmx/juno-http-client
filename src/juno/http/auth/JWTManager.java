package juno.http.auth;

import juno.content.DataStorage;

public class JWTManager implements TokenManager {
    
    private final DataStorage storage;
    private final Token.OnAuth onAuth;
    
    public JWTManager(DataStorage storage, Token.OnAuth onAuth) {
        this.storage = storage;
        this.onAuth = onAuth;
    }

    @Override
    public Token getAccessToken() throws Exception {
        Token accessToken = readToken();
        
        if (accessToken == null || !accessToken.isValid()) {
            accessToken = onAuth.auth();
            saveToken(accessToken);
        }
        
        return accessToken;
    }

    private Token readToken() throws Exception {
        String token = storage.getItem("accessToken", null);
        return token != null ? new JWT(token) : null;
    }

    private void saveToken(Token token) throws Exception {
        storage.setItem("accessToken", token.getToken());
    }
}
