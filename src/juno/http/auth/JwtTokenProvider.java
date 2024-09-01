package juno.http.auth;

import java.io.File;
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

    @Override
    public Token retrieveOrRefreshToken() throws Exception {
        // Intenta recuperar el token de acceso almacenado
        String accessToken = getAccessToken();
        
        // Si el token de acceso no existe, se ejecuta el callback para refrescarlo
        if (accessToken == null) {
            onTokenRefresh.onTokenRefresh(this);
            accessToken = getAccessToken();
            
        } else {
            // Si existe un token, valida si es válido
            final JwtToken jwtToken = new JwtToken(accessToken);
            if (jwtToken.isValid()) {
                return jwtToken;
            }
            // Si el token no es válido, refresca el token llamando al callback
            onTokenRefresh.onTokenRefresh(this);
            accessToken = getAccessToken();
        }
            
        // Si después de intentar refrescar el token aún no se tiene un token de acceso, lanza una excepción
        if (accessToken == null) {
            throw new Exception("The token provider does not contain any access tokens");
        }
        
        // Retorna el nuevo token de acceso como un JwtToken
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
