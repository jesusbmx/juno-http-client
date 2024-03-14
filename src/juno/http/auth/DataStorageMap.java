package juno.http.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataStorageMap implements DataStorage {

    Map<String, String> values = new HashMap<String, String>();

    @Override
    public String read(String key) throws IOException {
        return values.get(key);
    }

    @Override
    public void save(String key, String value) throws IOException {
        values.put(key, value);
    }

    @Override
    public void delete(String key) throws IOException {
        values.remove(key);
    }

}
