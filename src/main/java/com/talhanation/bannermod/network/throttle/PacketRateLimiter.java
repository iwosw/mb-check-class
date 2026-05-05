package com.talhanation.bannermod.network.throttle;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;
import java.util.function.ToLongFunction;

/**
 * Per-player, per-packet-class rate limiter for serverbound BannerMod packets.
 *
 * <p>The limiter is intentionally Minecraft-free so it can be exercised in unit tests
 * without booting the runtime. Internally it keeps a {@link ConcurrentHashMap} keyed
 * on {@code (playerUuid, packetClass)} pairs holding the last accepted timestamp in
 * nanoseconds. {@link #tryAcquire(UUID, Class)} returns {@code true} when the call
 * site should process the packet and {@code false} when it must be dropped.</p>
 *
 * <p>Drops are counted both per-player and per-class so live ops can inspect spam
 * via {@link #droppedFor(UUID, Class)}, {@link #droppedFor(Class)} and
 * {@link #totalDropped()}.</p>
 */
public final class PacketRateLimiter {

    /** Default cooldown window in nanoseconds when no class-specific override is set. */
    public static final long DEFAULT_COOLDOWN_NANOS = 50L * 1_000_000L; // 50 ms

    /** Singleton used by network messages; tests should construct their own instance. */
    private static final PacketRateLimiter SHARED = new PacketRateLimiter();

    private final ConcurrentHashMap<Class<?>, Long> cooldownByClass = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Key, Long> lastAcceptedNanos = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, LongAdder> droppedByClass = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, LongAdder> droppedByPlayer = new ConcurrentHashMap<>();
    private final LongAdder totalDropped = new LongAdder();
    private final LongSupplier clock;
    private volatile ToLongFunction<Class<?>> cooldownSource;

    public PacketRateLimiter() {
        this(System::nanoTime);
    }

    public PacketRateLimiter(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Singleton used by production packet handlers. */
    public static PacketRateLimiter shared() {
        return SHARED;
    }

    /**
     * Configure the cooldown window for a specific packet class. {@code cooldownNanos < 0}
     * clears the explicit override and falls back to {@link #DEFAULT_COOLDOWN_NANOS};
     * {@code cooldownNanos == 0} disables throttling for that class.
     */
    public void setCooldownNanos(Class<?> packetClass, long cooldownNanos) {
        Objects.requireNonNull(packetClass, "packetClass");
        if (cooldownNanos < 0L) {
            cooldownByClass.remove(packetClass);
        } else {
            cooldownByClass.put(packetClass, cooldownNanos);
        }
    }

    /** Convenience for callers that prefer milliseconds. */
    public void setCooldownMillis(Class<?> packetClass, long cooldownMillis) {
        setCooldownNanos(packetClass, cooldownMillis * 1_000_000L);
    }

    /**
     * Install a function that returns the cooldown (in nanoseconds) for a given packet
     * class. When set, this takes precedence over {@link #setCooldownNanos(Class, long)}.
     * Use {@code null} to clear and fall back to the explicit map / default.
     */
    public void setCooldownSource(ToLongFunction<Class<?>> source) {
        this.cooldownSource = source;
    }

    long resolveCooldownNanos(Class<?> packetClass) {
        ToLongFunction<Class<?>> source = this.cooldownSource;
        if (source != null) {
            try {
                long fromSource = source.applyAsLong(packetClass);
                if (fromSource > 0L) return fromSource;
                if (fromSource == 0L) return 0L; // explicit disable
                // negative -> fall through to per-class map
            } catch (RuntimeException ignored) {
                // config not yet loaded etc.; fall through
            }
        }
        return cooldownByClass.getOrDefault(packetClass, DEFAULT_COOLDOWN_NANOS);
    }

    /**
     * Drop all per-player state. Configured cooldowns survive. Counters survive too;
     * use {@link #resetCounters()} for a full wipe.
     */
    public void clearState() {
        lastAcceptedNanos.clear();
    }

    public void resetCounters() {
        droppedByClass.clear();
        droppedByPlayer.clear();
        totalDropped.reset();
    }

    /**
     * Returns {@code true} if the caller may process the packet, {@code false} if it
     * must drop it. The first call for a given {@code (player, class)} pair always
     * accepts; subsequent calls inside the cooldown window are rejected and counted.
     */
    public boolean tryAcquire(UUID playerUuid, Class<?> packetClass) {
        if (playerUuid == null || packetClass == null) {
            // Without identity we cannot rate-limit; fail open so internal/system traffic still works.
            return true;
        }
        long cooldownNanos = resolveCooldownNanos(packetClass);
        if (cooldownNanos <= 0L) {
            return true;
        }
        long now = clock.getAsLong();
        Key key = new Key(playerUuid, packetClass);
        Long previous = lastAcceptedNanos.get(key);
        if (previous == null) {
            return lastAcceptedNanos.putIfAbsent(key, now) == null
                    || tryReplace(key, now, cooldownNanos, playerUuid, packetClass);
        }
        return tryReplace(key, now, cooldownNanos, playerUuid, packetClass);
    }

    private boolean tryReplace(Key key, long now, long cooldownNanos, UUID playerUuid, Class<?> packetClass) {
        while (true) {
            Long current = lastAcceptedNanos.get(key);
            if (current == null) {
                if (lastAcceptedNanos.putIfAbsent(key, now) == null) {
                    return true;
                }
                continue;
            }
            if (now - current < cooldownNanos) {
                recordDrop(playerUuid, packetClass);
                return false;
            }
            if (lastAcceptedNanos.replace(key, current, now)) {
                return true;
            }
        }
    }

    private void recordDrop(UUID playerUuid, Class<?> packetClass) {
        totalDropped.increment();
        droppedByClass.computeIfAbsent(packetClass, ignored -> new LongAdder()).increment();
        droppedByPlayer.computeIfAbsent(playerUuid, ignored -> new LongAdder()).increment();
    }

    public long droppedFor(UUID playerUuid, Class<?> packetClass) {
        long byPlayer = droppedByPlayer.getOrDefault(playerUuid, ZERO).sum();
        long byClass = droppedByClass.getOrDefault(packetClass, ZERO).sum();
        // The intersection isn't tracked separately; expose the smaller side as a conservative estimate.
        return Math.min(byPlayer, byClass);
    }

    public long droppedFor(Class<?> packetClass) {
        return droppedByClass.getOrDefault(packetClass, ZERO).sum();
    }

    public long droppedFor(UUID playerUuid) {
        return droppedByPlayer.getOrDefault(playerUuid, ZERO).sum();
    }

    public long totalDropped() {
        return totalDropped.sum();
    }

    public Map<Class<?>, Long> snapshotByClass() {
        ConcurrentHashMap<Class<?>, Long> out = new ConcurrentHashMap<>();
        for (Map.Entry<Class<?>, LongAdder> entry : droppedByClass.entrySet()) {
            out.put(entry.getKey(), entry.getValue().sum());
        }
        return out;
    }

    private static final LongAdder ZERO = new LongAdder();

    private record Key(UUID playerUuid, Class<?> packetClass) {
    }
}
