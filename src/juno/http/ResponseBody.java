package juno.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import juno.io.IOUtils;
import juno.http.convert.ResponseBodyConvert;

public class ResponseBody implements Closeable {
  public HttpRequest request;
  public int code;  
  public String status;
  public final Headers headers = new Headers();
  public boolean closed;
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
  
  public Reader charStream(Charset charset) throws IOException {
    return new InputStreamReader(in, charset);
  }
  
  public Reader charStream() throws IOException {
    return new InputStreamReader(in, request.charset);
  }
  
  public byte[] bytes() throws IOException {
    try {
      return IOUtils.readByteArray(in);
    } finally {
      close();
    }
  }
  
  public String string(Charset charset) throws IOException {
    final byte[] data = bytes();
    return new String(data, charset);
  }
  
  public String string() throws IOException {
    return string(request != null ? request.charset : HttpRequest.DEFAULT_ENCODING);
  }
  
  public void writeTo(OutputStream out) throws IOException {
    try {
      IOUtils.copy(in, out);
    } finally {
      close();
    }
  }
  
  public <V> V as(ResponseBodyConvert<V> convert) throws Exception {
    return convert.parse(this);
  }
  
  public <V> V as(Class<V> classOf) throws Exception {
    if (request == null || request.client == null) {
        throw new NullPointerException("This request does not have any HttpCli");
    }
    return as(request.client.getFactory().getResponseBodyConvert(classOf));
  }
  
  @Override public void close() {
    if (!closed) {
      closed = Boolean.TRUE;
      IOUtils.closeQuietly(in);
    }
  }   
}
