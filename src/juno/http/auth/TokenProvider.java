package juno.http.auth;

public interface TokenProvider {
    
    Token getValidToken() throws Exception;
    
    JwtToken getAccessToken() throws Exception;
    
    void setAccessToken(JwtToken token) throws Exception;
}
