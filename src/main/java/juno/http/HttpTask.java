package juno.http;

import juno.concurrent.AbstractTask;
import juno.concurrent.TaskDispatcher;
import juno.http.convert.ResponseBodyConverter;

public class HttpTask<T> extends AbstractTask<T> {
    public final HttpTransport stack;
    public final HttpRequest request;
    public final ResponseBodyConverter<T> converter;
    protected OnInterceptor interceptor;
    protected boolean throwOnHttpError = false;

    /**
     * Inyección de Dependencias: Dispatcher, HttpClient, ResponseBodyConvert
     */
    public HttpTask(
        TaskDispatcher dispatcher, HttpTransport stack, HttpRequest request, ResponseBodyConverter<T> converter
    ) {
        super(dispatcher);
        this.stack = stack;
        this.request = request;
        this.converter = converter;
    }
    
    private HttpResponse execute(HttpRequest request) throws Exception {
        if (interceptor == null) {
            return stack.send(request);
        } 
        return interceptor.intercept(request, stack);
    }

    @Override
    public T call() throws Exception {
        HttpResponse response = null;
        try {
          response = execute(request);
          if (throwOnHttpError && !response.isSuccessful()) {
            throw HttpException.from(response);
          }
          return converter.convert(response);

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
    
    public HttpTask<T> setInterceptor(OnInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public HttpTask<T> setThrowOnHttpError(boolean throwOnHttpError) {
        this.throwOnHttpError = throwOnHttpError;
        return this;
    }
}
