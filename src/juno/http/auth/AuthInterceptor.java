package juno.http.auth;

import juno.http.Authorization;

public class AuthInterceptor implements Authorization {
    
    private final TokenManager tokenManager;
    private String header = "Authorization";
    private String headerPrefix = "Bearer ";

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public String getValue() throws Exception {
        final Token token = tokenManager.getAccessToken();
        return headerPrefix + token.getToken();
    }

    @Override
    public String getHeader() {
        return header;
    }

    public AuthInterceptor setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

    public AuthInterceptor setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
        return this;
    }
}
