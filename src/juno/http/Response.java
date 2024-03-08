package juno.http;

public class Response<T> {
    
    public final T result;
    public final ResponseBody body;

    public Response(T result, ResponseBody body) {
        this.result = result;
        this.body = body;
    }
}
