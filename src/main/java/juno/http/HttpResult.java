package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpResult<T> {

    public final int code;
    public final Headers headers;
    public final T data;

    public HttpResult(int code, Headers headers, T body) {
        this.code = code;
        this.headers = headers;
        this.data = body;
    }

    @Override
    public String toString() {
        return "HttpResult{code=" + code + ", headers=" + headers + ", body=" + data + '}';
    }

    /**
     * Comportamiento "axios": lanza {@link HttpException} si la respuesta no
     * fue 2xx, sin intentar convertir el body de error; si fue exitosa
     * devuelve un {@link HttpResult} (code/headers/body).
     */
    public static class Converter<T> implements ResponseBodyConverter<HttpResult<T>> {
        private final ResponseBodyConverter<T> inner;

        public Converter(ResponseBodyConverter<T> inner) {
            this.inner = inner;
        }

        @Override
        public HttpResult<T> convert(HttpResponse response) throws Exception {
            if (!response.ok) {
                throw HttpException.from(response);
            }

            int code = response.code;
            Headers headers = response.headers;
            T body = inner.convert(response);

            return new HttpResult<>(code, headers, body);
        }
    }
}
