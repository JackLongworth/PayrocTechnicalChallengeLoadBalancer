package selection_strategies;

import connections.Backend;
import load_balancers.NoHealthyBackendsException;

import java.util.List;

public interface BackendSelectionStrategy {

    Backend select(List<Backend> targets) throws NoHealthyBackendsException;
}
