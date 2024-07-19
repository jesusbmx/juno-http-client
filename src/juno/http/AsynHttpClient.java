package juno.http;

import juno.http.convert.ResponseBodyConvert;

public class AsynHttpClient extends HttpClient {

    public AsynHttpClient() {
        super();
    }

    public AsynHttpClient(HttpStack httpStack) {
        super(httpStack);
    }

    // Unified method to create AsyncHttpRequest
    private <V> AsyncHttpRequest<V> createRequest(String method, String url, Headers headers, RequestBody body, ResponseBodyConvert<V> convert) {
        return createAsync(new HttpRequest(method, url, headers, body), convert);
    }

    private <V> AsyncHttpRequest<V> createRequest(String method, String url, Headers headers, RequestBody body, Class<V> convert) {
        return createAsync(new HttpRequest(method, url, headers, body), convert);
    }

    // GET Methods
    public <V> AsyncHttpRequest<V> get(String url, Headers headers, RequestBody body, ResponseBodyConvert<V> convert) {
        return createRequest("GET", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, RequestBody body, ResponseBodyConvert<V> convert) {
        return get(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, Headers headers, ResponseBodyConvert<V> convert) {
        return get(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, ResponseBodyConvert<V> convert) {
        return get(url, null, null, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, Headers headers, RequestBody body, Class<V> convert) {
        return createRequest("GET", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, RequestBody body, Class<V> convert) {
        return get(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, Headers headers, Class<V> convert) {
        return get(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> get(String url, Class<V> convert) {
        return get(url, null, null, convert);
    }

    public AsyncHttpRequest<HttpResponse> get(String url, Headers headers, RequestBody body) {
        return createRequest("GET", url, headers, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> get(String url, RequestBody body) {
        return get(url, null, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> get(String url, Headers headers) {
        return get(url, headers, null, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> get(String url) {
        return get(url, null, null, HttpResponse.class);
    }

    // POST Methods
    public <V> AsyncHttpRequest<V> post(String url, Headers headers, RequestBody body, ResponseBodyConvert<V> convert) {
        return createRequest("POST", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, RequestBody body, ResponseBodyConvert<V> convert) {
        return post(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, Headers headers, ResponseBodyConvert<V> convert) {
        return post(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, ResponseBodyConvert<V> convert) {
        return post(url, null, null, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, Headers headers, RequestBody body, Class<V> convert) {
        return createRequest("POST", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, RequestBody body, Class<V> convert) {
        return post(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, Headers headers, Class<V> convert) {
        return post(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> post(String url, Class<V> convert) {
        return post(url, null, null, convert);
    }

    public AsyncHttpRequest<HttpResponse> post(String url, Headers headers, RequestBody body) {
        return createRequest("POST", url, headers, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> post(String url, RequestBody body) {
        return post(url, null, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> post(String url, Headers headers) {
        return post(url, headers, null, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> post(String url) {
        return post(url, null, null, HttpResponse.class);
    }

    // PUT Methods
    public <V> AsyncHttpRequest<V> put(String url, Headers headers, RequestBody body, ResponseBodyConvert<V> convert) {
        return createRequest("PUT", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, RequestBody body, ResponseBodyConvert<V> convert) {
        return put(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, Headers headers, ResponseBodyConvert<V> convert) {
        return put(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, ResponseBodyConvert<V> convert) {
        return put(url, null, null, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, Headers headers, RequestBody body, Class<V> convert) {
        return createRequest("PUT", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, RequestBody body, Class<V> convert) {
        return put(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, Headers headers, Class<V> convert) {
        return put(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> put(String url, Class<V> convert) {
        return put(url, null, null, convert);
    }

    public AsyncHttpRequest<HttpResponse> put(String url, Headers headers, RequestBody body) {
        return createRequest("PUT", url, headers, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> put(String url, RequestBody body) {
        return put(url, null, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> put(String url, Headers headers) {
        return put(url, headers, null, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> put(String url) {
        return put(url, null, null, HttpResponse.class);
    }

    // DELETE Methods
    public <V> AsyncHttpRequest<V> delete(String url, Headers headers, RequestBody body, ResponseBodyConvert<V> convert) {
        return createRequest("DELETE", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, RequestBody body, ResponseBodyConvert<V> convert) {
        return delete(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, Headers headers, ResponseBodyConvert<V> convert) {
        return delete(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, ResponseBodyConvert<V> convert) {
        return delete(url, null, null, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, Headers headers, RequestBody body, Class<V> convert) {
        return createRequest("DELETE", url, headers, body, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, RequestBody body, Class<V> convert) {
        return delete(url, null, body, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, Headers headers, Class<V> convert) {
        return delete(url, headers, null, convert);
    }

    public <V> AsyncHttpRequest<V> delete(String url, Class<V> convert) {
        return delete(url, null, null, convert);
    }

    public AsyncHttpRequest<HttpResponse> delete(String url, Headers headers, RequestBody body) {
        return createRequest("DELETE", url, headers, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> delete(String url, RequestBody body) {
        return delete(url, null, body, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> delete(String url, Headers headers) {
        return delete(url, headers, null, HttpResponse.class);
    }

    public AsyncHttpRequest<HttpResponse> delete(String url) {
        return delete(url, null, null, HttpResponse.class);
    }

    public static void main(String[] args) throws Exception {
        AsynHttpClient client = new AsynHttpClient();

        AsyncHttpRequest<HttpResponse> async = client.get(
                "https://postman-echo.com/get");

        System.out.println(async.await());
    }
}
