package juno.http.auth;

import juno.http.Authorization;

/**
 * "OAuth %parameters%"
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