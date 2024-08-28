package juno.http.auth;

public interface Token {

    public String getToken();
    
    public boolean isValid();
    
    public interface OnAuth {

        /**
         * logea el app con el webservice.
         *
         * @param oldToken
         * @return
         * @throws java.lang.Exception
         */
        Token auth(Token oldToken) throws Exception;
    }
}
