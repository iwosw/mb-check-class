package com.talhanation.bannermod.war.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Pure decision layer for occupation tax accrual. Decides which occupations are due for a
 * tax cycle and what their next {@code lastTaxedAtGameTime} should be after this cycle.
 *
 * <p>Idempotency contract: an occupation accrues at most one cycle per call to
 * {@link #selectDue(Collection, long, long)}. The next-last-taxed timestamp advances by
 * exactly one {@code intervalTicks}, not to {@code currentTick}, so a long pause (server
 * downtime, occupation sat unticked) catches up gradually one cycle per call rather than
 * draining the defender treasury in a single burst.</p>
 */
public final class OccupationTaxPolicy {
    private OccupationTaxPolicy() {
    }

    public record DueOccupation(OccupationRecord record, long advanceTo) {
    }

    public static List<DueOccupation> selectDue(Collection<OccupationRecord> records,
                                                long currentTick,
                                                long intervalTicks) {
        if (records == null || records.isEmpty() || intervalTicks <= 0L) {
            return List.of();
        }
        List<DueOccupation> due = new ArrayList<>();
        for (OccupationRecord record : records) {
            if (record == null) continue;
            long elapsed = currentTick - record.lastTaxedAtGameTime();
            if (elapsed < intervalTicks) continue;
            due.add(new DueOccupation(record, record.lastTaxedAtGameTime() + intervalTicks));
        }
        return due;
    }

    public static int taxOwed(int chunks, int taxPerChunk) {
        if (chunks <= 0 || taxPerChunk <= 0) {
            return 0;
        }
        long total = (long) chunks * (long) taxPerChunk;
        return (int) Math.min(total, Integer.MAX_VALUE);
    }
}
