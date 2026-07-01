package juno.http;

import java.util.ArrayList;
import java.util.List;
import juno.concurrent.Task;
import juno.concurrent.TaskDispatcher;
import juno.http.auth.Authorization;
import juno.http.convert.ConverterFactory;
import juno.http.convert.RequestBodyConverter;
import juno.http.convert.ResponseBodyConverter;
import juno.http.convert.generic.GenericConverterFactory;
import juno.http.convert.json.JSONConverterFactory;

public class HttpClient implements HttpTransport, HttpExecutor {
    
  /** Singleton de la clase. */
  private static HttpClient instance;
    
  /** Procesara las peticiones a internet. */
  protected final HttpTransport mTransport;
 
  /** Authorization */
  protected Authorization mAuthorization;
  
  /** Headers adicionales */
  protected Headers additionalHeaders = new Headers();
  
  /** Interceptor de peticiones. */
  protected OnInterceptor mInterceptor;
  
  /** Fabrica para los adaptadores. */
  private List<ConverterFactory> mConverterFactories = new ArrayList<ConverterFactory>();
  
  /** Procesa la peticiones en segundo plano. */
  private TaskDispatcher mDispatcher = TaskDispatcher.getInstance();

  /**
   * Comportamiento "axios" por defecto: {@link #execute}/{@link #newTask} lanzan
   * {@link HttpException} si la respuesta no fue 2xx. Desactívalo si tu backend
   * usa códigos no-2xx para respuestas de negocio (no solo errores) y necesitas
   * leer el body igual — como antes, se convierte sin importar el status.
   */
  protected boolean throwOnHttpError = true;


  public HttpClient(HttpTransport transport) {
    mTransport = transport;
    mConverterFactories.add(new GenericConverterFactory());
    if (JSONConverterFactory.isJSONSupportAvailable()) {
        mConverterFactories.add(new JSONConverterFactory());
    }
  }
 
  public HttpClient() {
    this(new URLConnectionTransport());
  }
  
  public synchronized static HttpClient getInstance() {
    if (instance == null) {
      instance = new HttpClient();
    }
    return instance;
  }
  
  public synchronized static void setInstance(HttpClient val) {
    instance = val;
  }

  public boolean isDebug() {
    return Debug.isDebug();
  }
  
  public HttpClient setDebug(boolean b) {
    Debug.setDebug(b);
    return this;
  }

  public HttpTransport getHttpTransport() {
    return mTransport;
  }

  public Authorization getAuthorization() {
    return mAuthorization;
  }

  public HttpClient setAuthorization(Authorization mAuthorization) {
    this.mAuthorization = mAuthorization;
    return this;
  }

  public Headers getHeaders() {
    return additionalHeaders;
  }

  public void setHeaders(Headers additionalHeaders) {
    this.additionalHeaders = additionalHeaders;
  }
  
  public HttpClient addHeader(String name, String value) {
    this.additionalHeaders.add(name, value);
    return this;
  }
 
  public OnInterceptor getInterceptor() {
    return mInterceptor;
  }
    
  public HttpClient setInterceptor(OnInterceptor interceptor) {
    this.mInterceptor = interceptor;
    return this;
  }

  public List<ConverterFactory> getConverterFactories() {
    return mConverterFactories;
  }

  public HttpClient setConverterFactories(List<ConverterFactory> mConverterFactories) {
    this.mConverterFactories = mConverterFactories;
    return this;
  }

  public HttpClient addConverterFactory(ConverterFactory convertFactory) {
    this.mConverterFactories.add(convertFactory);
    return this;
  }
  
  public TaskDispatcher getDispatcher() {
    return mDispatcher;
  }

  public void setDispatcher(TaskDispatcher dispatcher) {
    this.mDispatcher = dispatcher;
  }

  public boolean isThrowOnHttpError() {
    return throwOnHttpError;
  }

  public HttpClient setThrowOnHttpError(boolean throwOnHttpError) {
    this.throwOnHttpError = throwOnHttpError;
    return this;
  }

  /**
   * Envíe sincrónicamente la solicitud y devuelva su respuesta.
   * 
   * @param request petición a realizar
   * 
   * @return una respuesta para el tipo de petición realizada
   * 
   * @throws java.io.IOException si se produjo un problema al hablar con el
   * servidor
   */
  @Override
  public HttpResponse send(HttpRequest request) throws Exception {
    if (mAuthorization != null) {
        request.addHeader("Authorization", mAuthorization.generateAuthHeader());
    }
    for (int i = 0; i < additionalHeaders.size(); i++) {
        request.addHeader(additionalHeaders.getName(i), additionalHeaders.getValue(i));
    }
    if (mInterceptor != null) {
        return mInterceptor.intercept(request, getHttpTransport());
    }
    return getHttpTransport().send(request);
  }

  /**
   * Comportamiento "axios": lanza {@link HttpException} si la respuesta no fue
   * 2xx (a menos que {@link #setThrowOnHttpError} esté en {@code false}); si fue
   * exitosa (o no se valida el status) devuelve el {@link HttpResult} (code/headers/data).
   */
  @Override
  public <V> HttpResult<V> execute(HttpRequest request, ResponseBodyConverter<V> converter) throws Exception {
    HttpResponse response = send(request);
    try {
      return new HttpResult.Converter<>(converter, throwOnHttpError).convert(response);

    } finally {
      response.close();
    }
  }

  public <V> HttpResult<V> execute(HttpRequest request, Class<V> cast) throws Exception {
    return execute(request, getResponseBodyConverter(cast));
  }

  /**
   * Versión diferida de {@link #execute}: comparte el mismo flag {@link #isThrowOnHttpError()},
   * así que también lanza {@link HttpException} (hacia {@code onFailure}) si la
   * respuesta no fue 2xx, salvo que se haya desactivado.
   *
   * @param <V>
   * @param request petición a realizar
   * @param converter adaptador para parsear la respuesta
   *
   * @return una llamada diferida
   */
  public <V> HttpTask<V> newTask(HttpRequest request, ResponseBodyConverter<V> converter) {
    return new HttpTask<>(getDispatcher(), this, request, converter, throwOnHttpError);
  }

  public <V> HttpTask<V> newTask(HttpRequest request, Class<V> cast) {
    return newTask(request, getResponseBodyConverter(cast));
  }

  public HttpTask<HttpResponse> newTask(HttpRequest request) {
    return newTask(request, getResponseBodyConverter(HttpResponse.class));
  }

  /**
   * En vez de {@link HttpResult}{@code <V>} (como {@link #newTask}), resuelve
   * directamente {@link HttpResult#data}. Azúcar sobre {@code newTask(...).newDataTask()}
   * para no exponer el wrapping en el sitio de la llamada.
   */
  public <V> Task<V> newDataTask(HttpRequest request, ResponseBodyConverter<V> converter) {
    return newTask(request, converter).newDataTask();
  }

  public <V> Task<V> newDataTask(HttpRequest request, Class<V> cast) {
    return newTask(request, cast).newDataTask();
  }

  public Task<HttpResponse> newDataTask(HttpRequest request) {
    return newTask(request).newDataTask();
  }

  public <V> RequestBody createRequestBody(V object) {
    if (object == null) return null;
    try {
      final Class<V> type = (Class<V>) object.getClass();
      return getRequestBodyConverter(type).convert(object);

    } catch(Exception e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }
  
  public <V> ResponseBodyConverter<V> getResponseBodyConverter(Class<V> type) {
    for (ConverterFactory converterFactory : mConverterFactories) {
      ResponseBodyConverter responseBodyConverter = converterFactory.responseBodyConverter(type);
      if (responseBodyConverter != null) {
        return responseBodyConverter;
      }
    }
    throw new IllegalArgumentException("Could not HttpResponse converter for class '" + type.getCanonicalName() + "'");
  }
  
  public <V> RequestBodyConverter<V> getRequestBodyConverter(Class<V> type) {
    for (ConverterFactory converterFactory : mConverterFactories) {
      RequestBodyConverter<V> requestBodyConverter = converterFactory.requestBodyConverter(type);
      if (requestBodyConverter != null) {
        return requestBodyConverter;
      }
    }
    throw new IllegalArgumentException("Could not RequestBody converter for class '" + type.getCanonicalName() + "'");
  }

}