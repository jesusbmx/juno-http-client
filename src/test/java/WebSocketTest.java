
import java.util.concurrent.CountDownLatch;
import juno.http.Headers;
import juno.http.ws.WebSocket;
import juno.http.ws.WebSocketAdapter;

/**
 * Demo manual contra el echo server público "wss://ws.postman-echo.com/raw"
 * (igual estilo que Test.java con postman-echo.com): conecta, manda un
 * mensaje, imprime el eco que regresa el servidor y cierra la conexión.
 */
public class WebSocketTest {

    public static void main(String[] args) throws Exception {
        final CountDownLatch done = new CountDownLatch(1);

        WebSocket ws = new WebSocket("wss://ws.postman-echo.com/raw", new WebSocketAdapter() {
            @Override
            public void onOpen(WebSocket ws, Headers responseHeaders) {
                System.out.println("[open] handshake OK:\n" + responseHeaders);
                ws.send("hola desde juno-http-client");
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("[message] " + text);
                ws.close(WebSocket.NORMAL_CLOSURE, "listo");
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
                done.countDown();
            }

            @Override
            public void onFailure(WebSocket ws, Exception e) {
                e.printStackTrace();
                done.countDown();
            }
        });

        ws.connect();

        // el hilo lector de WebSocket corre como daemon: esperamos aquí a que
        // se dispare onClosed/onFailure para que el proceso no termine antes.
        done.await();
    }
}
