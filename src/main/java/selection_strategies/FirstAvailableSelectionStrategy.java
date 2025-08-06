package selection_strategies;

import connections.Backend;

import java.util.List;

public class FirstAvailableSelectionStrategy implements BackendSelectionStrategy {

    @Override
    public Backend select(List<Backend> backends) {
        for (Backend backend : backends) {
            if (backend.isHealthy()) {
                return backend;
            }
        }
        throw new RuntimeException("No healthy backends available");
    }
}