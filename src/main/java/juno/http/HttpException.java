package juno.http;

public class HttpException extends Exception {

    public final int code;
    public final Headers headers;
    public final String body;

    public HttpException(int code, Headers headers, String body) {
        super("HTTP " + code + (body == null || body.isEmpty() ? "" : ": " + body));
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public static HttpException from(HttpResponse response) {
        int code = response.code;
        String body = "";
        try {
            body = response.readString();
        } catch (Exception ignored) {
        }
        return new HttpException(code, response.headers, body);
    }

    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
}
