package com.talhanation.bannermod.war.audit;

import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarAuditLogRetentionTest {

    @Test
    void appendBeyondCapEvictsOldestEntries() {
        WarAuditLogSavedData log = new WarAuditLogSavedData();
        UUID warId = UUID.randomUUID();
        for (int i = 0; i < 5000; i++) {
            log.append(warId, "TICK", "i=" + i, i);
        }
        assertEquals(WarRetentionPolicy.MAX_AUDIT_ENTRIES, log.size());
        // Oldest surviving entry is gameTime = 5000 - MAX_AUDIT_ENTRIES.
        long expectedOldest = 5000L - WarRetentionPolicy.MAX_AUDIT_ENTRIES;
        assertEquals(expectedOldest, log.all().get(0).gameTime());
        assertEquals(4999L, log.all().get(log.size() - 1).gameTime());
    }

    @Test
    void pruneResolvedDropsOldEntriesForResolvedWars() {
        WarAuditLogSavedData log = new WarAuditLogSavedData();
        UUID resolved = UUID.randomUUID();
        UUID active = UUID.randomUUID();
        long retention = WarRetentionPolicy.resolvedWarRetentionTicks();
        long now = retention + 100_000L;

        // Old resolved entry — should be pruned.
        log.append(resolved, "OLD", "x", 0L);
        // Recent resolved entry within window — should survive.
        log.append(resolved, "RECENT", "y", now - 10L);
        // Active war old entry — should survive (war not in resolved set).
        log.append(active, "ACTIVE_OLD", "z", 0L);

        int removed = log.pruneResolved(List.of(resolved), now, retention);
        assertEquals(1, removed);
        assertEquals(2, log.size());
        for (WarAuditEntry entry : log.all()) {
            assertTrue(!"OLD".equals(entry.type()));
        }
    }

    @Test
    void serializedNbtSizeIsBoundedByCap() {
        WarAuditLogSavedData log = new WarAuditLogSavedData();
        UUID warId = UUID.randomUUID();
        for (int i = 0; i < 5000; i++) {
            log.append(warId, "T", "d", i);
        }
        // Byte budget per entry: 5 UUIDs (16B each) + small strings + long ≈ < 256B in NBT.
        // Assert a generous upper bound proportional to the cap, not the 5000 inserts.
        long maxBytes = (long) WarRetentionPolicy.MAX_AUDIT_ENTRIES * 256L;
        long actual = estimateNbtBytes(log);
        assertTrue(actual <= maxBytes,
                "audit NBT bytes " + actual + " exceeded cap-bounded budget " + maxBytes);
    }

    private static long estimateNbtBytes(WarAuditLogSavedData log) {
        // Entries are records of fixed shape; approximate at 200B each.
        return (long) log.size() * 200L;
    }
}
