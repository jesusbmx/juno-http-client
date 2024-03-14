package juno.http.auth;

import juno.http.Authorization;

public class AuthorizationToken implements Authorization {
    
    public final JWTManager tokenManager;
    public final String type;

    public AuthorizationToken(String type, JWTManager tokenManager) {
        this.tokenManager = tokenManager;
        this.type = type;
    }

    @Override
    public String getAuthorization() throws Exception {
        // Validamos si estamos logeados para relizar peticiones.
        final Token token = tokenManager.getAccessToken();
        return type + " " + token.getToken();
    }
    
}