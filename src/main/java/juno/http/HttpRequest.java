package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpRequest {

    /**
     * Tiempo limite de espera por default.
     */
    public static final int DEFAULT_TIMEOUT = 2500 * 2 * 2;

    /**
     * Metodo de la request: OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE.
     */
    private final String method;

    /**
     * Url de nuestra request.
     */
    private final HttpUrl url;

    /**
     * Lista de encabezados adicionales de HTTP para esta peticion.
     */
    private Headers headers;

    /**
     * Parametros de nuestra request.
     */
    private RequestBody body;

    /**
     * Etiqueta para identificar la request.
     */
    private Object tag = HttpRequest.class;

    /**
     * Tiempo limite de espera.
     */
    private int timeoutMs = DEFAULT_TIMEOUT;

    public HttpRequest(String method, HttpUrl url, RequestBody body) {
        this.method = method.toUpperCase();
        this.url = url;
        this.body = body;
    }
    
    public HttpRequest(String method, HttpUrl url) {
        this(method, url, null);
    }
    
    public HttpRequest(String method, String url, RequestBody body) {
        this(method, new HttpUrl(url), body);
    }
    
    public HttpRequest(String method, String url) {
        this(method, new HttpUrl(url), null);
    }
    
    // Métodos de fábrica para solicitudes HTTP
    
    public static HttpRequest get(HttpUrl url) {
        return new HttpRequest("GET", url);
    }

    public static HttpRequest get(String url) {
        return new HttpRequest("GET", url);
    }

    public static HttpRequest post(HttpUrl url, RequestBody body) {
        return new HttpRequest("POST", url, body);
    }

    public static HttpRequest post(String url, RequestBody body) {
        return new HttpRequest("POST",  url, body);
    }

    public static HttpRequest put(HttpUrl url, RequestBody body) {
        return new HttpRequest("PUT", url, body);
    }

    public static HttpRequest put(String url, RequestBody body) {
        return new HttpRequest("PUT", url, body);
    }

    public static HttpRequest delete(HttpUrl url) {
        return new HttpRequest("DELETE", url);
    }

    public static HttpRequest delete(String url) {
        return new HttpRequest("DELETE", url);
    }

    public static HttpRequest patch(HttpUrl url, RequestBody body) {
        return new HttpRequest("PATCH", url, body);
    }

    public static HttpRequest patch(String url, RequestBody body) {
        return new HttpRequest("PATCH", url, body);
    }

    public static HttpRequest head(HttpUrl url) {
        return new HttpRequest("HEAD", url);
    }

    public static HttpRequest head(String url) {
        return new HttpRequest("HEAD", url);
    }

    public static HttpRequest options(HttpUrl url) {
        return new HttpRequest("OPTIONS", url);
    }

    public static HttpRequest options(String url) {
        return new HttpRequest("OPTIONS", url);
    }

    public static HttpRequest trace(HttpUrl url) {
        return new HttpRequest("TRACE", url);
    }

    public static HttpRequest trace(String url) {
        return new HttpRequest("TRACE", url);
    }
    
    // Getters y Setters

    public String getMethod() {
        return method;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public Headers getHeaders() {
        return headers;
    }

    public HttpRequest setHeaders(Headers headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequest addHeader(String name, String value) {
        if (headers == null) {
            headers = new Headers();
        }
        headers.add(name, value);
        return this;
    }

    public RequestBody getBody() {
        return body;
    }

    public HttpRequest setBody(RequestBody body) {
//        if (body != null) {
//            this.headers.set(Headers.CONTENT_TYPE, body.contentType());
//        } else {
//            this.headers.remove(Headers.CONTENT_TYPE);
//        }
        this.body = body;
        return this;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public HttpRequest setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public HttpRequest setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public boolean requiresRequestBody() {
        return method.equals("POST") || method.equals("PUT") || method.equals("PATCH");
    }

    public String urlAndParams() {
        if (!requiresRequestBody()) {
            if (body != null) {
                if (body instanceof FormBody) {
                    return url.toString((FormBody) body);
                }
            }
        }

        return url.toString();
    }
    
    public HttpResponse send(HttpTransport stack) throws Exception {
        return stack.send(this);
    }

    public HttpResponse send() throws Exception {
        return send(HttpClient.getInstance());
    }

    public <V> HttpResult<V> execute(HttpExecutor executor, ResponseBodyConverter<V> convert) throws Exception {
        return executor.execute(this, convert);
    }

    public <V> HttpResult<V> execute(ResponseBodyConverter<V> convert) throws Exception {
        return execute(HttpClient.getInstance(), convert);
    }

    public <V> HttpResult<V> execute(HttpClient client, Class<V> convert) throws Exception {
        return client.execute(this, convert);
    }

    public <V> HttpResult<V> execute(Class<V> convert) throws Exception {
        return execute(HttpClient.getInstance(), convert);
    }

    @Override
    public String toString() {
        return "HttpRequest{" + "method=" + method + ", url=" + url + ", headers=" + headers + ", body=" + body + ", tag=" + tag + ", timeoutMs=" + timeoutMs + '}';
    }
}
