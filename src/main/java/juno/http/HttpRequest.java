package juno.http;

import juno.http.convert.ResponseBodyConverter;

public class HttpRequest {

    /**
     * Tiempo limite de espera por default.
     */
    public static final int DEFAULT_TIMEOUT = 2500 * 2 * 2;

    /**
     * Metodo por default.
     */
    public static final String DEFAULT_METHOD = "GET";

    /**
     * Metodo de la request: OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE.
     */
    private String method = DEFAULT_METHOD;

    /**
     * Url de nuestra request.
     */
    private HttpUrl url;

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


    public HttpRequest() {
    } 
   
    public HttpRequest(String method, HttpUrl url, Headers headers, RequestBody body) {
        this.method = method.toUpperCase();
        this.url = url;
        this.headers = headers;
        this.body = body;
    }
    public HttpRequest(String method, HttpUrl url, RequestBody body) {
        this(method, url, null, body);
    }
    public HttpRequest(String method, HttpUrl url, Headers headers) {
        this(method, url, headers, null);
    }
    public HttpRequest(String method, HttpUrl url) {
        this(method, url, null, null);
    }
    
    public HttpRequest(String method, String url, Headers headers, RequestBody body) {
        this(method, new HttpUrl(url), headers, body);
    }
    public HttpRequest(String method, String url, RequestBody body) {
        this(method, new HttpUrl(url), null, body);
    }
    public HttpRequest(String method, String url, Headers headers) {
        this(method, new HttpUrl(url), headers, null);
    }
    public HttpRequest(String method, String url) {
        this(method, new HttpUrl(url), null, null);
    }


    public String getMethod() {
        return method;
    }

    public HttpRequest setMethod(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    public HttpUrl getUrl() {
        return url;
    }

    public HttpRequest setUrl(HttpUrl url) {
        this.url = url;
        return this;
    }

    public HttpRequest setUrl(String url) {
        return setUrl(new HttpUrl(url));
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
    
    public HttpResponse execute(HttpStack stack) throws Exception {
        return stack.execute(this);
    }
    
    public HttpResponse execute() throws Exception {
        return execute(HttpClient.getInstance());
    }

    public <V> V execute(HttpClient client, ResponseBodyConverter<V> convert) throws Exception {
        return client.execute(this, convert);
    }
    
    public <V> V execute(ResponseBodyConverter<V> convert) throws Exception {
        return execute(HttpClient.getInstance(), convert);
    }

    public <V> V execute(HttpClient client, Class<V> convert) throws Exception {
        return client.execute(this, convert);
    }
    
    public <V> V execute(Class<V> convert) throws Exception {
        return execute(HttpClient.getInstance(), convert);
    }

    @Override
    public String toString() {
        return "HttpRequest{" + "method=" + method + ", url=" + url + ", headers=" + headers + ", body=" + body + ", tag=" + tag + ", timeoutMs=" + timeoutMs + '}';
    }
    
//  public static void main(String[] args) {
//    FormBody body = new FormBody()
//            .add("fields",  "status,message,query,country,city");
//
//    HttpRequest request = new HttpRequest(
//            "GET", "http://ip-api.com", body);
//    
//    System.out.println(request.urlAndParams());
//  }
}
