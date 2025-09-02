package juno.http;

public interface OnInterceptor {

    HttpResponse intercept(HttpRequest request, HttpTransport transport) throws Exception;
}
