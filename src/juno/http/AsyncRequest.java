package juno.http;

import juno.concurrent.AbstractAsync;
import juno.concurrent.Dispatcher;
import juno.http.convert.ResponseBodyConvert;

public class AsyncRequest<T> extends AbstractAsync<Response<T>> {
    public final HttpExecutor executor;
    public final HttpRequest request;
    public final ResponseBodyConvert<T> convert;
    protected OnInterceptor<T> interceptor;

    /**
     * Inyección de Dependencias: Dispatcher, HttpClient, ResponseBodyConvert
     */
    public AsyncRequest(
        Dispatcher dispatcher, HttpExecutor executor, HttpRequest request, ResponseBodyConvert<T> convert
    ) {
        super(dispatcher);
        this.executor = executor;
        this.request = request;
        this.convert = convert;
    }

    @Override
    public Response<T> call() throws Exception {
        if (interceptor == null)
            return executor.execute(request, convert);
        
        return interceptor.intercept(executor, request, convert);
    }

    public OnInterceptor<T> getInterceptor() {
        return interceptor;
    }
    
    public AsyncRequest<T> setInterceptor(OnInterceptor<T> interceptor) {
        this.interceptor = interceptor;
        return this;
    }
    
    public interface OnInterceptor<V> {
        Response<V> intercept(HttpExecutor executor, HttpRequest request, ResponseBodyConvert<V> convert) throws Exception;
    }
}
