package com.talhanation.bannermod.war.runtime;

import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

/**
 * Counts rebel-aligned and occupier-aligned actors at a revolt's objective location, so
 * {@link WarRevoltScheduler} can resolve the revolt against actual on-the-ground control
 * instead of timer-driven auto-success.
 *
 * <p>Production wires this through a server-level helper that walks entities in the objective
 * chunk and resolves their political affiliation via {@code PoliticalMembership}. Tests pass
 * a deterministic fake.
 */
@FunctionalInterface
public interface ObjectivePresenceProbe {
    PresenceCounts countAt(ChunkPos objectiveChunk, UUID rebelEntityId, UUID occupierEntityId);

    record PresenceCounts(int rebelCount, int occupierCount) {
        public PresenceCounts {
            rebelCount = Math.max(0, rebelCount);
            occupierCount = Math.max(0, occupierCount);
        }

        public static final PresenceCounts EMPTY = new PresenceCounts(0, 0);
    }
}
