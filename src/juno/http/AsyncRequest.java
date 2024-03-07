package juno.http;

import juno.concurrent.Dispatcher;
import juno.http.convert.ResponseBodyConvert;
import juno.concurrent.AbstractAsync;

public class AsyncRequest<T> extends AbstractAsync<T> {
    
    public final HttpRequest request;
    public final ResponseBodyConvert<T> convert;
    protected OnInterceptor<T> interceptor;

    public AsyncRequest(
        Dispatcher dispatcher, HttpRequest request, ResponseBodyConvert<T> convert
    ) {
        super(dispatcher);
        this.request = request;
        this.convert = convert;
    }

    @Override
    public T call() throws Exception {
        if (interceptor == null)
            return request.execute(convert);
        
        return interceptor.intercept(request, convert);
    }

    public OnInterceptor<T> getInterceptor() {
        return interceptor;
    }
    
    public AsyncRequest<T> setInterceptor(OnInterceptor<T> interceptor) {
        this.interceptor = interceptor;
        return this;
    }
    
    public interface OnInterceptor<V> {
        V intercept(HttpRequest request, ResponseBodyConvert<V> convert) throws Exception;
    }
}
