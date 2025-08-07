package integration;

import config.Config;
import config.ConfigParser;
import connections.Backend;
import load_balancers.LoadBalancer;
import load_balancers.TcpLoadBalancer;
import integration.mock_clients.TestClient;
import integration.mock_servers.EchoServer;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;


public class LoadBalancerIntegrationTest {

    static final int BOOT_TIME = 500;

    static List<EchoServer> mockBackends = new ArrayList<>();

    static LoadBalancer loadBalancer;

    private static Config config;

    @BeforeEach
    public void setup() throws InterruptedException {
        config = ConfigParser.parseEnvironmentVariablesToConfig();

        mockBackends.clear();

        for (Backend backends : config.getBackends()) {
            int backendPort = backends.getAddress().getPort();
            EchoServer echoServer = new EchoServer(backendPort);
            Thread.startVirtualThread(echoServer);

            mockBackends.add(echoServer);
        }

        Thread.sleep(BOOT_TIME);

        if (loadBalancer == null) {
            loadBalancer = new TcpLoadBalancer(config);
        }
        Thread.startVirtualThread(loadBalancer);
    }

    @Test
    public void allBackendsOk() throws Exception {
        // Give LB and echo time to boot if needed
        Thread.sleep(BOOT_TIME);

        for (int i = 0; i < mockBackends.size(); i++) {
            String message = "ping";

            String response = TestClient.sendAndReceive("localhost", config.getPort(), message);
            assert(response.startsWith(message));
        }
    }

    @Test
    public void oneBackendGoesDown() throws Exception {
        // Give LB and echo time to boot if needed
        Thread.sleep(BOOT_TIME);

        EchoServer server = mockBackends.get(1);
        server.stop();

        Thread.sleep(config.getHealthCheckInterval().toMillis() * 2);

        for (int i = 0; i < mockBackends.size() - 1; i++) {
            String message = "ping";
            String response = TestClient.sendAndReceive("localhost", config.getPort(), message);

            assert(response.startsWith(message) && !response.endsWith(Integer.toString(server.getAddress().getPort())));
        }
    }

    @AfterEach
    public void shutDownLoadBalancerAndServers() {
        loadBalancer.stop();

        for (EchoServer server : mockBackends) {
            server.stop();
        }
    }
}
