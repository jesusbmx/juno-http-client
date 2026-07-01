package juno.http;

public class HttpException extends Exception {

    public final int code;
    public final Headers headers;
    public final String data;

    public HttpException(int code, Headers headers, String data) {
        super("HTTP " + code + (data == null || data.isEmpty() ? "" : ": " + data));
        this.code = code;
        this.headers = headers;
        this.data = data;
    }

    /**
     * Igual que Retrofit: el body del error queda crudo en {@link #data}, sin
     * intentar convertirlo con el converter de la respuesta exitosa — el error
     * no necesariamente tiene la misma forma (puede ser HTML, texto plano, etc.).
     * Si necesitas parsearlo, hazlo explícitamente con el converter que corresponda.
     */
    public static HttpException from(HttpResponse response) {
        int code = response.code;
        String data = "";
        try {
            data = response.readString();
        } catch (Exception ignored) {
        }
        return new HttpException(code, response.headers, data);
    }

    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    public boolean isServerError() {
        return code >= 500 && code < 600;
    }
}
