package rate_limiters;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ConnectionsWindow {
    AtomicInteger connections = new AtomicInteger(0);
    long start;
}
