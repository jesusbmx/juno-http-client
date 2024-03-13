package juno.http.auth;

import juno.http.HttpRequest;
import juno.http.HttpStack;
import juno.http.HttpResponse;
import juno.http.OnInterceptor;

public class AuthTokenInterceptor implements OnInterceptor {
    
    public final JWTManager tokenManager;
    public final String header;
    public final String headerPrefix;

    public AuthTokenInterceptor(JWTManager tokenManager, String header, String headerPrefix) {
        this.tokenManager = tokenManager;
        this.header = header;
        this.headerPrefix = headerPrefix;
    }

    public AuthTokenInterceptor(JWTManager tokenManager) {
        this(tokenManager, "Authorization", "Bearer ");
    }
   
    @Override
    public HttpResponse intercept(HttpRequest request, HttpStack httpStack) throws Exception {
        // Validamos si estamos logeados para relizar peticiones.
        final JWT jwt = tokenManager.getToken();
        // Agregamos la autorizacion a la peticiòn.
        request.addHeader(header, headerPrefix + jwt.getToken());

        return httpStack.execute(request);
    }
    
}