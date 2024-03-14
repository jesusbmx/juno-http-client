package juno.http.auth;

public interface Token {

    public String getToken();
    
    public boolean isValid();

    public interface OnAuth {

        /**
         * logea el app con el webservice.
         *
         * @return
         * @throws java.lang.Exception
         */
        Token auth() throws Exception;
    }
}
