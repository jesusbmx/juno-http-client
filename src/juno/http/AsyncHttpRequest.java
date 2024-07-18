package juno.http;

import juno.concurrent.AbstractAsync;
import juno.concurrent.Dispatcher;
import juno.http.convert.ResponseBodyConvert;

public class AsyncHttpRequest<T> extends AbstractAsync<T> {
    public final HttpStack stack;
    public final HttpRequest request;
    public final ResponseBodyConvert<T> convert;
    protected OnInterceptor interceptor;

    /**
     * Inyección de Dependencias: Dispatcher, HttpClient, ResponseBodyConvert
     */
    public AsyncHttpRequest(
        Dispatcher dispatcher, HttpStack stack, HttpRequest request, ResponseBodyConvert<T> convert
    ) {
        super(dispatcher);
        this.stack = stack;
        this.request = request;
        this.convert = convert;
    }
    
    private HttpResponse execute(HttpRequest request) throws Exception {
        if (interceptor == null) {
            return stack.execute(request);
        } 
        return interceptor.intercept(request, stack);
    }

    @Override
    public T call() throws Exception {
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

    public OnInterceptor getInterceptor() {
        return interceptor;
    }
    
    public AsyncHttpRequest<T> setInterceptor(OnInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }
}
