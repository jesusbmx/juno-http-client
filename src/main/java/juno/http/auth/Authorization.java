package juno.http.auth;

public interface Authorization {

    /**
     * 
     * @return 
     *      "Bearer %token%" or 
     *      "OAuth %parameters%" or 
     *      "Basic %base64_encoded_credentials%" or 
     * 
     * @throws Exception 
     */
    String generateAuthHeader() throws Exception;
}
