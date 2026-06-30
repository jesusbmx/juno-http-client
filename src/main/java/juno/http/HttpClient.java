package juno.http;

import java.util.ArrayList;
import java.util.List;
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

  @Override
  public <V> V send(HttpRequest request, ResponseBodyConverter<V> converter) throws Exception {
    HttpResponse response = null;
    try {
      response = send(request);
      return converter.convert(response);
      
    } catch(Exception e) {
      if (response != null) {
        response.close();
      }
      
      throw e;
    }
  }
 
  @Override
  public <V> V send(HttpRequest request, Class<V> cast) throws Exception {
    return send(request, getResponseBodyConverter(cast));
  }

  @Override
  public <V> HttpResult<V> sendResult(HttpRequest request, ResponseBodyConverter<V> converter) throws Exception {
    return send(request, new HttpResult.Converter<>(converter));
  }

  @Override
  public <V> HttpResult<V> sendResult(HttpRequest request, Class<V> cast) throws Exception {
    return sendResult(request, getResponseBodyConverter(cast));
  }
 
  /**
   * Crea una invocación de un método que envía una solicitud a un servidor web 
   * y devuelve una respuesta.
   * 
   * @param <V>
   * @param request petición a realizar
   * @param converter adaptador para parsear la respuesta
   * 
   * @return una llamada
   */
  public <V> HttpTask<V> newTask(HttpRequest request, ResponseBodyConverter<V> converter) {
    return new HttpTask<V>(getDispatcher(), this, request, converter)
        .setThrowOnHttpError(true);
  }
  
  public <V> HttpTask<V> newTask(HttpRequest request, Class<V> cast) {
    return this.newTask(request, getResponseBodyConverter(cast));
  }

  public HttpTask<HttpResponse> newTask(HttpRequest request) {
    return this.newTask(request, getResponseBodyConverter(HttpResponse.class));
  }

  public <V> HttpTask<HttpResult<V>> newResultTask(HttpRequest request, ResponseBodyConverter<V> converter) {
    return new HttpTask<>(getDispatcher(), this, request, new HttpResult.Converter<>(converter));
  }

  public <V> HttpTask<HttpResult<V>> newResultTask(HttpRequest request, Class<V> cast) {
    return newResultTask(request, getResponseBodyConverter(cast));
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