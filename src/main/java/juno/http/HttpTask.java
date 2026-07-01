package juno.http;

import juno.concurrent.AbstractTask;
import juno.concurrent.TaskDispatcher;
import juno.http.convert.ResponseBodyConverter;

/**
 * Llamada diferida: {@code HttpTask<V>} siempre resuelve un {@link HttpResult}{@code <V>}
 * (nunca el {@code V} crudo), igual que {@link HttpClient#execute} en su versión síncrona.
 */
public class HttpTask<V> extends AbstractTask<HttpResult<V>> {
    public final HttpTransport stack;
    public final HttpRequest request;
    public final ResponseBodyConverter<V> converter;
    protected OnInterceptor interceptor;

    /**
     * Inyección de Dependencias: Dispatcher, HttpClient, ResponseBodyConvert
     */
    public HttpTask(
        TaskDispatcher dispatcher, HttpTransport stack, HttpRequest request, ResponseBodyConverter<V> converter
    ) {
        super(dispatcher);
        this.stack = stack;
        this.request = request;
        this.converter = converter;
    }

    private HttpResponse getResponse() throws Exception {
        if (interceptor == null) {
            return stack.send(request);
        }
        return interceptor.intercept(request, stack);
    }

    @Override
    public HttpResult<V> call() throws Exception {
        HttpResponse response = getResponse();
        try {
          return new HttpResult.Converter<>(converter).convert(response);

        } finally {
          response.close();
        }
    }

    public OnInterceptor getInterceptor() {
        return interceptor;
    }

    public HttpTask<V> setInterceptor(OnInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }
}
