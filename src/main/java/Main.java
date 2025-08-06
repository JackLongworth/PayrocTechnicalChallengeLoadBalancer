import config.Config;
import config.ConfigParser;
import load_balancers.LoadBalancer;
import load_balancers.TcpLoadBalancer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Main {

    public static void main(String[] args) {
        Config config = ConfigParser.parseEnvironmentVariablesToConfig();

        LoadBalancer loadBalancer = new TcpLoadBalancer(config);
        Thread loadBalancerThread = Thread.startVirtualThread(loadBalancer);
        try {
            loadBalancerThread.join();
        } catch (InterruptedException e) {
            log.error("Load balancer thread was interrupted whilst trying to join");
        }
    }
}
