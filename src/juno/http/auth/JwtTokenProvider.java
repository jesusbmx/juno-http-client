package juno.http.auth;

import java.io.File;
import juno.content.DataStorage;
import juno.content.FileDataStorage;

public class JwtTokenProvider implements TokenProvider {
    
    private final DataStorage storage;
    private final OnTokenRefresh onTokenRefresh;

    public interface OnTokenRefresh {
        void onTokenRefresh(TokenProvider provider) throws Exception;
    }
    
    public JwtTokenProvider(DataStorage storage, OnTokenRefresh onTokenRefresh) {
        this.storage = storage;
        this.onTokenRefresh = onTokenRefresh;
    }
    
    public JwtTokenProvider(File storage, OnTokenRefresh onTokenRefresh) {
        this(new FileDataStorage(storage), onTokenRefresh);
    }

    @Override
    public Token retrieveOrRefreshToken() throws Exception {
        String accessToken = getAccessToken();
        
        if (accessToken == null) {
            onTokenRefresh.onTokenRefresh(this);
            accessToken = getAccessToken();
            
        } else {
            final JwtToken jwtToken = new JwtToken(accessToken);
            if (jwtToken.isValid()) {
                return jwtToken;
            }
            onTokenRefresh.onTokenRefresh(this);
            accessToken = getAccessToken();
        }
            
        if (accessToken == null) {
            throw new Exception("The token provider does not contain any access tokens");
        }
        
        return new JwtToken(accessToken);
    }

    @Override
    public String getAccessToken() throws Exception {
        return storage.getItem("accessToken", null);
    }

    @Override
    public void setAccessToken(String token) throws Exception {
        storage.setItem("accessToken", token);
    }
    
    @Override
    public String getRefreshToken() throws Exception {
        return storage.getItem("refreshToken", null);
    }

    @Override
    public void setRefreshToken(String token) throws Exception {
        storage.setItem("refreshToken", token);
    }
    
}
