package juno.http;

public interface OnInterceptor {

    HttpResponse intercept(HttpRequest request, HttpStack stack) throws Exception;
}
