package juno.http.auth;

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