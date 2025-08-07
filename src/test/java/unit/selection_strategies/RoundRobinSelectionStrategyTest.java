package unit.selection_strategies;


import connections.Backend;
import org.junit.jupiter.api.Test;
import selection_strategies.BackendSelectionStrategy;
import selection_strategies.RoundRobinSelectionStrategy;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoundRobinSelectionStrategyTest {

    BackendSelectionStrategy roundRobin = new RoundRobinSelectionStrategy();

    @Test
    public void selectOneBackend() {
        List<Backend> mockBackends = new ArrayList<>();

        final int MAX = 10;

        for (int i = 0; i < MAX; i++) {
            Backend mockBackend = mock(Backend.class);
            when(mockBackend.getAddress()).thenReturn(new InetSocketAddress("localhost", 9001 + i));
            when(mockBackend.isHealthy()).thenReturn(true);
            mockBackends.add(mockBackend);
        }

        for (Backend backend : mockBackends) {
            assertEquals(backend.getAddress(), roundRobin.select(mockBackends).getAddress());
        }
    }
}
