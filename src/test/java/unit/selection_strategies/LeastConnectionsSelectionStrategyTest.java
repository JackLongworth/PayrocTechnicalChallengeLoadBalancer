package unit.selection_strategies;

import ch.qos.logback.core.testUtil.RandomUtil;
import connections.Backend;
import org.junit.jupiter.api.Test;
import selection_strategies.BackendSelectionStrategy;
import selection_strategies.LeastConnectionsSelectionStrategy;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeastConnectionsSelectionStrategyTest {

    BackendSelectionStrategy leastConnectionsSelectionStrategy = new LeastConnectionsSelectionStrategy();

    @Test
    void selectOneBackend() {
        List<Backend> mockBackends = new ArrayList<>();

        final int MAX = 10;
        Map<InetSocketAddress, Integer> connections = new HashMap<>(MAX);

        for (int i = 0; i < MAX; i++) {
            Backend mockBackend = mock(Backend.class);
            InetSocketAddress mockAddr = new InetSocketAddress("localhost", 9001 + i);
            AtomicInteger mockConnectionAmount = new AtomicInteger(RandomUtil.getPositiveInt() % 50);

            when(mockBackend.getAddress()).thenReturn(mockAddr);
            when(mockBackend.getConnections()).thenReturn(mockConnectionAmount);
            when(mockBackend.isHealthy()).thenReturn(true);
            connections.put(mockAddr, mockConnectionAmount.intValue());
            mockBackends.add(mockBackend);
        }

        Map.Entry<InetSocketAddress, Integer> expected = connections.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .orElseThrow();

        assertEquals(expected.getKey(), leastConnectionsSelectionStrategy.select(mockBackends).getAddress());
    }
}
