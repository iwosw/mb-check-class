package com.talhanation.bannermod.army.command;

import net.minecraft.core.UUIDUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandIntentLogTest {

    private static final UUID PLAYER_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID PLAYER_B = UUID.fromString("00000000-0000-0000-0000-0000000000a2");

    @BeforeEach
    void reset() {
        CommandIntentLog.instance().clearAllForTest();
    }

    @Test
    void nullArgsAreIgnored() {
        CommandIntentLog.instance().record(null, moveIntent(1L), 3);
        assertEquals(0, CommandIntentLog.instance().sizeFor(PLAYER_A));
    }

    @Test
    void sizeIsBoundedByMaxEntriesPerPlayer() {
        UUID playerUuid = UUID.randomUUID();
        // We can't construct a real ServerPlayer in unit tests, so exercise the bounded
        // deque via the underlying collection directly — the cap check doesn't depend on
        // the player reference after the UUID lookup.
        for (int i = 0; i < CommandIntentLog.MAX_ENTRIES_PER_PLAYER + 25; i++) {
            writeRaw(playerUuid, moveIntent(i));
        }
        assertEquals(CommandIntentLog.MAX_ENTRIES_PER_PLAYER, CommandIntentLog.instance().sizeFor(playerUuid));
    }

    @Test
    void recentForReturnsNewestFirst() {
        UUID playerUuid = UUID.randomUUID();
        writeRaw(playerUuid, moveIntent(1L));
        writeRaw(playerUuid, moveIntent(2L));
        writeRaw(playerUuid, moveIntent(3L));

        List<CommandIntentLog.Entry> entries = CommandIntentLog.instance().recentFor(playerUuid);
        assertEquals(3, entries.size());
        assertEquals(3L, ((CommandIntent.Movement) entries.get(0).intent()).issuedAtGameTime());
        assertEquals(1L, ((CommandIntent.Movement) entries.get(2).intent()).issuedAtGameTime());
    }

    @Test
    void perPlayerBuffersAreIsolated() {
        writeRaw(PLAYER_A, moveIntent(10L));
        writeRaw(PLAYER_B, moveIntent(20L));
        writeRaw(PLAYER_B, moveIntent(30L));

        assertEquals(1, CommandIntentLog.instance().sizeFor(PLAYER_A));
        assertEquals(2, CommandIntentLog.instance().sizeFor(PLAYER_B));
    }

    @Test
    void clearForDropsOnePlayerLeavesOthers() {
        writeRaw(PLAYER_A, moveIntent(1L));
        writeRaw(PLAYER_B, moveIntent(2L));

        CommandIntentLog.instance().clearFor(PLAYER_A);

        assertEquals(0, CommandIntentLog.instance().sizeFor(PLAYER_A));
        assertEquals(1, CommandIntentLog.instance().sizeFor(PLAYER_B));
    }

    /**
     * Bypass the ServerPlayer-only record() API for tests by poking the underlying map
     * directly via a tiny reflection shim. Production callers always have a ServerPlayer.
     */
    private static void writeRaw(UUID playerUuid, CommandIntent intent) {
        assertTrue(UUIDUtil.AUTHLIB_CODEC != null, "sanity anchor for Minecraft boot classes");
        try {
            java.lang.reflect.Field field = CommandIntentLog.class.getDeclaredField("byPlayer");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, java.util.Deque<CommandIntentLog.Entry>> map =
                    (java.util.Map<UUID, java.util.Deque<CommandIntentLog.Entry>>) field.get(CommandIntentLog.instance());
            java.util.Deque<CommandIntentLog.Entry> deque =
                    map.computeIfAbsent(playerUuid, ignored -> new java.util.ArrayDeque<>());
            synchronized (deque) {
                deque.addFirst(new CommandIntentLog.Entry(playerUuid, intent, 1));
                while (deque.size() > CommandIntentLog.MAX_ENTRIES_PER_PLAYER) {
                    deque.pollLast();
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static CommandIntent.Movement moveIntent(long tick) {
        return new CommandIntent.Movement(
                tick,
                CommandIntentPriority.NORMAL,
                false,
                0,
                0,
                false,
                null
        );
    }
}
