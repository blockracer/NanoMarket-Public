package market;

import uk.oczadly.karl.jnano.websocket.WsObserver;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.websocket.api.Session;

import static market.Main.*;

public class Observer implements WsObserver {
    private static final int INITIAL_RECONNECT_DELAY = 1000; // Initial delay in milliseconds
    private static final int MAX_RECONNECT_DELAY = 30000;    // Maximum delay in milliseconds
    private static final double BACKOFF_MULTIPLIER = 2.0;     // Exponential backoff multiplier

    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> reconnectTask;
    private AtomicLong delayWrapper;

    public Observer() {
        this.delayWrapper = new AtomicLong(INITIAL_RECONNECT_DELAY);
    }

    @Override
    public void onOpen(int httpStatus) {
        System.out.println("WebSocket connected!");
        cancelReconnect();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket disconnected! Code: " + code + ", Reason: " + reason);

        // Schedule reconnection attempt
        scheduleReconnect();
    }

    @Override
    public void onSocketError(Exception ex) {
        ex.printStackTrace();
        // Handle socket error as needed
    }

    private void scheduleReconnect() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }

        if (reconnectTask == null || reconnectTask.isDone()) {
            reconnectTask = executorService.scheduleWithFixedDelay(() -> {
                try {
                    long currentDelay = delayWrapper.get();
                    System.out.println("Attempting to reconnect...");
                    if (ws.connect()) {
                        System.out.println("Reconnected successfully!");
                        cancelReconnect();
                    } else {
                        System.err.println("Reconnect attempt failed, retrying in " + currentDelay + " ms");
                        long nextDelay = Math.min((long) (currentDelay * BACKOFF_MULTIPLIER), MAX_RECONNECT_DELAY);
                        delayWrapper.set(nextDelay);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, 0, INITIAL_RECONNECT_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
        }
    }

    public void stopReconnect() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
