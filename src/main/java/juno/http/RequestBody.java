package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import juno.io.Files;
import juno.io.IOUtils;

public abstract class RequestBody {
  public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");

  public abstract String contentType() throws IOException;

  public abstract long contentLength() throws IOException;

  public abstract void writeTo(OutputStream out) throws IOException;
  
  public static RequestBody create(final String contentType, final String content) {
    return new RequestBody() {
      @Override public String contentType() throws IOException {
        return contentType;
      }
      @Override public long contentLength() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
          baos = IOUtils.arrayOutputStream();
          writeTo(baos);
          return baos.size();
        } finally {
          IOUtils.closeQuietly(baos);
        }
      }
      @Override public void writeTo(OutputStream out) throws IOException {
        final Charset charset = Headers.getCharsetFromContentType(
                contentType, DEFAULT_ENCODING);
        byte[] data = content.getBytes(charset);
        out.write(data);
      }
    };
  }
  
  public static RequestBody create(final String contentType, final byte[] content) {
    return new RequestBody() {
      @Override public String contentType() throws IOException {
        return contentType;
      }
      @Override public long contentLength() throws IOException {
        return content.length;
      }
      @Override public void writeTo(OutputStream out) throws IOException {
        out.write(content);
      }
    };
  }
  
  public static RequestBody create(final String contentType, final File content) {
    return new RequestBody() {
      @Override public String contentType() throws IOException {
        return contentType;
      }
      @Override public long contentLength() throws IOException {
        return content.length();
      }
      @Override public void writeTo(OutputStream out) throws IOException {
        Files.copy(content, out);
      }
    };
  }
}
