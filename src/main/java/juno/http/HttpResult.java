package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpResult<T> {

    public final int code;
    public final Headers headers;
    public final T data;

    public HttpResult(int code, Headers headers, T data) {
        this.code = code;
        this.headers = headers;
        this.data = data;
    }

    @Override
    public String toString() {
        return "HttpResult{code=" + code + ", headers=" + headers + ", data=" + data + '}';
    }

    /**
     * Comportamiento "axios": lanza {@link HttpException} si la respuesta no
     * fue 2xx, sin intentar convertir el body de error; si fue exitosa
     * devuelve un {@link HttpResult} (code/headers/data). Con {@code throwOnHttpError}
     * en {@code false}, convierte el body sin importar el status (como antes).
     */
    public static class Converter<T> implements ResponseBodyConverter<HttpResult<T>> {
        private final ResponseBodyConverter<T> inner;
        private final boolean throwOnHttpError;

        public Converter(ResponseBodyConverter<T> inner) {
            this(inner, true);
        }

        public Converter(ResponseBodyConverter<T> inner, boolean throwOnHttpError) {
            this.inner = inner;
            this.throwOnHttpError = throwOnHttpError;
        }

        @Override
        public HttpResult<T> convert(HttpResponse response) throws Exception {
            if (throwOnHttpError && !response.ok) {
                throw HttpException.from(response);
            }

            T data = inner.convert(response);

            return new HttpResult<>(response.code, response.headers, data);
        }
    }
}
