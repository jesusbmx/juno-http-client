package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import juno.io.IOUtils;

public final class Debug {
    private static final List<String> LEGIBLE_CONTENT_TYPES = Arrays.asList(
            "application/json",
            "application/xml",
            "text/plain",
            "text/html"
    );
    
    private static boolean isDebug;
    
    private Debug() {
        
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean isDebug) {
        Debug.isDebug = isDebug;
    }
//    
//    public static void debug(String method, String url) {
//        if (isDebug) {
//            System.out.println(method + " " + url + "\n");
//        }
//    }
//    
//    public static void debug(Headers headers) {
//        if (isDebug) {
//            System.out.println(headers);
//        }
//    }

    public static void debug(HttpRequest request) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append(request.getMethod()).append(" ").append(request.urlAndParams()).append("\n");
            
            final Headers headers = request.headers;
            if (headers != null) {
                debugInfo.append("\n").append(headers);
            }
            
            System.out.println(debugInfo);
        }
    }
    
    public static void debug(HttpRequest request, RequestBody rb, String contentType, long contentLength) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append(request.getMethod()).append(" ").append(request.urlAndParams()).append("\n");
            
            final Headers headers = request.headers;
            if (headers != null) {
                debugInfo.append("\n").append(headers);
            }
            
            debugInfo.append("\n").append(Headers.CONTENT_TYPE).append(": ").append(contentType).append("\n")
                    .append(Headers.CONTENT_LENGTH).append(": ").append(contentLength).append("\n\n");

            if (isLegibleContentType(contentType)) {
                final ByteArrayOutputStream outputStream = IOUtils.arrayOutputStream();
                rb.writeTo(outputStream, request.charset);
                final String requestBodyString = outputStream.toString();
                outputStream.close();
                
                debugInfo.append(requestBodyString).append("\n");

            } else {
                debugInfo.append("-- binary --\n\n");
            }

            System.out.println(debugInfo);
        }
    }
    
    public static void debug(ResponseBody responseBody) {
        if (isDebug) {
            System.out.println(responseBody);
        }
    }
    
    private static boolean isLegibleContentType(String contentType) {
        for (String legibleContentType : LEGIBLE_CONTENT_TYPES) {
            if (contentType.contains(legibleContentType)) {
                return true;
            }
        }
        return false;
    }
}
