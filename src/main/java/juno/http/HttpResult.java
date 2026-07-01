package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpResult<T> {

    public final int code;
    public final Headers headers;
    public final T body;
    public final HttpException error;

    public HttpResult(int code, Headers headers, T body, HttpException error) {
        this.code = code;
        this.headers = headers;
        this.body = body;
        this.error = error;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

    /**
     * Comportamiento "axios": desenvuelve el resultado o lanza el error HTTP.
     */
    public T getOrThrow() throws HttpException {
        if (!isSuccessful()) {
            throw error;
        }
        return body;
    }

    @Override
    public String toString() {
        return "HttpResult{code=" + code + ", headers=" + headers + ", body=" + body + '}';
    }

    public static class Converter<T> implements ResponseBodyConverter<HttpResult<T>> {
        private final ResponseBodyConverter<T> inner;

        public Converter(ResponseBodyConverter<T> inner) {
            this.inner = inner;
        }

        @Override
        public HttpResult<T> convert(HttpResponse response) throws Exception {
            int code = response.code;
            Headers headers = response.headers;
            T body = null;
            HttpException error = null;
            
            if (response.isSuccessful()) {
                body = inner.convert(response);
            } else {
                error = HttpException.from(response);
                response.close();
            }
            
            return new HttpResult<>(code, headers, body, error);
        }
    }
}
