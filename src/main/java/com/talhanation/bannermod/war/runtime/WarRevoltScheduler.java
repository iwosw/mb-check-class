package com.talhanation.bannermod.war.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Auto-resolution policy for pending {@link RevoltRecord}s during active battle windows.
 *
 * <p>A pending revolt becomes due once {@code scheduledAtGameTime} is reached. The next time
 * the configured {@link BattleWindowSchedule} is open, every due pending revolt resolves as
 * {@link RevoltState#SUCCESS}: the underlying occupation is removed and the revolt record
 * is updated. Admin commands remain available as a manual override.
 */
public final class WarRevoltScheduler {
    private WarRevoltScheduler() {
    }

    /** Pure-logic discovery of pending revolts due for auto-resolution at {@code gameTime}. */
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
     * Resolves every due pending revolt while the battle window is open. Returns the count of
     * revolts that were successfully transitioned to {@link RevoltState#SUCCESS}.
     */
    public static int tick(RevoltRuntime revolts,
                           WarOutcomeApplier applier,
                           long gameTime,
                           boolean windowOpen) {
        if (revolts == null || applier == null || !windowOpen) {
            return 0;
        }
        int resolved = 0;
        for (UUID revoltId : dueRevoltIds(revolts, gameTime, true)) {
            RevoltRecord record = revolts.byId(revoltId).orElse(null);
            if (record == null || record.state() != RevoltState.PENDING) {
                continue;
            }
            boolean removed = applier.removeOccupationOnRevoltSuccess(record.occupationId(), gameTime);
            if (!removed) {
                continue;
            }
            if (revolts.resolve(record.id(), RevoltState.SUCCESS, gameTime)) {
                resolved++;
            }
        }
        return resolved;
    }
}
