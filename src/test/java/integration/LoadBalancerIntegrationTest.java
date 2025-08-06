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

    static List<EchoServer> echoServers = new ArrayList<>();

    private static Config config;

    @BeforeAll
    public static void setup() throws InterruptedException {
        config = ConfigParser.parseEnvironmentVariablesToConfig();

        for (Backend backends : config.getBackends()) {
            int backendPort = backends.getAddress().getPort();
            EchoServer echoServer = new EchoServer(backendPort);
            echoServers.add(echoServer);
            Thread.ofVirtual().start(echoServer);
        }

        Thread.sleep(2000);

        LoadBalancer loadBalancer = new TcpLoadBalancer(config);
        Thread.startVirtualThread(loadBalancer);

    }

    @Test
    public void simplePingTest() throws Exception {
        // Give LB and echo time to boot if needed
        Thread.sleep(BOOT_TIME);

        for (Backend backend : config.getBackends()) {
            String message = "ping";

            String response = TestClient.sendAndReceive("localhost", config.getPort(), message);
            assert(response.startsWith(message));
        }
    }

    @Disabled
    public void simpleHealthCheckerTest() throws Exception {
        // Give LB and echo time to boot if needed
        Thread.sleep(config.getHealthCheckTimeout().toMillis() * 2);

        EchoServer server = echoServers.get(1);
        server.stop();

        Thread.sleep(config.getHealthCheckTimeout().toMillis() * 2);

        for (int i = 0; i < config.getBackends().size() - 1; i++) {
            String message = "ping";
            String response = TestClient.sendAndReceive("localhost", config.getPort(), message);
            assert(!response.endsWith("9002"));
        }
    }

    @AfterAll
    public static void shutdownServers() {
        for (EchoServer server : echoServers) {
            server.stop();
        }
    }
}
