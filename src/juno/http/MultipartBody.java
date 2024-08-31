/**
 * ====================================================================
 * Codificacion {@code multipart/form-data}.
 * Contenido mixto POST (datos binarios y caracter) RFC2388.
 * ====================================================================
 */
package juno.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import juno.io.IOUtils;

public class MultipartBody extends RequestBody {
  /** Espacio colon */
  public static final byte[] COLON_SPACE = {':', ' '};
  
  /** Linea separadora. */
  public static final byte[] CR_LF = {'\r', '\n'};
  
  /** Dos Guiones. */
  public static final byte[] TWO_DASHES = {'-', '-'};

  /** Lista de datos. */
  protected final List<Part> parts;
  
  /** Variable requerida para : multipart/form-data. */
  protected final String boundary;
  
  
  public MultipartBody(List<Part> parts, String boundary) {
    this.parts = parts;
    this.boundary = boundary;
  }
  
  public MultipartBody(String boundary) {
    this(new ArrayList<Part>(), boundary);
  }
  
  public MultipartBody() {
    this(Long.toOctalString(System.currentTimeMillis()));
  }
  
  public MultipartBody(Map<String, Object> map) {
    this();
    addMap(map);
  } 
  
  public MultipartBody(Object bean) {
    this();
    
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
                        addObject(key, result);
                    }
                } catch (IllegalAccessException ignore) {
                } catch (IllegalArgumentException ignore) {
                } catch (InvocationTargetException ignore) {
                }
            }
        }
    }
  } 
  
  public String boundary() {
    return boundary;
  }
  
  @Override public String contentType() {
    return "multipart/form-data; boundary=" + boundary;
  }
  
  @Override public long contentLength() throws IOException {
    long len = 0;
    for (int i = 0; i < parts.size(); i++) {
      final Part part = parts.get(i);
      len += part.body.contentLength();
    }
    ByteArrayOutputStream baos = null;
    try {
      baos = IOUtils.arrayOutputStream();
      doWrite(baos, false);
      return baos.size() + len;
    } finally {
      IOUtils.closeQuietly(baos);
    }
  }

  @Override public void writeTo(OutputStream out) throws IOException {
    doWrite(out, true);
  }
  
  private void doWrite(OutputStream out, boolean write) throws IOException {
    byte[] boundaryToCharArray = boundary.getBytes();

    for (Part part : parts) {      
      out.write(TWO_DASHES);
      out.write(boundaryToCharArray);
      out.write(CR_LF);

      // Write Format Multipart Header:
      for (int i = 0, size = part.headers.size(); i < size; i++) {
        out.write(part.headers.getName(i).getBytes());
        out.write(COLON_SPACE);
        out.write(part.headers.getValue(i).getBytes());
        out.write(CR_LF);
      }
      out.write(Headers.CONTENT_TYPE.getBytes());
      out.write(COLON_SPACE);
      out.write(part.body.contentType().getBytes());
      out.write(CR_LF);
      
      // Write Body:
      out.write(CR_LF);
      if (write) part.body.writeTo(out);
      out.write(CR_LF);
    }

    // End of multipart/form-data.
    out.write(TWO_DASHES);
    out.write(boundaryToCharArray);
    out.write(TWO_DASHES);
    out.write(CR_LF);
  }

  public MultipartBody addPart(Part bodyPart) {
    parts.add(bodyPart);
    return this;
  }
  
  public MultipartBody addBody(String name, RequestBody body) {
    return addPart(Part.createFormData(name, body));
  }
  
  public MultipartBody addBody(RequestBody body) {
    return addPart(new Part(body, new Headers()));
  }
  
  public MultipartBody addParam(String name, Object value, Charset charset) {
    final String newValue = (value == null) ? "" : value.toString();
    final RequestBody body = RequestBody.create(
            "text/plain; charset=" + charset.name(), newValue);
    return addPart(Part.createFormData(name, body));
  }
  
  public MultipartBody addParam(String name, Object value) {
    return addParam(name, value, DEFAULT_ENCODING);
  }

  public MultipartBody addFile(String name, File file) {
    return addFile(name, file, file.getName());
  }
  
  public MultipartBody addFile(String name, File file, String filename) {
    final RequestBody body = RequestBody.create(
            "application/octet-stream", file);
    return addPart(Part.createFormData(name, filename, body));
  }
  
  public MultipartBody addFile(String name, byte[] value, String filename) {
    final RequestBody body = RequestBody.create(
            "application/octet-stream", value);
    return addPart(Part.createFormData(name, filename, body));
  }
  
  public MultipartBody addObject(String name, Object value) {
    if (value == null) 
      addParam(name, value);
    else if (value instanceof File) 
      addFile(name, (File) value);
    else if (value instanceof Part) 
      addPart((Part) value);
    else if (value instanceof RequestBody) 
      addBody(name, (RequestBody) value);
    else
      addParam(name, value);
    
    return this;
  }
  
  public MultipartBody addMap(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      addObject(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public List<Part> parts() {
    return parts;
  }
  
  public static class Part {

    final RequestBody body;
    final Headers headers;

    public Part(RequestBody body, Headers headers) {
      this.body = body;
      this.headers = headers;
    }

    public Headers headers() {
      return headers;
    }

    public RequestBody body() {
      return body;
    }
    
    public static Part createFormData(String name, RequestBody body) {
      final Headers headers = Headers.of(
              Headers.CONTENT_DISPOSITION, String.format("form-data; name=\"%s\"", name),
              // .add(Headers.CONTENT_TYPE, String.format("text/plain; charset=%s", charset.name()))
              Headers.CONTENT_TRANSFER_ENCODING, "8bit");
      return new Part(body, headers);
    }

    public static Part createFormData(String name, String filename, RequestBody body) {
      final Headers headers = Headers.of(
              Headers.CONTENT_DISPOSITION, String.format("form-data; name=\"%s\"; filename=\"%s\"", name, filename),
              // .add(Headers.CONTENT_TYPE, "application/octet-stream")
              Headers.CONTENT_TRANSFER_ENCODING, "binary");
      return new Part(body, headers);
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