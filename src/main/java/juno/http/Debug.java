package juno.http;

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
        //"multipart/form-data",
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
     * {@code [timestamp] INFO: METHOD url status elapsed ms - size}
     * y, si hubo body de request legible, una segunda línea con {@code reqBody}.
     */
    public static void log(HttpRequest request, HttpResponse response, String reqBody, long elapsedMs) {
        if (!isDebug) {
            return;
        }

        final long size = response.getContentLength();
        final StringBuilder line = new StringBuilder()
                .append('[').append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())).append(']')
                .append(" INFO: ")
                .append(request.getMethod()).append(' ')
                .append(request.urlAndParams())
                .append(' ').append(response.code)
                .append(' ').append(elapsedMs).append(" ms")
                .append(" - ").append(size >= 0 ? String.valueOf(size) : "-")
                .append('\n');

        if (reqBody != null && !reqBody.isEmpty()) {
            line.append("    reqBody: ").append(reqBody).append('\n');
        }

        System.out.print(line);
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
