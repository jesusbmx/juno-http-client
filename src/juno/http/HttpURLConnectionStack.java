package juno.http;

import java.io.BufferedOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import juno.io.IOUtils;

public class HttpURLConnectionStack implements HttpStack { 
  
  /**
   * Abre una conexión HTTP a intenert apartir de una petición.
   *
   * @param src conección.
   *
   * @return una conexión HTTP abierta.
   *
   * @throws java.io.IOException
   */
  public HttpURLConnection open(URL src) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) src.openConnection();
    // Workaround for the M release HttpURLConnection not observing the
    // HttpURLConnection.setFollowRedirects() property.
    // https://code.google.com/p/android/issues/detail?id=194495
    conn.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
    return conn;
  }

  /**
   * Abre una conexión HTTP a intenert apartir de una petición.
   *
   * @param request petición a intenert.
   *
   * @return una conexión HTTP abierta.
   *
   * @throws java.io.IOException
   */
  public HttpURLConnection open(HttpRequest request) throws IOException {
    final String url = request.urlAndParams();
    Debug.getInstance()
            .debug("%s %s", request.getMethod(), url);
    
    final URL src = new URL(url);
    final HttpURLConnection conn = open(src);
    conn.setConnectTimeout(request.getTimeoutMs());
    conn.setReadTimeout(request.getTimeoutMs());
    conn.setUseCaches(Boolean.FALSE);
    conn.setDoInput(Boolean.TRUE);
    conn.setRequestMethod(request.getMethod());
    
    return conn;
  }

  /**
   * Manda una lista de encabezados adicionales de HTTP para esta petición.
   *
   * @param conn HTTP
   * @param request peticion
   *
   * @throws IOException
   */
  public void writeHeaders(HttpURLConnection conn, HttpRequest request)
  throws IOException {    
    final Headers headers = request.getHeaders();
    if (headers != null) {
      Debug.getInstance().debug("%s", headers);
      for (int i = 0, size = headers.size(); i < size; i++) {
        conn.addRequestProperty(
                headers.getName(i), headers.getValue(i));
      }
    }
  }
  
  /**
   * Escribe el cuerpo de de esta petición.
   *
   * @param conn HTTP
   * @param request peticion
   *
   * @throws IOException
   */
  public void writeBody(HttpURLConnection conn, HttpRequest request) 
  throws IOException {
    if (!request.requiresRequestBody()) {
      return;
    }
     
    final RequestBody requestBody = request.getBody();
    if (requestBody != null) {
      final String contentType = requestBody.contentType(request.getCharset()); 
        
      // Setup connection:
      conn.setDoOutput(Boolean.TRUE);
      conn.addRequestProperty(Headers.CONTENT_TYPE, contentType);

      // Length:
      final long contentLength = requestBody.contentLength(
              request.getCharset());
      conn.setFixedLengthStreamingMode((int) contentLength);

      // Write params:
      BufferedOutputStream bos = null;
      try {
        bos = new BufferedOutputStream(conn.getOutputStream());
        Debug.getInstance().debug(requestBody, contentType, contentLength, request.charset);
        requestBody.writeTo(bos, request.getCharset());
        
      } finally {
        IOUtils.closeQuietly(bos);
      }
    }
  }
  
  /**
   * Obtiene la respuesta del servidor
   * @param conn
   * @param request
   * @return
   * @throws IOException 
   */
  public ResponseBody getResponse(HttpURLConnection conn, HttpRequest request) 
  throws IOException {
    final int responseCode = conn.getResponseCode();
    final String status = conn.getResponseMessage();

    if (responseCode == -1) {
      // -1 is returned by getResponseCode() if the response code could not be retrieved.
      // Signal to the caller that something was wrong with the connection.
      throw new IOException("Could not retrieve response code from HttpUrlConnection.");
    }
    
    // HttpEntity populated with data from <code>connection</code>.
    InputStream in;
    try {
      in = conn.getInputStream();
    } catch (IOException ioe) {
      in = conn.getErrorStream();
    }
    if (in == null) {
      throw new IOException("code: " + responseCode + ", status: " + status 
              + ", message: The container is empty");
    }
    
    // Create Http Response
    final ResponseBody response = new ResponseBody(
            new UrlConnectionInputStream(conn, in));
    
    response.request = request;
    response.code = responseCode;
    response.status = status;
    response.headers.addHeadersMapList(conn.getHeaderFields());
    
    Debug.getInstance().debug("\nHTTP Response %s %s\n%s\n", 
            responseCode, status, response.headers);
    
    return response;
  }
  
  /**
   * Wrapper for a {@link HttpURLConnection}'s InputStream which disconnects the connection on
   * stream close.
   */
  static class UrlConnectionInputStream extends FilterInputStream {
    private final HttpURLConnection mConnection;

    UrlConnectionInputStream(HttpURLConnection connection, InputStream in) {
      super(in);
      mConnection = connection;
    }

    @Override
    public void close() throws IOException {
      super.close();
      mConnection.disconnect();
     }
  }

  /**
   * Ejecuta una petición.
   *
   * @param request petición a ejecutar
   *
   * @return el resultado de la petición realizada
   * 
   * @throws java.io.IOException
   */
  @Override public ResponseBody execute(HttpRequest request) throws IOException {
    HttpURLConnection conn = null;
    try {
      conn = open(request);
      writeHeaders(conn, request);
      writeBody(conn, request);
      return getResponse(conn, request);
     
    } catch (IOException e) {
      if (conn != null) {
        conn.disconnect();
      }
      throw e;
    }
  }
  
}