package juno.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import juno.io.IOUtils;

public class ResponseBody implements Closeable {
    /** Codificación predeterminada. */
   public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");

   public int code;  
   public String status;
   public final Headers headers = new Headers();
   public boolean closed;
   public Charset charset = DEFAULT_ENCODING;
   public final InputStream in;

   public ResponseBody(InputStream in) {
     this.in = in;
   }

   public long getContentLength() {
     return headers.getContentLength();
   }

   public String getContentType() {
     return headers.getContentType();
   }

   public byte[] readBytes() throws IOException {
     try {
       return IOUtils.readByteArray(in);
     } finally {
       close();
     }
   }

   public String readString(Charset charset) throws IOException {
     try {
       return IOUtils.readString(in, charset);
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
       IOUtils.closeQuietly(in);
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
