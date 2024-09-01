package juno.http.auth;

public interface TokenProvider {
    
    Token retrieveOrRefreshToken() throws Exception;
    
    String getAccessToken() throws Exception;
    
    void setAccessToken(String token) throws Exception;
    
    String getRefreshToken() throws Exception;
    
    void setRefreshToken(String token) throws Exception;
    
    void clearAuthTokens() throws Exception ;
}
