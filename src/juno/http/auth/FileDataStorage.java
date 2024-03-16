package juno.http.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import juno.io.IOUtils;

public class FileDataStorage implements DataStorage {

    public final File src;
    private Properties storage;

    public FileDataStorage(File src) {
        this.src = src;
    }
    
    public synchronized Properties load() throws IOException {
        if (storage == null ) {
            storage = new Properties();
                                
            if (src.exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(src);
                    storage.load(in);

                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        
        return storage;
    }
    
    public synchronized void store(Properties properties) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(src);
            properties.store(out, null);
            
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
    
    @Override
    public String read(String key) throws IOException {
        Properties properties = load();
        return properties.getProperty(key);
    }
    
    @Override
    public void save(String key, String value) throws IOException {
        Properties properties = load();
        properties.setProperty(key, value);
        store(properties);
    }

    @Override
    public void delete(String key) throws IOException {
        Properties properties = load();
        properties.remove(key);
        store(properties);
    }
}
