package juno.http.auth;

import juno.http.Authorization;
import juno.util.Base64;

/**
 * "Basic %base64_encoded_credentials%"
 */
public class BasicAuthorization implements Authorization {

    private final String username;
    private final String password;

    public BasicAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String generateAuthHeader() {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }
}