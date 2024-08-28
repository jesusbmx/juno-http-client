package juno.http.auth;

import java.io.File;
import juno.content.DataStorage;
import juno.content.FileDataStorage;

public class JwtTokenProvider implements TokenProvider {
    
    private final DataStorage storage;
    private final OnRefresh onRefresh;

    public interface OnRefresh {
        void onRefreshRequest(TokenProvider provider) throws Exception;
    }
    
    public JwtTokenProvider(DataStorage storage, OnRefresh onRefresh) {
        this.storage = storage;
        this.onRefresh = onRefresh;
    }
    
    public JwtTokenProvider(File storage, OnRefresh onRefresh) {
        this(new FileDataStorage(storage), onRefresh);
    }

    @Override
    public Token retrieveOrRefreshToken() throws Exception {
        String accessToken = getAccessToken();
        
        if (accessToken == null) {
            onRefresh.onRefreshRequest(this);
            accessToken = getAccessToken();
            
        } else {
            final JwtToken jwtToken = new JwtToken(accessToken);
            if (jwtToken.isValid()) {
                return jwtToken;
            }
            onRefresh.onRefreshRequest(this);
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
