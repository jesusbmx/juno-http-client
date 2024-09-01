package juno.http.auth;

public interface TokenProvider {

    public String getAccessToken() throws Exception;
    
    public Token getAccessToken(String token) throws Exception;

    public void setAccessToken(String token) throws Exception;
    
    public void setAccessToken(Token token) throws Exception;
    
    public String getRefreshToken() throws Exception;
    
    public Token getRefreshToken(String token) throws Exception;

    public void setRefreshToken(String token) throws Exception;
    
    public void setRefreshToken(Token token) throws Exception;
    
    public void clearAuthTokens() throws Exception;
    
    public Token retrieveOrRefreshToken() throws Exception;
      
    public boolean isLoggedIn();
    
}
