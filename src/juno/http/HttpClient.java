package juno.http;

import juno.concurrent.Dispatcher;
import juno.http.convert.ConvertFactory;
import juno.http.convert.ResponseBodyConvert;
import juno.http.convert.generic.GenericConvertFactory;

public class HttpClient implements HttpStack {
    
  /** Singleton de la clase. */
  private static HttpClient instance;
    
  /** Procesara las peticiones a internet. */
  protected final HttpStack mHttpStack;
 
  /** Interceptor de peticiones. */
  protected OnInterceptor mInterceptor;
  
  /** Fabrica para los adaptadores. */
  private ConvertFactory mConvertFactory = GenericConvertFactory.getInstance();
  
  /** Procesa la peticiones en segundo plano. */
  private Dispatcher mDispatcher = Dispatcher.getInstance();
    
  public HttpClient(HttpStack stack) {
    mHttpStack = stack;
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
  
  public OnInterceptor getInterceptor() {
    return mInterceptor;
  }
    
  public HttpClient setInterceptor(OnInterceptor interceptor) {
    this.mInterceptor = interceptor;
    return this;
  }
  
  public ConvertFactory getFactory() {
    return mConvertFactory;
  }
  
  public HttpClient setFactory(ConvertFactory convertFactory) {
    this.mConvertFactory = convertFactory;
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
    if (mInterceptor != null) {
        return mInterceptor.intercept(request, getHttpStack());
    }
    return getHttpStack().execute(request);
  }

  public <V> V execute(HttpRequest request, ResponseBodyConvert<V> convert) throws Exception {
    HttpResponse response = null;
    try {
      response = execute(request);
      return convert.parse(response);
      
    } catch(Exception e) {
      if (response != null) {
        response.close();
      }
      
      throw e;
    }
  }
 
  public <V> V execute(HttpRequest request, Class<V> cast) throws Exception {
    return execute(request, getFactory().getResponseBodyConvert(cast));
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
  public <V> AsyncRequest<V> newAsyncRequest(HttpRequest request, ResponseBodyConvert<V> convert) {
    return new AsyncRequest<V>(getDispatcher(), this, request, convert);
  }
  
  public <V> AsyncRequest<V> newAsyncRequest(HttpRequest request, Class<V> cast) {
    return this.newAsyncRequest(request, getFactory()
            .getResponseBodyConvert(cast));
  }
  
  public AsyncRequest<HttpResponse> newAsyncRequest(HttpRequest request) {
    return this.newAsyncRequest(request, getFactory()
            .getResponseBodyConvert(HttpResponse.class));
  }
  
  public <V> RequestBody createRequestBody(V src) {
    return getFactory().createRequestBody(src);
  }
  
  public <V> FormBody createFormBody(V src) {
    return getFactory().createFormBody(src);
  }
  
  public <V> MultipartBody createMultipartBody(V src) {
    return getFactory().createMultipartBody(src);
  }
}