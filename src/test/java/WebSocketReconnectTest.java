
import java.util.Date;
import juno.http.Headers;
import juno.http.ws.WebSocket;
import juno.http.ws.WebSocketAdapter;
import juno.util.Dates;

/**
 * Demo manual de reconexión contra "wss://ws.postman-echo.com/raw". No requiere
 * cambios en WebSocket: toda la lógica de reconectar vive aquí, igual patrón que
 * el "onclose + setTimeout(connect, delay)" del hook de React Native.
 *
 * Manda un mensaje por segundo mientras el socket está abierto; corta el WiFi
 * para forzar un onFailure y vuelve a prenderlo para ver la reconexión.
 */
public class WebSocketReconnectTest {

    private static final String URL = "wss://ws.postman-echo.com/raw";
    private static final long RECONNECT_DELAY_MS = 3000;

    public static void main(String[] args) throws Exception {
        WebSocket ws = new WebSocket(URL, new WebSocketAdapter() {
            @Override
            public void onOpen(final WebSocket ws, Headers responseHeaders) {
                System.out.println("[open] handshake OK:\n" + responseHeaders);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("[message] " + text);
            }

            @Override
            public void onMessage(WebSocket ws, byte[] bytes) {
                System.out.println("[message binario] " + bytes.length + " bytes");
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                System.out.println("[closing] code:" + code + " reason:" + reason);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                System.out.println("[closed] code:" + code + " reason:" + reason);
            }

            @Override
            public void onFailure(WebSocket ws, Exception e) {
                System.err.println("[error] message:" + e.getMessage());
            }
        });

        // Espera bloqueando hasta que la conexión esté lista (o falle)
        boolean connected = ws.connectBlocking();

        if (connected) {
            while (true) {
                try {
                ws.send("tick: " + new Date());
                Thread.sleep(1000);
                } catch(Exception e) {
                    System.out.println("no se pudo mandar la informacion al server");
                    e.printStackTrace();
                    break;
                }
            }
        } else {
            System.err.println("No se pudo conectar al servidor WebSocket");
        }
    }

   
}
