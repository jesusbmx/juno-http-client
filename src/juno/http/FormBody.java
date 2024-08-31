/**
 * ====================================================================
 * Codificacion {@code application-www-www-form-urlencoded}.
 * Los valores son codificados en tuplas de valores llaves separados 
 * por '&', con un '='  entre la llave y el valor.
 * ====================================================================
 */
package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import juno.io.IOUtils;
import juno.tuple.Pair;

public class FormBody extends RequestBody {

  protected final List<Pair<String, Object>> values 
          = new ArrayList<Pair<String, Object>>();
  
  protected Charset charset = DEFAULT_ENCODING;

  public FormBody() {
  }

  public FormBody(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      values.add(new Pair<String, Object>(entry.getKey(), entry.getValue()));
    }
  } 
  
  public FormBody(Object bean) {
    Class<?> type = bean.getClass();
    boolean includeSuperClass = type.getClassLoader() != null;
    Method[] methods = includeSuperClass ? type.getMethods() : type.getDeclaredMethods();
    for (final Method method : methods) {
        final int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers)
                && !Modifier.isStatic(modifiers)
                && method.getParameterTypes().length == 0
                && !method.isBridge()
                && method.getReturnType() != Void.TYPE
                && isValidMethodName(method.getName())) {
            final String key = getKeyNameFromMethod(method);
            if (key != null && !key.isEmpty()) {
                try {
                    final Object result = method.invoke(bean);
                    if (result != null) {
                        add(key, result);
                    }
                } catch (IllegalAccessException ignore) {
                } catch (IllegalArgumentException ignore) {
                } catch (InvocationTargetException ignore) {
                }
            }
        }
    }
  } 

  public Charset getCharset() {
    return charset;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }
  
  /**
   * @return el tipo de contenido para POST o PUT.
   */
  @Override public String contentType() {
    return "application/x-www-form-urlencoded; charset=" + charset.name();
  }
  
  @Override public long contentLength() throws IOException {
    ByteArrayOutputStream baos = null;
    try {
      baos = IOUtils.arrayOutputStream();
      writeTo(baos);
      return baos.size();
    } finally {
      IOUtils.closeQuietly(baos);
    }
  }

  @Override public void writeTo(OutputStream out) throws IOException {
    for (int i = 0, size = size(); i < size; i++) {
      if (i > 0) out.write('&');
      out.write(URLEncoder.encode(key(i), charset.name()).getBytes());
      out.write('=');
      out.write(URLEncoder.encode(valueAsString(i), charset.name()).getBytes());
    }
  }
  
  public String encodedUrlParams(Charset chrst) throws IOException {
    if (size() == 0) {
        return "";
    }
    ByteArrayOutputStream baos = null;
    try {
      baos = IOUtils.arrayOutputStream();
      writeTo(baos);
      return new String(baos.toByteArray(), chrst);
    } finally {
      IOUtils.closeQuietly(baos);
    }
  }
  
  public int size() {
    return values.size();
  }
  
  public FormBody add(String key, Object value) {
    values.add(new Pair<String, Object>(key, value));
    return this;
  }
  
  public FormBody addMap(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public String key(int index) {
    return values.get(index).getFirst();
  }
  
  public Object value(int index) {
    return values.get(index).getSecond();
  }
  
  public String valueAsString(int index) {
    final Object value = value(index);
    return value == null ? "" : value.toString();
  }

  @Override public String toString() {
    try {
      return encodedUrlParams(charset);
    } catch(Exception e) {
      return "";
    }
  }
  
  private static boolean isValidMethodName(String name) {
    return !"getClass".equals(name) && !"getDeclaringClass".equals(name);
  }

  private static String getKeyNameFromMethod(Method method) {
    String key;
    final String name = method.getName();
    if (name.startsWith("get") && name.length() > 3) {
        key = name.substring(3);
    } else if (name.startsWith("is") && name.length() > 2) {
        key = name.substring(2);
    } else {
        return null;
    }
    // if the first letter in the key is not uppercase, then skip.
    // This is to maintain backwards compatibility before PR406
    // (https://github.com/stleary/JSON-java/pull/406/)
    if (key.length() == 0 || Character.isLowerCase(key.charAt(0))) {
        return null;
    }
    if (key.length() == 1) {
        key = key.toLowerCase(Locale.ROOT);
    } else if (!Character.isUpperCase(key.charAt(1))) {
        key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
    }
    return key;
  }
}
