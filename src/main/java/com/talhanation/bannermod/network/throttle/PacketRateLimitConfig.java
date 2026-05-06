package com.talhanation.bannermod.network.throttle;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.network.messages.military.MessageAttack;
import com.talhanation.bannermod.network.messages.military.MessageCombatStance;
import com.talhanation.bannermod.network.messages.military.MessageFaceCommand;
import com.talhanation.bannermod.network.messages.military.MessageMovement;

/**
 * Wires {@link PacketRateLimiter#shared()} to the live {@link RecruitsServerConfig}
 * values. Cooldowns are resolved on every call so reload changes apply immediately and
 * the limiter stays usable in unit tests where the config is not loaded.
 */
public final class PacketRateLimitConfig {

    private PacketRateLimitConfig() {
    }

    public static void install() {
        PacketRateLimiter.shared().setCooldownSource(PacketRateLimitConfig::cooldownNanosFor);
    }

    static long cooldownNanosFor(Class<?> packetClass) {
        if (packetClass == MessageMovement.class) {
            return millisOrDefault(RecruitsServerConfig.PacketRateLimitMovementMillis, 50);
        }
        if (packetClass == MessageFaceCommand.class) {
            return millisOrDefault(RecruitsServerConfig.PacketRateLimitFaceMillis, 50);
        }
        if (packetClass == MessageCombatStance.class) {
            return millisOrDefault(RecruitsServerConfig.PacketRateLimitStanceMillis, 100);
        }
        if (packetClass == MessageAttack.class) {
            return millisOrDefault(RecruitsServerConfig.PacketRateLimitAttackMillis, 100);
        }
        // -1 signals "no opinion" so the limiter falls back to its defaults.
        return -1L;
    }

    private static long millisOrDefault(net.neoforged.neoforge.common.ModConfigSpec.IntValue value, int fallbackMillis) {
        int millis = fallbackMillis;
        if (value != null) {
            try {
                millis = value.get();
            } catch (RuntimeException ignored) {
                // config not loaded yet
            }
        }
        return Math.max(0L, (long) millis) * 1_000_000L;
    }
}
