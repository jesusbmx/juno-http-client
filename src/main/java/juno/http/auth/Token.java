package juno.http.auth;

public interface Token {

    public String getToken();
    
    public boolean isValid();
}
