package juno.http;

import juno.concurrent.Dispatcher;
import juno.http.convert.ResponseBodyConvert;
import juno.concurrent.AbstractAsync;

public class AsyncRequest<T> extends AbstractAsync<T> {
    public final HttpClient client;
    public final HttpRequest request;
    public final ResponseBodyConvert<T> convert;
    protected OnInterceptor<T> interceptor;

    /**
     * Inyección de Dependencias: Dispatcher, HttpClient, ResponseBodyConvert
     */
    public AsyncRequest(
        Dispatcher dispatcher, HttpClient client, HttpRequest request, ResponseBodyConvert<T> convert
    ) {
        super(dispatcher);
        this.client = client;
        this.request = request;
        this.convert = convert;
    }
    
    public AsyncRequest(
        HttpClient client, HttpRequest request, ResponseBodyConvert<T> convert
    ) {
        this(client.getDispatcher(), client, request, convert);
    }

    @Override
    public T call() throws Exception {
        if (interceptor == null)
            return client.execute(request, convert);
        
        return interceptor.intercept(client, request, convert);
    }

    public OnInterceptor<T> getInterceptor() {
        return interceptor;
    }
    
    public AsyncRequest<T> setInterceptor(OnInterceptor<T> interceptor) {
        this.interceptor = interceptor;
        return this;
    }
    
    public interface OnInterceptor<V> {
        V intercept(HttpClient client, HttpRequest request, ResponseBodyConvert<V> convert) throws Exception;
    }
}
