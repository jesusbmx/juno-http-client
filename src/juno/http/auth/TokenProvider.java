package juno.http.auth;

public interface TokenProvider {
    
    Token getValidToken() throws Exception;
}
