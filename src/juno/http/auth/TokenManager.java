package juno.http.auth;

public interface TokenManager {
    
    public Token retrieveValidAccessToken() throws Exception;
}
