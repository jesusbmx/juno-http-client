package juno.http.convert.json;

import juno.http.HttpResponse;

public class JsonConversionException extends Exception {

    public final int statusCode;
    public final String contentType;
    public final String bodyPreview;

    public JsonConversionException(
            String message,
            HttpResponse response,
            String body,
            Throwable cause
    ) {
        super(buildMessage(message, response, body), cause);

        this.statusCode = response.code;
        this.contentType = response.getContentType();
        this.bodyPreview = truncate(body);
    }

    private static String buildMessage(
            String message,
            HttpResponse response,
            String body
    ) {
        return message
                + "\nStatus: " + response.code
                + "\nContent-Type: " + response.getContentType()
                + "\nBody: " + truncate(body);
    }

    private static String truncate(String body) {
        if (body == null)
            return "null";

        body = body.replace("\n", "\\n");

        int max = 500;

        return body.length() <= max
                ? body
                : body.substring(0, max) + "...";
    }
}