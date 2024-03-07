package juno.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpUrl {
  final String url;
  final Map<String, String> pathVars = new HashMap<String, String>();
  final FormBody parameters;

  public HttpUrl(String url, FormBody parameters) {
    this.url = url;
    this.parameters = parameters;
  }
  
  public HttpUrl(String url) {
    this(url, new FormBody());
  }
  
  public String getUrl() {
    return url;
  }
  
  /**
   * "http://ip-api.com/json/{ip}"
   * 
   * @param chrst
   * @return "http://ip-api.com/json/24.48.0.1" 
   * @throws java.io.IOException 
   */
  public String encodedUrlPaths(Charset chrst) throws IOException {
    String result = url;
    for (Map.Entry<String, String> path : pathVars.entrySet()) {
        String target = "{" + path.getKey() + "}";
        String value = path.getValue();
        value = value == null ? "" : value;
        String replacement = URLEncoder.encode(value, chrst.name());
        result = result.replace(target, replacement);
    }
    return result;
  }
    
  public HttpUrl setPath(String key, String value) {
    pathVars.put(key, value);
    return this;
  }
  
  public String path(String key) {
    return pathVars.get(key);
  }
  
  public int pathSize() {
    return pathVars.size();
  }
  
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
  
  public String queryParameterName(int index) {
    return parameters.key(index);
  }
  
  public Object queryParameterValue(int index) {
    return parameters.value(index);
  }
  
  public int queryParameterSize() {
    return parameters.size();
  }
  
  public String toString(FormBody body, Charset charset) {
    try {
      final String encodedUrlPaths = encodedUrlPaths(charset);
      if (body.size() == 0) return encodedUrlPaths;
    
      final String encodedParams = body.encodedUrlParams(charset);

      return new StringBuilder(encodedUrlPaths.length() + 1 + encodedParams.length())
            .append(encodedUrlPaths)
            .append(encodedUrlPaths.endsWith("?") ? '&' : '?')
            .append(encodedParams).toString();
      
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public String toString(Charset charset) {
    return toString(parameters, charset);
  }
  
  @Override public String toString() {
    return toString(HttpRequest.DEFAULT_ENCODING);
  }
  
//  public static void main(String[] args) {
//    HttpUrl url = new HttpUrl(
//        "http://ip-api.com/{returnType}/{ip}");
//
//    url.setPath("returnType", "json");
//    url.setPath("ip", "24.48.0.1");
//
//    url.addQueryParameter("fields", "status,message,query,country,city");
//    url.addQueryParameter("lang", "en");
//    
//    System.out.println(url.toString());
//  }
}
