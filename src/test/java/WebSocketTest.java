import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import juno.http.ws.ServerHandshake;
import juno.http.ws.WebSocket;
import juno.http.ws.WebSocketAdapter;
import juno.util.Dates;

/**
 * Demo de un "servicio" WebSocket con ciclo de vida mount/unmount.
 *
 * mount()   -> abre la conexión y, si se cae por cualquier razón que no sea
 *              un unmount() explícito, se reconecta solo (backoff simple)
 *              usando el scheduler como si fuera un setTimeout.
 * unmount() -> apaga el servicio de verdad: cancela cualquier reconexión
 *              pendiente, cierra el socket activo y apaga el scheduler.
 */
public class WebSocketTest {

    // Hilo NO daemon -> mantiene la JVM viva mientras haya tareas pendientes
    // (el reintento agendado). Si usáramos un hilo daemon acá, el proceso
    // podría morir en medio de la espera del backoff.
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    
    private static final long RECONNECT_DELAY_MS = 8_000;

    private volatile WebSocket ws;
    private volatile boolean mounted;
    private volatile ScheduledFuture<?> pendingReconnect;

    /** Monta el servicio: conecta y se auto-reconecta mientras esté mounted. */
    public synchronized void mount() {
        if (mounted) return; // ya montado, no duplicar
        mounted = true;
        connect();
    }

    /**
     * Desmonta el servicio: apaga de verdad. Cancela cualquier reconexión
     * agendada, cierra el socket activo y apaga el scheduler (no habrá más
     * reintentos posibles después de esto para esta instancia).
     */
    public synchronized void unmount() {
        if (!mounted) return;
        mounted = false;

        if (pendingReconnect != null) {
            pendingReconnect.cancel(false); // cancela el "setTimeout" si aún no disparó
            pendingReconnect = null;
        }
        if (ws != null) {
            ws.close(WebSocket.NORMAL_CLOSURE, "unmount");
        }

        scheduler.shutdown();
    }

    private void connect() {
        ws = new WebSocket("wss://ws.postman-echo.com/raw", new WebSocketAdapter() {
            @Override
            public void onOpen(WebSocket ws, ServerHandshake handshake) {
                System.out.println("[open] conectado: status:" + handshake.getHttpStatus() + " statusMessage:" + handshake.getHttpStatusMessage());
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("[message] " + text);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                System.out.println("[closed] code:" + code + " reason:" + reason);
                reconnect();
            }

            @Override
            public void onFailure(WebSocket ws, Exception e) {
                System.out.println("[failure] " + e);
            }
        });
        ws.connect(); // asíncrono, no bloquea
    }

    /**
     * Equivalente a setTimeout(function() { connect(); }, delay) pero
     * re-chequeando "mounted" tanto al agendar como al disparar, para no
     * revivir un servicio que ya fue apagado con unmount().
     */
    private synchronized void reconnect() {
        if (!mounted) return; // fue un cierre intencional (unmount) -> no reconectar
        System.out.println("[reconnect]");

        pendingReconnect = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (WebSocketTest.this) {
                    pendingReconnect = null;
                    if (mounted) connect(); // por si unmount() llegó durante la espera
                }
            }
        }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    
    void simulator() {
         while (true) {
            try {
                ws.send("Tick: " + Dates.dateTimeFormat());
                Thread.sleep(1000);
            } catch (Exception e) {
                break;
            }
        }
    }

    // --- demo ---
    public static void main(String[] args) throws Exception {
        WebSocketTest service = new WebSocketTest();

        service.mount();       // arranca, se reconecta solo si se cae
        service.simulator();   // el servicio vive acá, reconectando si hace falta

        service.unmount();     // apagado real y definitivo: no reconecta más y apaga el scheduler
        System.out.println("Servicio apagado.");
    }
}