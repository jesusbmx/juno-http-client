package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpResult<T> {

    public final int code;
    public final Headers headers;
    public final T body;

    public HttpResult(int code, Headers headers, T body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
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
            T body = inner.convert(response);
            return new HttpResult<>(code, headers, body);
        }
    }
}
