package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static juno.http.MultipartBody.COLON_SPACE;
import static juno.http.MultipartBody.CR_LF;
import static juno.http.MultipartBody.TWO_DASHES;
import juno.io.IOUtils;
import juno.util.Strings;

public final class Debug {

    private static final List<String> READABLE_CONTENT_TYPES = new ArrayList<String>(Arrays.asList(
        "application/json",
        "application/xml",
        "application/x-www-form-urlencoded",
        "multipart/form-data",
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
            debugInfo.append(Strings.join(message, " "));            
            debugInfo.append("\n");
            System.out.print(debugInfo);
        }
    }
    
    public static void debugRequest(HttpRequest request) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder()
                    .append("```http-request-").append(request.hashCode()).append("\n")
                    .append(request.getMethod()).append(" ")
                    .append(request.urlAndParams()).append(" HTTP/1.1").append("\r\n");
            
            final Headers headers = request.getHeaders();
            if (headers != null) {
                debugInfo.append(headers);
            }
            
            debugInfo.append("```\n");
            System.out.print(debugInfo);
        }
    }
    
    public static void debugRequest(HttpRequest request, RequestBody rb, String contentType, long contentLength) throws IOException {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder()
                    .append("```http-request-").append(request.hashCode()).append("\n")
                    .append(request.getMethod()).append(" ")
                    .append(request.urlAndParams()).append(" HTTP/1.1").append("\r\n");
            
            final Headers headers = request.getHeaders();
            if (headers != null) {
                debugInfo.append(headers);
            }
            
            debugInfo.append(Headers.CONTENT_TYPE).append(": ").append(contentType).append("\r\n")
                     .append(Headers.CONTENT_LENGTH).append(": ").append(contentLength).append("\r\n");

            if (isReadableContentType(contentType)) {
                try (ByteArrayOutputStream outputStream = IOUtils.arrayOutputStream()) {
                    if (rb instanceof MultipartBody) {
                        write(System.out, (MultipartBody) rb);
                    } else {
                        rb.writeTo(outputStream);
                    }
                    debugInfo.append("\r\n").append(outputStream.toString()).append("\r\n");
                }
            } else {
                debugInfo.append("\r\n{{binary}}\r\n");
            }

            debugInfo.append("```\n");
            System.out.print(debugInfo);
        }
    }
    
    private static void write(OutputStream out, MultipartBody multipartBody) throws IOException {
        byte[] boundaryToCharArray = multipartBody.boundary.getBytes();
        List<MultipartBody.Part> parts = multipartBody.parts();
        
        for (MultipartBody.Part part : parts) {      
          out.write(TWO_DASHES);
          out.write(boundaryToCharArray);
          out.write(CR_LF);

          // Write Format Multipart Header:
          for (int i = 0, size = part.headers.size(); i < size; i++) {
            out.write(part.headers.getName(i).getBytes());
            out.write(COLON_SPACE);
            out.write(part.headers.getValue(i).getBytes());
            out.write(CR_LF);
          }
          
          final String contentType = part.body.contentType();
          out.write(Headers.CONTENT_TYPE.getBytes());
          out.write(COLON_SPACE);
          out.write(contentType.getBytes());
          out.write(CR_LF);

          // Write Body:
          out.write(CR_LF);
          if (isReadableContentType(contentType)) 
              part.body.writeTo(out);
          else
              out.write("{{binary}}".getBytes());
          
          out.write(CR_LF);
        }

        // End of multipart/form-data.
        out.write(TWO_DASHES);
        out.write(boundaryToCharArray);
        out.write(TWO_DASHES);
        out.write(CR_LF);
    }
    
    public static void debugResponse(HttpRequest request, HttpResponse responseBody) {
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
