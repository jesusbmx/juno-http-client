package juno.http;

public interface OnInterceptor {

    HttpResponse intercept(HttpRequest request, HttpStack httpStack) throws Exception;
}
