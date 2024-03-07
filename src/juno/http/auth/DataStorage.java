package juno.http.auth;

import java.io.IOException;

public interface DataStorage {
    
    public String read(String key)
            throws IOException;

    public void save(String key, String value)
            throws IOException;

    public void delete(String key)
            throws IOException;
    
}
