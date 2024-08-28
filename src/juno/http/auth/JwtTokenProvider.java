package juno.http.auth;

import java.io.File;
import juno.content.DataStorage;
import juno.content.FileDataStorage;

public class JwtTokenProvider implements TokenProvider {
    
    private final DataStorage storage;
    private final OnAuth onAuth;
    
    public interface OnAuth {

        /**
         * logea el app con el webservice.
         *
         * @param oldToken
         * @return
         * @throws java.lang.Exception
         */
        JwtToken auth(JwtToken oldToken) throws Exception;
    }
    
    public JwtTokenProvider(DataStorage storage, OnAuth onAuth) {
        this.storage = storage;
        this.onAuth = onAuth;
    }
    
    public JwtTokenProvider(File storage, OnAuth onAuth) {
        this(new FileDataStorage(storage), onAuth);
    }

    @Override
    public Token getValidToken() throws Exception {
        JwtToken accessToken = readToken();
        
        if (accessToken == null || !accessToken.isValid()) {
            accessToken = onAuth.auth(accessToken);
            saveToken(accessToken);
        }
        
        return accessToken;
    }

    private JwtToken readToken() throws Exception {
        final String token = storage.getItem("accessToken", null);
        return token != null ? new JwtToken(token) : null;
    }

    private void saveToken(JwtToken token) throws Exception {
        storage.setItem("accessToken", token.getToken());
    }
}
