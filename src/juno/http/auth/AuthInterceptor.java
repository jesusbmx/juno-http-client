package juno.http.auth;

import juno.http.Authorization;

public class AuthInterceptor implements Authorization {
    
    private final TokenManager tokenManager;
    private String headerPrefix;

    public AuthInterceptor(String headerPrefix, TokenManager tokenManager) {
        this.tokenManager = tokenManager;
        this.headerPrefix = headerPrefix;
    }
    
    public AuthInterceptor(TokenManager tokenManager) {
        this("Bearer ", tokenManager);
    }

    @Override
    public String getAuthorizationHeaderValue() throws Exception {
        final Token token = tokenManager.retrieveValidAccessToken();
        return headerPrefix + token.getToken();
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

    public AuthInterceptor setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
        return this;
    }
}
