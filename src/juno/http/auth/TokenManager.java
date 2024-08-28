package juno.http.auth;

public interface TokenManager {
    
    public Token getAccessToken() throws Exception;
}
