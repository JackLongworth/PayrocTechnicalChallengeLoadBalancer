package config;

import connections.Backend;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import rate_limiters.RateLimiter;
import selection_strategies.BackendSelectionStrategy;

import java.time.Duration;
import java.util.List;

@Getter
@Setter(AccessLevel.PACKAGE)
public class Config {

    private int port;
    private List<Backend> backends;
    private Duration healthCheckInterval;
    private Duration healthCheckTimeout;

    private BackendSelectionStrategy selectionStrategy;

    private RateLimiter rateLimiter;

    private int maxConnections;
}
