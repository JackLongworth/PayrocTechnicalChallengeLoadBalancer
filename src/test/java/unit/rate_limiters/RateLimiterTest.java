package unit.rate_limiters;

import org.junit.jupiter.api.Test;
import rate_limiters.RateLimiter;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class RateLimiterTest {

    private final static int MAX_REQUESTS = 10;

    RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, 60000);

    @Test
    void rateLimiterExceedsMaximumConnections() {
        InetAddress address = mock(InetAddress.class);
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allow(address));
        }

        assertFalse(rateLimiter.allow(address));
    }

    @Test
    void rateLimiterLessThanMaxConnections() {
        final int WITHIN_MAX = MAX_REQUESTS - (MAX_REQUESTS / 2);

        InetAddress address = mock(InetAddress.class);
        for (int i = 0; i < WITHIN_MAX - 1; i++) {
            assertTrue(rateLimiter.allow(address));
        }
    }

    @Test
    void rateLimiterExceedsMaxConnectionsWithTwoDifferentAddresses() {
        InetAddress address1 = mock(InetAddress.class);
        InetAddress address2 = mock(InetAddress.class);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allow(address1));
            assertTrue(rateLimiter.allow(address2));
        }

        assertFalse(rateLimiter.allow(address1));
        assertFalse(rateLimiter.allow(address2));
    }



    @Test
    void rateLimiterExceedsMaxConnectionsWithOneAddressAndDoesntWithAnother() {
        InetAddress address1 = mock(InetAddress.class);
        InetAddress address2 = mock(InetAddress.class);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allow(address1));
        }

        for (int i = 0; i < MAX_REQUESTS / 2; i++) {
            assertTrue(rateLimiter.allow(address2));
        }

        assertFalse(rateLimiter.allow(address1));
    }
}
