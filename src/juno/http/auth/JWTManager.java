package juno.http.auth;

import java.io.File;
import java.io.IOException;
import org.json.JSONException;

public class JWTManager implements JWT.OnAuth {
    
    public final DataStorage storage;
    public final JWT.OnAuth onAuth;
    protected JWT accessJwt;
    
    public JWTManager(DataStorage storage, JWT.OnAuth onAuth) {
        this.storage = storage;
        this.onAuth = onAuth;
    }
    
    public JWTManager(File file, JWT.OnAuth onAuth) {
        this(new DataStorageFile(file), onAuth);
    }
    
    @Override
    public JWT auth() throws Exception {
        return onAuth.auth();
    }
  
    public void saveToken(JWT token) throws IOException {
        storage.save("accessToken", token.getToken());
        this.accessJwt = token;
    }
    
    public JWT readToken() throws IOException {
        if (accessJwt == null) {
            String accessToken = storage.read("accessToken");
            if (accessToken != null) {
                try {
                    accessJwt = new JWT(accessToken);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return accessJwt;
    }
    
    public void deleteToken() throws IOException {
        storage.delete("accessToken");
        accessJwt = null;
    }
       
    public synchronized JWT getToken() throws Exception {
        JWT jwt = readToken();
        
        if (jwt == null) {
            jwt = auth();
            saveToken(jwt);
            return jwt;
        }
        
        if (jwt.isValid()) {
            return jwt;
        }
        
        jwt = auth();
        saveToken(jwt);
        
        return jwt;
    }
}
