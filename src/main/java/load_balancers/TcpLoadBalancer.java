package load_balancers;

import config.Config;
import connections.Backend;
import connections.BidirectionalConnection;
import lombok.extern.slf4j.Slf4j;
import selection_strategies.BackendSelectionStrategy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;


@Slf4j
public class TcpLoadBalancer implements LoadBalancer {

    private final int port;
    private final List<Backend> backends;

    private final Duration healthCheckInterval;

    private final BackendSelectionStrategy selectionStrategy;

    public TcpLoadBalancer(Config config) {
        this.port = config.getPort();
        this.backends = config.getBackends();
        this.healthCheckInterval = config.getHealthCheckInterval();
        this.selectionStrategy = config.getSelectionStrategy();
    }

    private void handleNewConnection(Socket client) {

        Backend target = null;
        try {
            target = this.selectionStrategy.select(this.backends);
        } catch (NoHealthyBackendsException e) {
            log.error("{} Exiting...", e.getMessage());
            System.exit(1);
        }

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

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Load balancer listening on port {}", port);

            HealthChecker healthChecker = new HealthChecker(this.backends, this.healthCheckInterval);
            Thread healthCheckerThread = Thread.startVirtualThread(healthChecker);

            while (!Thread.interrupted()) {
                Socket client = serverSocket.accept();
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
}
