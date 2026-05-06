package com.talhanation.bannermod.network.throttle;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketRateLimiterTest {

    private static final long MS = 1_000_000L;

    private static final class FakeClock {
        final AtomicLong nowNanos = new AtomicLong(0L);

        long get() {
            return nowNanos.get();
        }

        void advanceMillis(long millis) {
            nowNanos.addAndGet(millis * MS);
        }
    }

    private static final class PacketA {
    }

    private static final class PacketB {
    }

    @Test
    void firstCallAfterCooldownAccepts() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 50L);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.tryAcquire(player, PacketA.class), "first call accepts");

        clock.advanceMillis(60L);
        assertTrue(limiter.tryAcquire(player, PacketA.class), "after cooldown, accepts");
        assertEquals(0L, limiter.totalDropped());
    }

    @Test
    void secondCallWithinCooldownIsRejectedAndCounted() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 50L);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.tryAcquire(player, PacketA.class));
        clock.advanceMillis(10L);
        assertFalse(limiter.tryAcquire(player, PacketA.class));
        clock.advanceMillis(10L);
        assertFalse(limiter.tryAcquire(player, PacketA.class));

        assertEquals(2L, limiter.totalDropped());
        assertEquals(2L, limiter.droppedFor(PacketA.class));
        assertEquals(2L, limiter.droppedFor(player));
    }

    @Test
    void perPlayerIsolation() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 50L);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        assertTrue(limiter.tryAcquire(a, PacketA.class));
        assertTrue(limiter.tryAcquire(b, PacketA.class), "different player not rate-limited by player a");
        assertFalse(limiter.tryAcquire(a, PacketA.class));
        assertFalse(limiter.tryAcquire(b, PacketA.class));
        assertEquals(2L, limiter.totalDropped());
        assertEquals(1L, limiter.droppedFor(a));
        assertEquals(1L, limiter.droppedFor(b));
    }

    @Test
    void perPacketClassIsolation() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 50L);
        limiter.setCooldownMillis(PacketB.class, 50L);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.tryAcquire(player, PacketA.class));
        assertTrue(limiter.tryAcquire(player, PacketB.class), "different class not rate-limited by class A");
        assertFalse(limiter.tryAcquire(player, PacketA.class));
        assertFalse(limiter.tryAcquire(player, PacketB.class));
        assertEquals(1L, limiter.droppedFor(PacketA.class));
        assertEquals(1L, limiter.droppedFor(PacketB.class));
    }

    @Test
    void zeroCooldownDisablesThrottling() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 0L);
        UUID player = UUID.randomUUID();

        for (int i = 0; i < 1000; i++) {
            assertTrue(limiter.tryAcquire(player, PacketA.class));
        }
        assertEquals(0L, limiter.totalDropped());
    }

    @Test
    void cooldownSourceTakesPrecedenceOverExplicitMap() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 1L);
        limiter.setCooldownSource(cls -> cls == PacketA.class ? 100L * MS : -1L);
        UUID player = UUID.randomUUID();

        assertTrue(limiter.tryAcquire(player, PacketA.class));
        clock.advanceMillis(10L);
        assertFalse(limiter.tryAcquire(player, PacketA.class), "source overrides 1ms map value with 100ms");
        clock.advanceMillis(120L);
        assertTrue(limiter.tryAcquire(player, PacketA.class));
    }

    @Test
    void floodAtKilohertzAcceptsAtMostTwentyPerSecond() {
        FakeClock clock = new FakeClock();
        PacketRateLimiter limiter = new PacketRateLimiter(clock::get);
        limiter.setCooldownMillis(PacketA.class, 50L);
        UUID player = UUID.randomUUID();

        int accepted = 0;
        // Simulate one packet per millisecond for 1000 ms (1 kHz flood for 1 second).
        for (int i = 0; i < 1000; i++) {
            if (limiter.tryAcquire(player, PacketA.class)) {
                accepted++;
            }
            clock.advanceMillis(1L);
        }

        assertTrue(accepted <= 21, "expected <= 21 accepted at 50ms cooldown over 1s, got " + accepted);
        assertTrue(accepted >= 19, "expected >= 19 accepted at 50ms cooldown over 1s, got " + accepted);
        assertEquals(1000 - accepted, limiter.droppedFor(PacketA.class));
    }
}
