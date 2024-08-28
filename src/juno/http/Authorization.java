package juno.http;

public interface Authorization {
    
    String getHeader();

    String getValue() throws Exception;
}
