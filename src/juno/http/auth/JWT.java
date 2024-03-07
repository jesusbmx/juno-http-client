package juno.http.auth;

import java.util.Date;
import juno.util.Convert;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * https://jwt.io/
 */
public class JWT {
    
    public final String token;
    
    /**
     * {"alg":"HS256","typ":"JWT"}
     * 
     * El encabezado normalmente consta de dos partes: el tipo de token, 
     * que es JWT, y el algoritmo de firma que se utiliza, como HMAC SHA256 o RSA.
     */ 
    public final JSONObject header;
    
    /**
     * {"email":"no-reply@labeldictate.com","roles":[{"id":6,"name":"sw"}],"iat":1684190223,"exp":1684276623}
     * 
     * es la carga útil, que contiene las reclamaciones. Las notificaciones son 
     * declaraciones sobre una entidad (normalmente, el usuario) y datos adicionales. 
     * Hay tres tipos de reclamaciones: registradas, públicas y privadas.
     */
    public final JSONObject payload;
    
    /**
     * Para crear la parte de firma, debe tomar el encabezado codificado, la carga 
     * útil codificada, un secreto, el algoritmo especificado en el encabezado y firmarlo.
     */
    public final String signature;
    
    /**
     * @param accessToken "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
     * @throws org.json.JSONException
     */
    public JWT(String accessToken) throws JSONException {
        this.token = accessToken;
        
        String[] chunks = accessToken.split("\\.");
        
        String sHeader = Convert.fromBase64(chunks[0]);
        header = new JSONObject(sHeader);

        String sPayload = Convert.fromBase64(chunks[1]);
        payload = new JSONObject(sPayload);
        
        signature = chunks[2];
    }

    public Date getDate(String name) {
        if (!payload.has(name)) {
            return null;
        }
        
        //System.out.printf("payload %s \n", payload.toString(1));
        double exp = payload.optDouble(name);
        
        long unixSeconds = (long) exp;
        
        // convert seconds to milliseconds
        return new Date(unixSeconds*1000L);
    }
    
    public Date expireAt() {
        return getDate("exp");
    }

    public boolean isValid() {
        Date expireAt = expireAt();
        if (expireAt == null) {
            return true;
        }
        
        long now = System.currentTimeMillis();
        long expAt = expireAt.getTime();

//        System.out.printf("expireAt %s < %s\n", 
//                Dates.dateTimeFormat(now), Dates.dateTimeFormat(expAt));

        return now < expAt;
    }

    public String getToken() {
        return token;
    }
    
    public interface OnAuth {
    
        /** 
         * logea el app con el webservice. 
         * 
         * @return 
         * @throws java.lang.Exception
         */
        JWT auth() throws Exception;
    }

    
//    public static void main(String[] args) throws JSONException {
//        JWT jwt = new JWT("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
//        System.out.println(jwt.header);
//        System.out.println(jwt.payload);
//    }
}
