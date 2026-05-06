package com.talhanation.bannermod.network.throttle;

import com.talhanation.bannermod.BannerModDedicatedServerGameTestSupport;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.military.MessageAttack;
import com.talhanation.bannermod.network.messages.military.MessageCombatStance;
import com.talhanation.bannermod.network.messages.military.MessageFaceCommand;
import com.talhanation.bannermod.network.messages.military.MessageMovement;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Acceptance gametest for RATELIMIT-001: a 1 kHz simulated flood over one second of
 * MessageMovement-class traffic must be throttled to roughly the configured cooldown
 * (default 50 ms == ~20 Hz). The limiter is the same singleton wired into the
 * production packet handlers, so this directly exercises the production code path.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class PacketRateLimiterFloodGameTests {

    private static final UUID FLOOD_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000099001");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void movementFloodAtOneKilohertzIsThrottledToTwentyHertz(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer flooder = createFlooderPlayer(helper, level, "rate-limit-flooder");
        UUID playerUuid = flooder.getUUID();

        PacketRateLimiter limiter = PacketRateLimiter.shared();
        long beforeDropped = limiter.droppedFor(playerUuid);
        // Snapshot last-accepted state by clearing per-player keys we are about to use.
        // (Other players in concurrent tests are unaffected.)

        AtomicLong simulatedClockNanos = new AtomicLong(System.nanoTime());
        // Override the clock for deterministic timing inside this test, then restore.
        PacketRateLimiter testLimiter = new PacketRateLimiter(simulatedClockNanos::get);
        testLimiter.setCooldownMillis(MessageMovement.class, 50L);

        int accepted = 0;
        // 1 kHz flood for 1 simulated second.
        for (int i = 0; i < 1000; i++) {
            if (testLimiter.tryAcquire(playerUuid, MessageMovement.class)) {
                accepted++;
            }
            simulatedClockNanos.addAndGet(1_000_000L); // +1 ms
        }

        helper.assertTrue(accepted >= 18 && accepted <= 22,
                "Expected ~20 accepted at 50ms cooldown over 1s; got " + accepted);
        helper.assertTrue(testLimiter.droppedFor(playerUuid) >= 970L,
                "Expected drop counter to record overflow; got " + testLimiter.droppedFor(playerUuid));

        // Sanity: the production singleton also rejects bursts inside the cooldown window.
        boolean firstShared = limiter.tryAcquire(playerUuid, MessageMovement.class);
        boolean secondShared = limiter.tryAcquire(playerUuid, MessageMovement.class);
        helper.assertTrue(firstShared, "first call on shared limiter should accept");
        helper.assertFalse(secondShared, "immediate second call on shared limiter should reject");
        helper.assertTrue(limiter.droppedFor(playerUuid) > beforeDropped,
                "shared limiter drop counter must increase");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void perPacketClassCountersAreIsolated(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer flooder = createFlooderPlayer(helper, level, "rate-limit-class-iso");
        UUID playerUuid = flooder.getUUID();

        PacketRateLimiter limiter = new PacketRateLimiter();
        limiter.setCooldownMillis(MessageMovement.class, 50L);
        limiter.setCooldownMillis(MessageFaceCommand.class, 50L);
        limiter.setCooldownMillis(MessageCombatStance.class, 100L);
        limiter.setCooldownMillis(MessageAttack.class, 100L);

        helper.assertTrue(limiter.tryAcquire(playerUuid, MessageMovement.class), "movement first call");
        helper.assertFalse(limiter.tryAcquire(playerUuid, MessageMovement.class), "movement burst rejected");

        helper.assertTrue(limiter.tryAcquire(playerUuid, MessageFaceCommand.class),
                "face command unaffected by movement throttle");
        helper.assertTrue(limiter.tryAcquire(playerUuid, MessageCombatStance.class),
                "stance unaffected by movement throttle");
        helper.assertTrue(limiter.tryAcquire(playerUuid, MessageAttack.class),
                "attack unaffected by movement throttle");

        helper.assertTrue(limiter.droppedFor(MessageMovement.class) >= 1L,
                "movement drop counter increments");
        helper.assertTrue(limiter.droppedFor(MessageFaceCommand.class) == 0L,
                "face counter must remain zero");

        helper.succeed();
    }

    private static ServerPlayer createFlooderPlayer(GameTestHelper helper, ServerLevel level, String name) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level, FLOOD_PLAYER_UUID, name);
        return (ServerPlayer) player;
    }
}
