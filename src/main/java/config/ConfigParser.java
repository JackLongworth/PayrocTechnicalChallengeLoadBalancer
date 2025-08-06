package config;

import connections.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import selection_strategies.BackendSelectionStrategy;
import selection_strategies.FirstAvailableSelectionStrategy;
import selection_strategies.LeastConnectionsSelectionStrategy;
import selection_strategies.RoundRobinSelectionStrategy;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ConfigParser {

    private static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    private static final String HOST_PORT_ENV_VAR = "LB_PORT";
    private static final String BACKENDS_ENV_VAR = "LB_BACKENDS";
    private static final String HEALTH_CHECK_INTERVAL_ENV_VAR = "LB_HEALTH_CHECK_INTERVAL_MS";
    private static final String HEALTH_CHECK_TIMEOUT_ENV_VAR = "LB_HEALTH_CHECK_TIMEOUT_MS";
    private static final String SELECTION_STRATEGY_ENV_VAR = "LB_STRATEGY";

    private static final int DEFAULT_HOST_PORT = 8080;
    private static final int DEFAULT_HEALTH_CHECK_INTERVAL_MS = 5000;
    private static final int DEFAULT_HEALTH_CHECK_TIMEOUT_MS = 1000;

    private static final BackendSelectionStrategy DEFAULT_SELECTION_STRATEGY = new RoundRobinSelectionStrategy();

    private enum SelectionStrategy {
        ROUND_ROBIN, LEAST_CONNECTIONS, FIRST_AVAILABLE
    }

    public static Config parseEnvironmentVariablesToConfig() {
        Config config = new Config();
        config.setPort(parseHostPort());
        config.setBackends(parseBackends());
        config.setHealthCheckInterval(parseHealthCheckInterval());
        config.setHealthCheckTimeout(parseHealthCheckTimeout());
        config.setSelectionStrategy(parseBackendSelectionStrategy());

        return config;
    }

    private static int parseHostPort() {
        return parseIntegerEnvironmentVariableOrDefault(HOST_PORT_ENV_VAR, DEFAULT_HOST_PORT);
    }

    private static List<Backend> parseBackends() {
        List<Backend> backends = new ArrayList<>();

        if (System.getenv().containsKey(BACKENDS_ENV_VAR)) {
            try {
                // comma separated list
                String[] addresses = System.getenv().get(BACKENDS_ENV_VAR).split(",");
                for (String address : addresses) {
                    String[] hostAndPort = address.split(":");
                    if (hostAndPort.length == 2) {
                        String host = hostAndPort[0].trim();
                        int port = Integer.parseInt(hostAndPort[1].trim());

                        InetSocketAddress backendAddress = new InetSocketAddress(host, port);
                        backends.add(new Backend(backendAddress));
                    }
                }
            } catch (NumberFormatException nfe) {
                log.error("Backends defined by environment variable {} are not in valid format", BACKENDS_ENV_VAR);
                log.error("Valid Usage: <host>:<port>[,<host>:<port>...]");
                System.exit(1);
            }
        } else {
            log.error("No backends are defined. Exiting...");
            System.exit(1);
        }

        return backends;
    }

    private static Duration parseHealthCheckInterval() {
        int healthCheckIntervalMs = parseIntegerEnvironmentVariableOrDefault(HEALTH_CHECK_INTERVAL_ENV_VAR, DEFAULT_HEALTH_CHECK_INTERVAL_MS);
        return Duration.ofMillis(healthCheckIntervalMs);
    }

    private static Duration parseHealthCheckTimeout() {
        int healthCheckIntervalMs = parseIntegerEnvironmentVariableOrDefault(HEALTH_CHECK_TIMEOUT_ENV_VAR, DEFAULT_HEALTH_CHECK_TIMEOUT_MS);
        return Duration.ofMillis(healthCheckIntervalMs);
    }


    private static int parseIntegerEnvironmentVariableOrDefault(String environmentVariable, int defaultValue) {
        int result = defaultValue;

        if (System.getenv().containsKey(environmentVariable)) {
            try {
                result = Integer.parseInt(System.getenv().get(environmentVariable));
            } catch (NumberFormatException nfe) {
                // Clearly the command line or environment is faulty so better to exit than continue with default and
                // cause confusion
                log.error("Environment variable {} is not a valid integer. Exiting...", environmentVariable);
                System.exit(1);
            }
        } else {
            log.error("Environment variable {} cannot be found. Using default value of {}", environmentVariable, defaultValue);
        }

        return result;
    }

    private static BackendSelectionStrategy parseBackendSelectionStrategy() {
        BackendSelectionStrategy result;

        if (System.getenv().containsKey(SELECTION_STRATEGY_ENV_VAR)) {
            SelectionStrategy selectionStrategy = SelectionStrategy.valueOf(System.getenv().get(SELECTION_STRATEGY_ENV_VAR));


            switch(selectionStrategy) {
                case ROUND_ROBIN -> result = new RoundRobinSelectionStrategy();
                case LEAST_CONNECTIONS -> result = new LeastConnectionsSelectionStrategy();
                case FIRST_AVAILABLE -> result = new FirstAvailableSelectionStrategy();
                default -> result = DEFAULT_SELECTION_STRATEGY;
            }
        } else {
            log.warn("No definition found for {} so using default value", SELECTION_STRATEGY_ENV_VAR);
            result = DEFAULT_SELECTION_STRATEGY;
        }

        return result;
    }
}
