package juno.http;

public class HttpException extends Exception {

    public final int code;

    public HttpException(int code, String body) {
        super("HTTP " + code + (body == null || body.isEmpty() ? "" : ": " + body));
        this.code = code;
    }

    public static HttpException from(HttpResponse response) {
        int code = response.code;
        String body = "";
        try {
            body = response.readString();
        } catch (Exception ignored) {
        }
        return new HttpException(code, body);
    }

    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
}
