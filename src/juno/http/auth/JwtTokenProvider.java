package juno.http.auth;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import juno.content.DataStorage;
import juno.content.FileDataStorage;

public class JwtTokenProvider implements TokenProvider {
    
    // Almacenamiento para los tokens (acceso y refresco)
    private final DataStorage storage;
    
    // Callback que se ejecuta cuando se debe refrescar el token
    private final OnTokenRefresh onTokenRefresh;

    // Interfaz que define un método para manejar el refresco del token
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
    
    public DataStorage getStorage() {
        return storage;
    }

    @Override
    public JwtToken retrieveOrRefreshToken() throws Exception {
        // Intenta recuperar el token de acceso almacenado
        JwtToken accessToken = getAccessToken(getAccessToken());
        
        // Si el token de acceso no existe o no es valido, se ejecuta el callback para refrescarlo
        if (accessToken == null || !accessToken.isValid()) {
            onTokenRefresh.onTokenRefresh(this);
            accessToken = getAccessToken(getAccessToken());
        } 
            
        // Si después de intentar refrescar el token aún no se tiene un token de acceso, lanza una excepción
        if (accessToken == null) {
            throw new Exception("The token provider does not contain any access tokens");
        }
        
        // Retorna el nuevo token de acceso como un JwtToken
        return accessToken;
    }

    @Override
    public boolean isLoggedIn() {
        try {
            // Intenta recuperar el token de acceso almacenado
            final JwtToken accessToken = getAccessToken(getAccessToken());
            if (accessToken == null) {
                return false;
            }
            // Si existe un token, valida si es válido
            return accessToken.isValid();
            
        } catch (Exception e) {
            return false; // Token inválido o formato incorrecto
        }
    }
    
    @Override
    public String getAccessToken() throws Exception {
        return storage.getItem("accessToken", null);
    }
    
    @Override
    public JwtToken getAccessToken(String token) throws Exception {
        return token == null ? null : new JwtToken(token);
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
    public JwtToken getRefreshToken(String token) throws Exception {
        return token == null ? null : new JwtToken(token);
    }

    @Override
    public void setRefreshToken(String token) throws Exception {
        storage.setItem("refreshToken", token);
    }
    
    /**
     * Remove the auth tokens from storage
     * @throws java.lang.Exception
     */
    @Override
    public void clearAuthTokens() throws Exception {
        List<String> keys = Arrays.asList("accessToken", "refreshToken");
        storage.multiRemove(keys);
    }
}
