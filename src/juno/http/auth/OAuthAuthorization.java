package juno.http.auth;

import juno.http.Authorization;

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