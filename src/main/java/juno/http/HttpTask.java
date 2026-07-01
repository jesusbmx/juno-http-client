package juno.http;

import juno.concurrent.AbstractTask;
import juno.concurrent.Callback;
import juno.concurrent.CallbackAdapter;
import juno.concurrent.OnError;
import juno.concurrent.OnResponse;
import juno.concurrent.Task;
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

    /**
     * Versión diferida de {@link HttpClient#send}: en vez de {@link HttpResult}{@code <V>},
     * resuelve directamente {@code V} (o lanza {@link HttpException}/la excepción que
     * haya ocurrido) — sin duplicar la ejecución, solo desenvuelve {@link HttpResult#data}.
     */
    public Task<V> toTask() {
        final HttpTask<V> self = this;
        return new Task<V>() {
            @Override
            public void async(Callback<V> callback) {
                self.async(unwrap(callback));
            }

            @Override
            public void async(OnResponse<V> onResponse, OnError onError) {
                async(new CallbackAdapter<>(onResponse, onError));
            }

            @Override
            public V sync() throws Exception {
                return self.sync().data;
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return self.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return self.isCancelled();
            }

            @Override
            public boolean isDone() {
                return self.isDone();
            }

            @Override
            public boolean isAlive() {
                return self.isAlive();
            }
        };
    }

    private Callback<HttpResult<V>> unwrap(final Callback<V> callback) {
        return new Callback<HttpResult<V>>() {
            @Override
            public void onResponse(HttpResult<V> result) throws Exception {
                callback.onResponse(result.data);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        };
    }
}
