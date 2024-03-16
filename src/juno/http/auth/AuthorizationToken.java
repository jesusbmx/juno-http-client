package juno.http.auth;

import juno.http.Authorization;

public class AuthorizationToken implements Authorization {
    
    private final TokenManager tokenManager;
    private final String type;

    public AuthorizationToken(String type, TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.type = type;
    }

    @Override
    public String getAuthorization() throws Exception {
        Token token = tokenManager.getAccessToken();
        return type + " " + token.getToken();
    }
}
