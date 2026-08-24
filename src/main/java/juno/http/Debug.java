package juno.http;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import juno.util.Strings;

public final class Debug {

    private static final List<String> READABLE_CONTENT_TYPES = new ArrayList<String>(Arrays.asList(
        "application/json",
        "application/xml",
        "application/x-www-form-urlencoded",
        "multipart/form-data",
        "text/plain",
        "text/html"
    ));

    private static boolean isDebug;

    private Debug() {

    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean isDebug) {
        Debug.isDebug = isDebug;
    }

    public static void debug(String tag, Object... message) {
        if (isDebug) {
            final StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("[Debug] ").append(tag).append(" -> ");
            debugInfo.append(Strings.join(message, " "));
            debugInfo.append("\n");
            System.out.print(debugInfo);
        }
    }

    /**
     * Registra una línea por petición, una vez que ya se tiene la respuesta:
     * {@code [I] [HH:mm:ss.SSS]  [METHOD] url status elapsedms - size}
     * y, si el body de la request es legible, una segunda línea con {@code reqBody}.
     */
    public static void log(HttpRequest request, HttpResponse response, long elapsedMs) {
        if (!isDebug) {
            return;
        }

        final long size = response.getContentLength();
        final StringBuilder line = new StringBuilder()
                .append("[I] [").append(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date())).append(']')
                .append("  [").append(request.getMethod()).append("] ")
                .append(request.urlAndParams())
                .append(' ').append(response.code)
                .append(' ').append(elapsedMs).append("ms")
                .append(" - ").append(size >= 0 ? String.valueOf(size) : "-")
                .append('\n');

        final String reqBody = describeBody(request.getBody());
        if (reqBody != null && !reqBody.isEmpty()) {
            line.append("    reqBody: ").append(reqBody).append('\n');
        }

        System.out.print(line);
    }

    /**
     * Describe el body de una request como texto para debug: {@code null} si
     * no hay body o su content-type no es legible. Un {@link MultipartBody}
     * siempre se describe (su propio {@code toString()} omite las partes binarias).
     */
    public static String describeBody(RequestBody body) {
        if (body == null) {
            return null;
        }
        if (body instanceof MultipartBody) {
            return body.toString();
        }

        final String contentType = body.contentType();
        if (contentType == null || !isReadableContentType(contentType)) {
            return null;
        }

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            body.writeTo(baos);
            final Charset charset = Headers.getCharsetFromContentType(
                    contentType, RequestBody.DEFAULT_ENCODING);
            return new String(baos.toByteArray(), charset);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isReadableContentType(String contentType) {
        for (String legibleContentType : READABLE_CONTENT_TYPES) {
            if (contentType.startsWith(legibleContentType)) {
                return true;
            }
        }
        return false;
    }
}
