package juno.http;

public interface HttpStack {

  /**
   * Ejecuta una petición.
   *
   * @param request petición a ejecutar
   *
   * @return el resultado de la petición realizada
   *
   * @throws java.io.IOException
   */
  public HttpResponse execute(HttpRequest request) throws Exception;
}