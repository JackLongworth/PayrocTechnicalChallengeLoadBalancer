package load_balancers;

import config.Config;
import connections.Backend;
import connections.BidirectionalConnection;
import lombok.extern.slf4j.Slf4j;
import rate_limiters.RateLimiter;
import selection_strategies.BackendSelectionStrategy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class TcpLoadBalancer implements LoadBalancer {

    private final AtomicInteger activeConnections = new AtomicInteger(0);

    private final int port;
    private final List<Backend> backends;

    private final Duration healthCheckInterval;
    private final Duration healthCheckTimeout;

    private final BackendSelectionStrategy selectionStrategy;

    private final RateLimiter rateLimiter;

    private boolean running = true;

    private final int maxConnections;

    public TcpLoadBalancer(Config config) {
        this.port = config.getPort();
        this.backends = config.getBackends();
        this.healthCheckInterval = config.getHealthCheckInterval();
        this.healthCheckTimeout = config.getHealthCheckTimeout();
        this.selectionStrategy = config.getSelectionStrategy();
        this.rateLimiter = config.getRateLimiter();
        this.maxConnections = config.getMaxConnections();
    }

    private void handleNewConnection(Socket client) {

        Backend target = selectBackend();

        if (!allowedByRateLimiter(client) || target == null) return;

        try (Socket backend = new Socket()) {
            backend.connect(target.getAddress());
            target.getConnections().incrementAndGet();

            BidirectionalConnection connection = new BidirectionalConnection(client, backend);
            connection.start();
            target.getConnections().decrementAndGet();
        } catch (IOException e) {
            log.error("Something went wrong with backend socket {} : {}", target.getAddress().toString(), e.getMessage());
        }
    }

    private Backend selectBackend() {
        Backend target = null;
        try {
            target = this.selectionStrategy.select(this.backends);
        } catch (NoHealthyBackendsException e) {
            log.error("{} Exiting...", e.getMessage());
            this.stop();
        }
        return target;
    }

    private boolean allowedByRateLimiter(Socket client) {
        boolean allowed = rateLimiter.allow(client.getInetAddress());

        if (!allowed) {
            log.warn("Rate limit exceeded for {}", client.getInetAddress());
            try {
                client.close();
            } catch (IOException e) {
                log.debug("Closing socket for rate-limited client: {}", e.getMessage());
            }
        }
        return allowed;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Load balancer listening on port {}", port);

            HealthChecker healthChecker = new HealthChecker(backends, healthCheckInterval, healthCheckTimeout);
            Thread healthCheckerThread = Thread.startVirtualThread(healthChecker);

            while (running && !Thread.interrupted()) {
                Socket client = serverSocket.accept();

                if (activeConnections.incrementAndGet() > maxConnections) {
                    log.warn("Connection refused: too many active connections ({})", activeConnections.get());
                    client.close();
                    activeConnections.decrementAndGet();
                    continue;
                }

                Thread.startVirtualThread(() -> {
                    try {
                        handleNewConnection(client);
                    } catch (NoHealthyBackendsException e) {
                        log.error("Stopping load balancer because : {}", e.getMessage());
                    }
                });
            }
            healthCheckerThread.interrupt();
        } catch (IOException e) {
            log.error("Couldn't open socket : {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        running = false;
    }
}
