package juno.http;

import java.util.ArrayList;
import java.util.List;
import juno.concurrent.Dispatcher;
import juno.http.convert.ConverterFactory;
import juno.http.convert.generic.GenericConverterFactory;
import juno.http.convert.RequestBodyConverter;
import juno.http.convert.ResponseBodyConverter;

public class HttpClient implements HttpStack {
    
  /** Singleton de la clase. */
  private static HttpClient instance;
    
  /** Procesara las peticiones a internet. */
  protected final HttpStack mHttpStack;
 
  /** Authorization */
  protected Authorization mAuthorization;
  
  /** Headers adicionales */
  protected Headers additionalHeaders = new Headers();
  
  /** Interceptor de peticiones. */
  protected OnInterceptor mInterceptor;
  
  /** Fabrica para los adaptadores. */
  private List<ConverterFactory> mConverterFactories = new ArrayList<ConverterFactory>();
  
  /** Procesa la peticiones en segundo plano. */
  private Dispatcher mDispatcher = Dispatcher.getInstance();
    
  public HttpClient(HttpStack stack) {
    mHttpStack = stack;
    mConverterFactories.add(new GenericConverterFactory());
  }
 
  public HttpClient() {
    this(new HttpURLConnectionStack());
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

  public HttpStack getHttpStack() {
    return mHttpStack;
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
  
  public Dispatcher getDispatcher() {
    return mDispatcher;
  }

  public void setDispatcher(Dispatcher dispatcher) {
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
  public HttpResponse execute(HttpRequest request) throws Exception {
    if (mAuthorization != null) {
        request.addHeader("Authorization", mAuthorization.generateAuthHeader());
    }
    for (int i = 0; i < additionalHeaders.size(); i++) {
        request.addHeader(additionalHeaders.getName(i), additionalHeaders.getValue(i));
    }
    if (mInterceptor != null) {
        return mInterceptor.intercept(request, getHttpStack());
    }
    return getHttpStack().execute(request);
  }

  public <V> V execute(HttpRequest request, ResponseBodyConverter<V> convert) throws Exception {
    HttpResponse response = null;
    try {
      response = execute(request);
      return convert.convert(response);
      
    } catch(Exception e) {
      if (response != null) {
        response.close();
      }
      
      throw e;
    }
  }
 
  public <V> V execute(HttpRequest request, Class<V> cast) throws Exception {
    return execute(request, getResponseBodyConverter(cast));
  }
 
  /**
   * Crea una invocación de un método que envía una solicitud a un servidor web 
   * y devuelve una respuesta.
   * 
   * @param <V>
   * @param request petición a realizar
   * @param convert adaptador para parsear la respuesta
   * 
   * @return una llamada
   */
  public <V> AsyncHttpRequest<V> createAsync(HttpRequest request, ResponseBodyConverter<V> convert) {
    return new AsyncHttpRequest<V>(getDispatcher(), this, request, convert);
  }
  
  public <V> AsyncHttpRequest<V> createAsync(HttpRequest request, Class<V> cast) {
    return this.createAsync(request, getResponseBodyConverter(cast));
  }
  
  public AsyncHttpRequest<HttpResponse> createAsync(HttpRequest request) {
    return this.createAsync(request, getResponseBodyConverter(HttpResponse.class));
  }
  
  public <V> RequestBody createRequestBody(V object) {
    if (object == null) return null;
    try {
      final Class<V> type = (Class<V>) object.getClass();
      return getRequestBodyConverter(type).convert(object);

    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
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