package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import juno.io.IOUtils;

public final class Debug {

    private static final List<String> READABLE_CONTENT_TYPES = new ArrayList<String>(Arrays.asList(
        "application/json",
        "application/xml",
        "application/x-www-form-urlencoded",
        //"multipart/form-data",
        "text/plain",
        "text/html"
    ));
    
    private static boolean isDebug;
    
    private Debug() {
        
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean isDebug) {
        Debug.isDebug = isDebug;
    }

    public static void debug(String tag, Object... message) {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("[Debug] ").append(tag).append(" -> ");
            for (int i = 0; i < message.length; i++) {
                if (i > 0) {
                    debugInfo.append(" ");
                }
                debugInfo.append(message[i]);
            }
            debugInfo.append("\n");
            System.out.print(debugInfo);
        }
    }
    
    public static void debug(HttpRequest request) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("```http-request-").append(request.hashCode()).append("\n");
            debugInfo.append(request.getMethod()).append(" ")
                    .append(request.urlAndParams()).append(" HTTP/1.1").append("\r\n");
            
            final Headers headers = request.getHeaders();
            if (headers != null) {
                debugInfo.append(headers);
            }
            
            debugInfo.append("```\n");
            System.out.print(debugInfo);
        }
    }
    
    public static void debug(HttpRequest request, RequestBody rb, String contentType, long contentLength) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("```http-request-").append(request.hashCode()).append("\n");
            debugInfo.append(request.getMethod()).append(" ")
                    .append(request.urlAndParams()).append(" HTTP/1.1").append("\r\n");
            
            final Headers headers = request.getHeaders();
            if (headers != null) {
                debugInfo.append(headers);
            }
            
            debugInfo.append(Headers.CONTENT_TYPE).append(": ").append(contentType).append("\r\n")
                     .append(Headers.CONTENT_LENGTH).append(": ").append(contentLength).append("\r\n");

            if (isReadableContentType(contentType)) {
                final ByteArrayOutputStream outputStream = IOUtils.arrayOutputStream();
                rb.writeTo(outputStream);
                final String requestBodyString = outputStream.toString();
                outputStream.close();
                
                debugInfo.append("\r\n").append(requestBodyString).append("\r\n");

            } else {
                debugInfo.append("\r\n").append("-- binary --\r\n");
            }

            debugInfo.append("```\n");
            System.out.print(debugInfo);
        }
    }
    
    public static void debug(HttpRequest request, HttpResponse responseBody) {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("```http-response-").append(request.hashCode()).append("\n");
            debugInfo.append(responseBody).append("\r\n");
            debugInfo.append("```\n");
            System.out.print(debugInfo);
        }
    }
    
    private static boolean isReadableContentType(String contentType) {
        for (String legibleContentType : READABLE_CONTENT_TYPES) {
            if (contentType.startsWith(legibleContentType)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getReadableContentTypes() {
        return READABLE_CONTENT_TYPES;
    }
    
    public static void addReadableContentType(String contentType) {
        READABLE_CONTENT_TYPES.add(contentType);
    }
}
