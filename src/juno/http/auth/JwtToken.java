package juno.http.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import juno.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * https://jwt.io/
 */
public class JwtToken implements Token {
    
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
     * @throws java.io.UnsupportedEncodingException
     */
    public JwtToken(String accessToken) throws JSONException, UnsupportedEncodingException {
        this.token = accessToken;
        
        String[] chunks = accessToken.split("\\.");
        if (chunks.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format.");
        }
        
        String sHeader = new String(Base64.getUrlDecoder().decode(chunks[0]), "UTF-8");
        header = new JSONObject(sHeader);

        String sPayload = new String(Base64.getUrlDecoder().decode(chunks[1]), "UTF-8");
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

    @Override
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

    @Override
    public String getToken() {
        return token;
    }
    
     /**
     * Genera un JWT a partir de un header, un payload y una clave secreta.
     *
     * @param header El objeto JSONObject que representa el header del JWT.
     * @param payload El objeto JSONObject que representa el payload del JWT.
     * @param secretKey La clave secreta utilizada para generar la firma.
     * @return El token JWT completo como un String.
     * @throws NoSuchAlgorithmException Si el algoritmo HMAC SHA-256 no está disponible.
     * @throws java.io.UnsupportedEncodingException
     */
    public static String generateToken(JSONObject header, JSONObject payload, String secretKey) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // Codificar header y payload en Base64URL
        String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.toString().getBytes("UTF-8"));
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toString().getBytes("UTF-8"));

        // Concatenar header y payload
        String unsignedToken = encodedHeader + "." + encodedPayload;

        // Crear la firma
        String signature = createHMACSHA256Signature(unsignedToken, secretKey);

        // Concatenar todo para formar el token completo
        return unsignedToken + "." + signature;
    }

    private static String createHMACSHA256Signature(String data, String secret) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((data + secret).getBytes("UTF-8"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
    
//    public static void main(String[] args) throws JSONException {
//        JWT jwt = new JWT("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
//        System.out.println(jwt.header);
//        System.out.println(jwt.payload);
//    }
    
//    // Método main para probar la generación del token
//    public static void main(String[] args) throws Exception {
//        // Crear el header y payload
//        JSONObject header = new JSONObject();
//        header.put("alg", "HS256");
//        header.put("typ", "JWT");
//
//        JSONObject payload = new JSONObject();
//        payload.put("sub", UUID.randomUUID());
//        payload.put("name", "John Doe");
//        payload.put("iat", System.currentTimeMillis() / 1000);
//        payload.put("exp", (System.currentTimeMillis() / 1000) + 3600);
//
//        // Generar el token
//        String secretKey = Long.toHexString(System.currentTimeMillis());
//        String token = generateToken(header, payload, secretKey);
//        System.out.println("Generated Token: " + token);
//    }
}
