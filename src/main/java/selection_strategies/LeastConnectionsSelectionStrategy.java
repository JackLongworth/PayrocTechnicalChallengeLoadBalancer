package selection_strategies;

import connections.Backend;
import load_balancers.NoHealthyBackendsException;

import java.util.Comparator;
import java.util.List;

public class LeastConnectionsSelectionStrategy implements BackendSelectionStrategy {


    @Override
    public Backend select(List<Backend> targets) throws NoHealthyBackendsException {
        return targets.stream()
                .filter(Backend::isHealthy)
                .min(Comparator.comparingInt(a -> a.getConnections().intValue()))
                .orElseThrow(NoHealthyBackendsException::new);
    }
}
