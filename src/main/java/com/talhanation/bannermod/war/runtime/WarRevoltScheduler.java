package com.talhanation.bannermod.war.runtime;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Objective-control resolution of pending {@link RevoltRecord}s during active battle windows.
 *
 * <p>A pending revolt becomes due once {@code scheduledAtGameTime} is reached. The next time
 * the configured battle window is open, every due pending revolt is re-evaluated against the
 * objective location (the first chunk of the underlying occupation): rebel-only presence
 * resolves as {@link RevoltState#SUCCESS} and removes the occupation; any defender presence
 * resolves as {@link RevoltState#FAILED} and audits the loss; an empty objective stays
 * {@link RevoltState#PENDING} so the revolt re-evaluates on the next window pass instead of
 * silently flipping ownership while no one is contesting it.
 */
public final class WarRevoltScheduler {
    private WarRevoltScheduler() {
    }

    /** Pure-logic discovery of pending revolts due for re-evaluation at {@code gameTime}. */
    public static List<UUID> dueRevoltIds(RevoltRuntime revolts, long gameTime, boolean windowOpen) {
        List<UUID> due = new ArrayList<>();
        if (revolts == null || !windowOpen) {
            return due;
        }
        for (RevoltRecord record : revolts.all()) {
            if (record.state() != RevoltState.PENDING) continue;
            if (record.scheduledAtGameTime() > gameTime) continue;
            due.add(record.id());
        }
        return due;
    }

    /**
     * Evaluates every due pending revolt against the {@link ObjectivePresenceProbe} while the
     * battle window is open. Returns the count of revolts that transitioned out of
     * {@link RevoltState#PENDING} on this call (sum of SUCCESS + FAILED).
     */
    public static int tick(RevoltRuntime revolts,
                           OccupationRuntime occupations,
                           WarOutcomeApplier applier,
                           ObjectivePresenceProbe probe,
                           long gameTime,
                           boolean windowOpen) {
        if (revolts == null || occupations == null || applier == null || probe == null || !windowOpen) {
            return 0;
        }
        int resolved = 0;
        for (UUID revoltId : dueRevoltIds(revolts, gameTime, true)) {
            RevoltRecord record = revolts.byId(revoltId).orElse(null);
            if (record == null || record.state() != RevoltState.PENDING) {
                continue;
            }
            OccupationRecord occupation = occupations.byId(record.occupationId()).orElse(null);
            if (occupation == null) {
                // Occupation already gone (manual admin removal, etc.) — drop the revolt as
                // FAILED so it does not linger as PENDING forever and the audit log records
                // the obsolete schedule.
                if (revolts.resolve(record.id(), RevoltState.FAILED, gameTime)) {
                    applier.recordRevoltFailureWithoutOccupation(record, gameTime);
                    resolved++;
                }
                continue;
            }
            ChunkPos objective = occupation.chunks().isEmpty() ? new ChunkPos(0, 0) : occupation.chunks().get(0);
            ObjectivePresenceProbe.PresenceCounts counts = probe.countAt(objective,
                    record.rebelEntityId(), record.occupierEntityId());
            if (counts == null) {
                counts = ObjectivePresenceProbe.PresenceCounts.EMPTY;
            }
            RevoltState outcome = RevoltOutcomePolicy.evaluate(counts.rebelCount(), counts.occupierCount());
            if (outcome == RevoltState.PENDING) {
                continue;
            }
            if (outcome == RevoltState.SUCCESS) {
                boolean removed = applier.removeOccupationOnRevoltSuccess(record.occupationId(), gameTime);
                if (!removed) {
                    continue;
                }
                if (revolts.resolve(record.id(), RevoltState.SUCCESS, gameTime)) {
                    resolved++;
                }
            } else { // FAILED
                if (revolts.resolve(record.id(), RevoltState.FAILED, gameTime)) {
                    applier.recordRevoltFailure(record, occupation, gameTime,
                            counts.rebelCount(), counts.occupierCount());
                    resolved++;
                }
            }
        }
        return resolved;
    }
}
