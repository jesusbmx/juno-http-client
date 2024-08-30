package juno.http;

import java.io.BufferedOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import juno.io.IOUtils;

public class HttpURLConnectionStack implements HttpStack {

    /**
     * Abre una conexión HTTP a intenert apartir de una petición.
     *
     * @param src conección.
     *
     * @return una conexión HTTP abierta.
     *
     * @throws java.io.IOException
     */
    public HttpURLConnection open(URL src) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) src.openConnection();
        // Workaround for the M release HttpURLConnection not observing the
        // HttpURLConnection.setFollowRedirects() property.
        // https://code.google.com/p/android/issues/detail?id=194495
        conn.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
        return conn;
    }

    /**
     * Abre una conexión HTTP a intenert apartir de una petición.
     *
     * @param request petición a intenert.
     *
     * @return una conexión HTTP abierta.
     *
     * @throws java.io.IOException
     */
    public HttpURLConnection open(HttpRequest request) throws IOException {
        final String url = request.urlAndParams();
        final URL src = new URL(url);
        final HttpURLConnection conn = open(src);
        conn.setConnectTimeout(request.getTimeoutMs());
        conn.setReadTimeout(request.getTimeoutMs());
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setRequestMethod(request.getMethod());
        return conn;
    }

    /**
     * Manda una lista de encabezados adicionales de HTTP para esta petición.
     *
     * @param conn HTTP
     * @param request peticion
     *
     * @throws IOException
     */
    public void writeHeaders(HttpURLConnection conn, HttpRequest request)
            throws IOException {
        final Headers headers = request.getHeaders();
        if (headers != null) {
            for (int i = 0, size = headers.size(); i < size; i++) {
                conn.addRequestProperty(
                        headers.getName(i), headers.getValue(i));
            }
        }
    }

    /**
     * Escribe el cuerpo de de esta petición.
     *
     * @param conn HTTP
     * @param request peticion
     *
     * @throws IOException
     */
    public void writeBody(HttpURLConnection conn, HttpRequest request)
            throws IOException {
        final RequestBody requestBody = request.getBody();

        if (request.requiresRequestBody() && requestBody != null) {
            final String contentType = requestBody.contentType();

            // Setup connection:
            conn.setDoOutput(true);
            conn.addRequestProperty(Headers.CONTENT_TYPE, contentType);

            // Length:
            final long contentLength = requestBody.contentLength();
            setFixedLengthStreamingMode(conn, contentLength);

            // Write params:
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(conn.getOutputStream());
                Debug.debug(request, requestBody, contentType, contentLength);
                requestBody.writeTo(bos);

            } finally {
                IOUtils.closeQuietly(bos);
            }

        } else {
            Debug.debug(request);
        }
    }

    public void setFixedLengthStreamingMode(
            HttpURLConnection conn, long contentLength) throws IOException {

        conn.setFixedLengthStreamingMode((int) contentLength);
    }

    /**
     * Obtiene la respuesta del servidor
     *
     * @param conn
     * @param request
     * @return
     * @throws IOException
     */
    public HttpResponse getResponse(HttpURLConnection conn, HttpRequest request)
            throws IOException {
        final int responseCode = conn.getResponseCode();
        final String status = conn.getResponseMessage();

        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could not be retrieved.
            // Signal to the caller that something was wrong with the connection.
            throw new IOException("Failed to retrieve response code from HttpURLConnection.");
        }

        // HttpEntity populated with data from <code>connection</code>.
        InputStream in;
        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            in = conn.getErrorStream();
            if (in == null) {
                throw new IOException("Failed to retrieve response body. Response code: " + responseCode + ", Status: " + status, e);
            }
        }
        
        final InputStream content = new UrlConnectionInputStream(conn, in);

        // Haeders
        final Headers headers = new Headers()
                .addHeadersMapList(conn.getHeaderFields());

        // Create Http Response
        final HttpResponse response = new HttpResponse(
                responseCode, headers, content);

        Debug.debug(request, response);

        return response;
    }

    /**
     * Wrapper for a {@link HttpURLConnection}'s InputStream which disconnects
     * the connection on stream close.
     */
    static class UrlConnectionInputStream extends FilterInputStream {

        private final HttpURLConnection mConnection;

        UrlConnectionInputStream(HttpURLConnection connection, InputStream in) {
            super(in);
            mConnection = connection;
        }

        @Override
        public void close() throws IOException {
            super.close();
            mConnection.disconnect();
        }
    }

    /**
     * Ejecuta una petición.
     *
     * @param request petición a ejecutar
     *
     * @return el resultado de la petición realizada
     *
     * @throws java.io.IOException
     */
    @Override
    public HttpResponse execute(HttpRequest request) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = open(request);
            writeHeaders(conn, request);
            writeBody(conn, request);
            return getResponse(conn, request);

        } catch (UnknownHostException e) {
            if (conn != null) {
                conn.disconnect();
            }
            String baseUrl = request.getUrl().baseUrl;
            throw new IOException("Network error: Unable to resolve host for URL: " + baseUrl + ". Check your internet connection.", e);
        
        } catch (IOException e) {
            if (conn != null) {
                conn.disconnect();
            }
            throw e;
        }
    }

}
