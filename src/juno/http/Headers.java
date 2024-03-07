package juno.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import juno.tuple.Pair;
import juno.util.Convert;
import juno.util.Strings;

public class Headers {

  public static final String CONTENT_DISPOSITION = "Content-Disposition";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";

  protected final List<Pair<String, String>> values;
  
  public Headers(int size) {
    values = new ArrayList<Pair<String, String>>(size);
  }
    
  public Headers() {
    this(10);
  }
  
  public Headers(String... headers) {
    this(headers.length);
    addHeaders(headers);
  }
  
  public Headers(Map<String, String> headers) {
    this(headers.size());
    addHeaders(headers);
  }
  
  public Headers(Headers headers) {
    this(headers.size());
    addHeaders(headers);
  }
  
  public int size() {
    return values.size();
  }
   
  public boolean isEmpty() {
    return values.isEmpty();
  }

  public void clear() {
    values.clear();
  }
  
  public void remove(int i) {
    values.remove(i);
  }
  
  public int indexOf(String key) {
    for (int i = 0; i < values.size(); i++) {
      if (key.equalsIgnoreCase(getName(i))) {
        return i;
      }
    }
    return -1;
  }
  
  public boolean containsName(String name) {
    if (name == null) return false;
    int i = indexOf(name);
    return i != -1;
  }

  public boolean containsValue(String value) {
   if (value == null) return false;
   for (int i = 0; i < size(); i++) {
    if (value.equals(getValue(i))) {
     return true;
    }
   }
   return false;
  }
  
  public String getName(int index) {
    return values.get(index).getFirst();
  }
  
  public String getValue(int index) {
    return values.get(index).getSecond();
  }
  
  public String getValue(String name, String defaultval) {
    int i = indexOf(name);
    return i != -1 ? getValue(i) : defaultval;
  }
  
  public String getValue(String name) {
    return getValue(name, null);
  }
  
  public long getLong(String name, long defValue) {
    return Convert.toLong(getValue(name), defValue);
  }
  
  public int getLong(String name, int defValue) {
    return Convert.toInt(getValue(name), defValue);
  }
  
  public long getContentLength() {
    return getLong(CONTENT_LENGTH, -1);
  }
  
  public String getContentType() {
    return getValue(CONTENT_TYPE, null);
  }
  
  public String getContentDisposition() {
    return getValue(CONTENT_DISPOSITION, null);
  }
  
  public String getFileName() {
    final String contentDisposition = getContentDisposition(); 
    if (contentDisposition != null) {
      return Strings.subStr(contentDisposition, "filename=\"", "\"");
    }
    return null;
  }
  
  public List<String> getValues(String name) {
    final List<String> r = new ArrayList<String>();
    for (int i = 0; i < size(); i++) {
      if (name.equalsIgnoreCase(getName(i))) {
        r.add(getValue(i));
      }
    }
    return r;
  }
  
  public Headers add(String name, String value) {
    // Check for malformed headers.
    if (name == null) throw new NullPointerException("name == null");
    if (name.isEmpty()) throw new IllegalArgumentException("name is empty");
    if (value == null) throw new NullPointerException("value for name " + name + " == null");
    if (name.indexOf('\0') != -1 || value.indexOf('\0') != -1) {
      throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
    }
    
    values.add(new Pair<String, String>(name, value));
    return this;
  }
  
  public Headers add(String name, List<String> values) {
    if (name == null) return this;
    for (int i = 0; i < values.size(); i++) {
      this.add(name, values.get(i));
    }
    return this;
  }

  public Headers add(String header) {
    int i = header.indexOf(':');
    if (i == -1) throw new IllegalArgumentException(header);
    String name = header.substring(0, i);
    String value = header.substring(i+1, header.length());
    return add(name.trim(), value.trim());
  }
    
  public Headers addHeaders(Map<String, String> headers) {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
    return this;
  }
  
  public Headers addHeaders(String... headers) {
    for (String header : headers) add(header);
    return this;
  }
  
  public Headers addHeaders(Headers headers) {
    this.values.addAll(headers.values);
    return this;
  }
  
  public Headers addHeadersMapList(Map<String, List<String>> map) {
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      this.add(entry.getKey(), entry.getValue());
    }
    return this;
  }
  
  public static Headers of(Map<String, List<String>> map) {
    Headers headers = new Headers(map.size());
    headers.addHeadersMapList(map);
    return headers;
  }

  /**
   * Returns headers for the alternating header names and values. There must be
   * an even number of arguments, and they must alternate between header names
   * and values.
   */
  public static Headers of(String... namesAndValues) {
    if (namesAndValues == null)  throw new NullPointerException("namesAndValues == null");
    if (namesAndValues.length % 2 != 0) {
      throw new IllegalArgumentException("Expected alternating header names and values");
    }

    Headers headers = new Headers(namesAndValues.length / 2);
    
    for (int i = 0; i < namesAndValues.length; i += 2) {
      String name = namesAndValues[i];
      String value = namesAndValues[i + 1];
      headers.add(name, value);
    }

    return headers;
  }
  
 

  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    for (int i = 0, size = size(); i < size; i++) {
      if (i > 0) result.append("\n");  
      result.append(getName(i)).append(": ")
              .append(getValue(i));
    }
    return result.toString();
  }
  
  /*public static void main(String[] args) {
    Headers h = new Headers(
           "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
           "Content-Type: application/x-www-form-urlencoded; charset=UTF-8"
    );
    h.add("x-headername", "x-value");
    System.out.println(h.size());
    System.out.println(h.containsKey(CONTENT_TYPE));
    System.out.println(h.getValue(CONTENT_TYPE));
    System.out.println(h);
  }*/
}
