package juno.http.auth;

import juno.http.Authorization;

/**
 * Implements OAuth Authorization scheme: "OAuth %token%"
 */
public class OAuthAuthorization implements Authorization {

    private final String token;

    public OAuthAuthorization(String token) {
        this.token = token;
    }

    @Override
    public String generateAuthHeader() {
        return "OAuth " + token;
    }
}