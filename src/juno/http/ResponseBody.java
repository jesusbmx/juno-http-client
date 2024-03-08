package juno.http;

import java.io.Closeable;
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
  
//  public Reader charStream(Charset charset) throws IOException {
//    return new InputStreamReader(in, charset);
//  }
//  
//  public Reader charStream() throws IOException {
//    return charStream(charset);
//  }
//  
//  public byte[] bytes() throws IOException {
//    try {
//      return IOUtils.readByteArray(in);
//    } finally {
//      close();
//    }
//  }
//  
//  public String string(Charset charset) throws IOException {
//    byte[] data = bytes();
//    return new String(data, charset);
//  }
//  
//  public String string() throws IOException {
//    return string(charset);
//  }
//  
//  public void writeTo(OutputStream out) throws IOException {
//    try {
//      IOUtils.copy(in, out);
//    } finally {
//      close();
//    }
//  }
  
  @Override public void close() {
    if (!closed) {
      closed = true;
      IOUtils.closeQuietly(in);
    }
  }   
}
