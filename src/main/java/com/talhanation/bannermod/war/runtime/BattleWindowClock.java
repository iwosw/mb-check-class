package com.talhanation.bannermod.war.runtime;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Pure-logic helper that turns a {@link BattleWindowSchedule} into a {@link Phase}
 * describing whether a battle window is currently open and how long until the next
 * transition. Used by the client HUD and any tooling that needs human-readable
 * countdowns. Time arguments must carry the same {@link java.time.ZoneId} as the
 * server's local clock (mirrors how {@link BattleWindow#isOpen(ZonedDateTime)} is
 * called from {@code WarPvpEvents}).
 *
 * <p>Assumes individual windows do not cross midnight — matching the schedule grammar
 * accepted by {@code WarServerConfig#parseWindow}.
 */
public final class BattleWindowClock {
    private BattleWindowClock() {
    }

    /** Active phase of the schedule at a given instant. */
    public sealed interface Phase {
        @Nullable BattleWindow window();
        Duration timeUntilTransition();

        record Open(BattleWindow window, Duration untilClose) implements Phase {
            @Override public Duration timeUntilTransition() { return untilClose; }
        }

        record Closed(@Nullable BattleWindow nextWindow, Duration untilOpen) implements Phase {
            @Override public BattleWindow window() { return nextWindow; }
            @Override public Duration timeUntilTransition() { return untilOpen; }
        }
    }

    public static Phase compute(BattleWindowSchedule schedule, ZonedDateTime now) {
        if (schedule == null || now == null) {
            return new Phase.Closed(null, Duration.ZERO);
        }
        for (BattleWindow window : schedule.windows()) {
            if (window.isOpen(now)) {
                ZonedDateTime closeAt = closeOf(window, now);
                Duration untilClose = closeAt.isAfter(now) ? Duration.between(now, closeAt) : Duration.ZERO;
                return new Phase.Open(window, untilClose);
            }
        }
        BattleWindow soonest = null;
        ZonedDateTime soonestOpen = null;
        for (BattleWindow window : schedule.windows()) {
            ZonedDateTime nextOpen = nextOpen(window, now);
            if (soonestOpen == null || nextOpen.isBefore(soonestOpen)) {
                soonestOpen = nextOpen;
                soonest = window;
            }
        }
        if (soonest == null) {
            return new Phase.Closed(null, Duration.ZERO);
        }
        return new Phase.Closed(soonest, Duration.between(now, soonestOpen));
    }

    /** Today's date at the window's end time, in the same zone as {@code now}. */
    public static ZonedDateTime closeOf(BattleWindow window, ZonedDateTime now) {
        return now.toLocalDate().atTime(window.endsAt()).atZone(now.getZone());
    }

    /**
     * Earliest {@code ZonedDateTime} strictly after {@code now} when {@code window} opens.
     * If today matches the window's day-of-week and start time hasn't passed, returns today's open.
     * Otherwise returns the next weekly occurrence.
     */
    public static ZonedDateTime nextOpen(BattleWindow window, ZonedDateTime now) {
        int daysAhead = (window.dayOfWeek().getValue() - now.getDayOfWeek().getValue() + 7) % 7;
        ZonedDateTime candidate = now.toLocalDate()
                .atTime(window.startsAt())
                .atZone(now.getZone())
                .plusDays(daysAhead);
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(7);
        }
        return candidate;
    }
}
