package juno.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class HttpUrl {
    public static final Charset DEFAULT_ENCODING = Charset.forName("utf-8");

    Charset charset = DEFAULT_ENCODING;
    final String baseUrl;
    final List<String> paths = new ArrayList<String>();
    final FormBody parameters = new FormBody();

    public HttpUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    
    /**
     * "http://ip-api.com/json/{ip}"
     *
     * @param chrst
     * @param separator '/'
     * @return "http://ip-api.com/json/24.48.0.1"
     * @throws java.io.IOException
     */
    public String encodedUrl(Charset chrst, char separator) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        
        if (!paths.isEmpty()) {
            
            if (sb.charAt(sb.length() - 1) != separator) {
                sb.append(separator);
            }
        
            for (int i = 0; i < paths.size(); i++) {
                if (i > 0 && sb.charAt(sb.length() - 1) != separator) {
                    sb.append(separator);
                }
                final String value = paths.get(i);
                final String valueEncode = URLEncoder.encode(value, chrst.name());
                sb.append(valueEncode);
            }
        }
        
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////

    public HttpUrl addPath(String path) {
        paths.add(path);
        return this;
    }

    public String getPath(int index) {
        return paths.get(index);
    }

    public int getPathSize() {
        return paths.size();
    }
    
    ////////////////////////////////////////////////////////////////////////////

    public HttpUrl addQueryParameter(String key, Object value) {
        parameters.add(key, value);
        return this;
    }

    public HttpUrl addFormBody(FormBody body) {
        for (int i = 0; i < body.size(); i++) {
            addQueryParameter(body.key(i), body.value(i));
        }
        return this;
    }

    public String getQueryParameterName(int index) {
        return parameters.key(index);
    }

    public Object getQueryParameterValue(int index) {
        return parameters.value(index);
    }

    public int getQueryParameterSize() {
        return parameters.size();
    }
    
    ////////////////////////////////////////////////////////////////////////////

    public String toString(FormBody body) {
        try {
            final String encodedUrl = encodedUrl(charset, '/');
            final String encodedParams = body.encodedUrlParams(charset);

            return new StringBuilder(encodedUrl.length() + 1 + encodedParams.length())
                    .append(encodedUrl)
                    .append(encodedUrl.endsWith("?") ? '&' : '?')
                    .append(encodedParams)
                    .toString();

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return toString(parameters);
    }

//    public static void main(String[] args) {
//        HttpUrl url = new HttpUrl("http://ip-api.com/")
//                .addPath("json")
//                .addPath("24.48.0.1")
//                .addQueryParameter("fields", "status,message,query,country,city")
//                .addQueryParameter("lang", "en");
//
//        System.out.println(url.toString());
//    }
}
