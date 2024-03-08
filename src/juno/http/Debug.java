package juno.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public final class Debug {
    private static Debug instance;
    
    private static final List<String> LEGIBLE_CONTENT_TYPES = Arrays.asList(
            "application/json",
            "application/xml",
            "text/plain",
            "text/html"
    );
    
    private boolean isDebug;
    
    private Debug() {
        
    }

    public static Debug getInstance() {
        if (instance == null) {
            instance = new Debug();
        }
        return instance;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void debug(String format, Object... args) {
        if (isDebug) {
            System.out.printf(format + "\n", args);
        }
    }

    public void debug(RequestBody rb, String contentType, long contentLength, Charset charset) throws IOException {
        if (isDebug) {
            debug("%s: %s", Headers.CONTENT_TYPE, contentType);
            debug("%s: %s", Headers.CONTENT_LENGTH, contentLength);
            if (isLegibleContentType(contentType)) {
                System.out.println();
                rb.writeTo(System.out, charset);
                System.out.println();
            } else {
                System.out.println();
                System.out.println("-- binary --");
                System.out.println();
            }
        }
    }
    
    public void debug(ResponseBody responseBody) {
        if (isDebug) {
            System.out.println(responseBody);
        }
    }
    
    private boolean isLegibleContentType(String contentType) {
        for (String legibleContentType : LEGIBLE_CONTENT_TYPES) {
            if (contentType.contains(legibleContentType)) {
                return true;
            }
        }
        return false;
    }
}
