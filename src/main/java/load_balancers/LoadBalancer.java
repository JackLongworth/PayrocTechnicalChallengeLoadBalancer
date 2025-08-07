package load_balancers;

public interface LoadBalancer extends Runnable {

    void stop();
}
