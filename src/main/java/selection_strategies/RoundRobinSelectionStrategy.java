package selection_strategies;

import connections.Backend;
import load_balancers.NoHealthyBackendsException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinSelectionStrategy implements BackendSelectionStrategy {

    private static final AtomicInteger current = new AtomicInteger();


    public RoundRobinSelectionStrategy() {
        current.set(0);
    }

    @Override
    public Backend select(List<Backend> targets) {
        List<Backend> healthyTargets = targets.stream().filter(Backend::isHealthy).toList();
        if (healthyTargets.isEmpty()) {
            throw new NoHealthyBackendsException();
        }

        return healthyTargets.get(current.getAndIncrement() % healthyTargets.size());
    }
}
