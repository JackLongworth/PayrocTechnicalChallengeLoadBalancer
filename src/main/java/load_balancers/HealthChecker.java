package load_balancers;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import connections.Backend;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HealthChecker implements Runnable {

    private final List<Backend> backends;
    private final Duration interval;
    private final Duration timeout;

    private boolean running = true;

    public HealthChecker(List<Backend> backends, Duration interval, Duration timeout) {
        this.backends = backends;
        this.interval = interval;
        this.timeout = timeout;
    }

    public void stop() {
        running = false;
    }

    // Since this is a virtual thread it will just be 'parked' when it sleeps so it's not technically busy waiting
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        log.info("Health Checker running...");
        while (running && !Thread.currentThread().isInterrupted()) {
            for (Backend backend : backends) {
                boolean previouslyHealthy = backend.isHealthy();

                try (Socket testSocket = new Socket()) {
                    testSocket.connect(backend.getAddress(), Long.valueOf(timeout.toMillis()).intValue());
                    backend.setHealthy(true);
                    log.debug("Backend {} is up", backend.getAddress());
                } catch (IOException e) {
                    backend.setHealthy(false);
                    log.warn("Backend {} is down", backend.getAddress());
                }

                if (previouslyHealthy && !backend.isHealthy()) {
                    log.info("Backend {} transitioned to UNHEALTHY", backend.getAddress());
                } else if (!previouslyHealthy && backend.isHealthy()) {
                    log.info("Backend {} transitioned to HEALTHY", backend.getAddress());
                }
            }

            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException e) {
                log.warn("Health checker interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
