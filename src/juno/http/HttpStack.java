package juno.http;

import java.io.IOException;

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
  public HttpResponse execute(HttpRequest request) throws IOException;
}