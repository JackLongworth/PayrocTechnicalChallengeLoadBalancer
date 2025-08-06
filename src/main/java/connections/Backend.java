package connections;

import lombok.Data;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Backend {

    private final InetSocketAddress address;
    private volatile boolean healthy = true;
    private AtomicInteger connections = new AtomicInteger(0);

    public Backend(InetSocketAddress address) {
        this.address = address;
    }
}
