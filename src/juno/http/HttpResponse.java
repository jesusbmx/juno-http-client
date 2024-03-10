package juno.http;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import juno.io.IOUtils;

public class HttpResponse implements Closeable {
    /** Codificación predeterminada. */
   public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");

   protected boolean closed;
   public Charset charset = DEFAULT_ENCODING;
   public final int code;  
   public final String status;
   public final Headers headers;
   public final InputStream content;

   public HttpResponse(int code, String status, Headers headers, InputStream content) {
    this.code = code;
    this.status = status;
    this.headers = headers;
    this.content = content;
   }
   
   public HttpResponse(int code, String status, Headers headers, File content) throws FileNotFoundException {
     this(code, status, headers, new FileInputStream(content));
   }
   
   public HttpResponse(int code, String status, Headers headers, byte[] content) {
     this(code, status, headers, new ByteArrayInputStream(content));
   }

   public long getContentLength() {
     return headers.getContentLength();
   }

   public String getContentType() {
     return headers.getContentType();
   }

   public byte[] readBytes() throws IOException {
     try {
       return IOUtils.readByteArray(content);
     } finally {
       close();
     }
   }

   public String readString(Charset charset) throws IOException {
     try {
       return IOUtils.readString(content, charset);
     } finally {
       close();
     }
   }

   public String readString() throws IOException {
     return readString(charset);
   }

   @Override public void close() {
     if (!closed) {
       closed = true;
       IOUtils.closeQuietly(content);
     }
   }   

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        // Response Line
        sb.append("HTTP/1.1 ")
                .append(code)
                .append(" OK").append("\n");

        // Headers:
        for (int i = 0; i < headers.size(); i++) {
            sb.append(headers.getName(i));
            sb.append(": ");
            sb.append(headers.getValue(i));
            sb.append("\n");
        }

        sb.append("\n");
        
        // Response body
        sb.append("--- content ---");

        return sb.toString();
    }
  
  
}
