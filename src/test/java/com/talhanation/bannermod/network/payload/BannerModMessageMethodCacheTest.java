package com.talhanation.bannermod.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies REFLCACHE-001: the static {@link BannerModMessage#METHOD_CACHE} ensures
 * {@link Class#getMethod(String, Class[])} fires at most once per
 * (declaring class, method name, parameter type) triple regardless of how many times
 * {@link BannerModMessage#invokeLegacy(String, Class, Object)} is called.
 */
class BannerModMessageMethodCacheTest {

    static final AtomicInteger FROM_BYTES_INVOCATIONS_A = new AtomicInteger();
    static final AtomicInteger FROM_BYTES_INVOCATIONS_B = new AtomicInteger();

    static final class DummyMessageA implements BannerModMessage<DummyMessageA> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public DummyMessageA fromBytes(FriendlyByteBuf buf) {
            FROM_BYTES_INVOCATIONS_A.incrementAndGet();
            return this;
        }
    }

    /** Plain dummy used to exercise the absent-method (one-way packet) cache branch. */
    static final class DummyMessageOneWay implements BannerModMessage<DummyMessageOneWay> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.CLIENTBOUND;
        }
    }

    static final class DummyMessageB implements BannerModMessage<DummyMessageB> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public DummyMessageB fromBytes(FriendlyByteBuf buf) {
            FROM_BYTES_INVOCATIONS_B.incrementAndGet();
            return this;
        }
    }

    @Test
    void invokeLegacyCachesResolvedMethodAcrossManyInvocations() {
        // Clear any state left by other tests so the assertions below are deterministic for our keys.
        BannerModMessage.MethodKey keyA =
                new BannerModMessage.MethodKey(DummyMessageA.class, "fromBytes", FriendlyByteBuf.class);
        BannerModMessage.MethodKey keyB =
                new BannerModMessage.MethodKey(DummyMessageB.class, "fromBytes", FriendlyByteBuf.class);
        BannerModMessage.MethodKey keyAbsent =
                new BannerModMessage.MethodKey(DummyMessageOneWay.class, "noSuchPacketMethod", FriendlyByteBuf.class);
        BannerModMessage.METHOD_CACHE.remove(keyA);
        BannerModMessage.METHOD_CACHE.remove(keyB);
        BannerModMessage.METHOD_CACHE.remove(keyAbsent);

        DummyMessageA messageA = new DummyMessageA();
        DummyMessageB messageB = new DummyMessageB();
        DummyMessageOneWay oneWay = new DummyMessageOneWay();

        FROM_BYTES_INVOCATIONS_A.set(0);
        FROM_BYTES_INVOCATIONS_B.set(0);

        int iterations = 500;
        for (int i = 0; i < iterations; i++) {
            messageA.invokeLegacy("fromBytes", FriendlyByteBuf.class, null);
            messageB.invokeLegacy("fromBytes", FriendlyByteBuf.class, null);
            oneWay.invokeLegacy("noSuchPacketMethod", FriendlyByteBuf.class, null);
        }

        // Body of fromBytes ran every iteration — proves invocation still happens.
        assertTrue(FROM_BYTES_INVOCATIONS_A.get() == iterations,
                "DummyMessageA.fromBytes should have been invoked once per iteration");
        assertTrue(FROM_BYTES_INVOCATIONS_B.get() == iterations,
                "DummyMessageB.fromBytes should have been invoked once per iteration");

        // Cache must hold exactly one Method per (class, name, paramType) triple.
        Method cachedA = BannerModMessage.METHOD_CACHE.get(keyA);
        Method cachedB = BannerModMessage.METHOD_CACHE.get(keyB);
        Method cachedAbsent = BannerModMessage.METHOD_CACHE.get(keyAbsent);

        assertNotNull(cachedA, "Cache must contain resolved method for DummyMessageA after invocation");
        assertNotNull(cachedB, "Cache must contain resolved method for DummyMessageB after invocation");
        assertSame(BannerModMessage.ABSENT_METHOD, cachedAbsent,
                "Cache must memoize ABSENT_METHOD for one-way packets so getMethod isn't re-called");

        // Different owner classes must produce different cached entries (no cross-pollution).
        assertNotSame(cachedA, cachedB, "Cached Method per owner class must be distinct");

        // Repeated lookups return the same cached Method instance — proves no fresh getMethod call occurred.
        for (int i = 0; i < 50; i++) {
            assertSame(cachedA, BannerModMessage.METHOD_CACHE.get(keyA));
            assertSame(cachedB, BannerModMessage.METHOD_CACHE.get(keyB));
            assertSame(cachedAbsent, BannerModMessage.METHOD_CACHE.get(keyAbsent));
        }
    }
}
