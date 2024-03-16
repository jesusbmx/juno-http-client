package juno.http.auth;

public interface TokenManager {
    Token getAccessToken() throws Exception;
}
