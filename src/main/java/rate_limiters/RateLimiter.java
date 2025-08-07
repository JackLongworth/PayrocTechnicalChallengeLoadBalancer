package rate_limiters;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;

    private final Map<InetAddress, ConnectionsWindow> rateMap = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allow(InetAddress clientIp) {
        long now = Instant.now().toEpochMilli();
        ConnectionsWindow window = rateMap.computeIfAbsent(clientIp, ip -> {
            ConnectionsWindow cw = new ConnectionsWindow();
            cw.start = now;
            return cw;
        });

        synchronized (window) {
            if (now - window.start >= windowMillis) {
                window.connections.set(0);
                window.start = now;
            }

            return window.connections.incrementAndGet() <= maxRequests;
        }
    }
}

