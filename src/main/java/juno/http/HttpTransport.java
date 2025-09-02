package juno.http;

public interface HttpTransport {

  /**
   * Ejecuta una petición.
   *
   * @param request petición a ejecutar
   *
   * @return el resultado de la petición realizada
   *
   * @throws java.io.IOException
   */
  public HttpResponse send(HttpRequest request) throws Exception;
}