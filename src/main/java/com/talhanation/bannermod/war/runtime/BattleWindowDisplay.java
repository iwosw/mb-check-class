package com.talhanation.bannermod.war.runtime;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Pure formatting helpers for player-facing battle window text. Keeps the {@link
 * BattleWindowClock} pure and lets UI code share one humanizer with future siege
 * HUDs and chat commands.
 */
public final class BattleWindowDisplay {
    private BattleWindowDisplay() {
    }

    /** "1d 2h", "3h 15m", "12m", "45s", "now". Caps at days; never returns negatives. */
    public static String formatDuration(@Nullable Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "now";
        }
        long totalSeconds = duration.getSeconds();
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return hours > 0 ? days + "d " + hours + "h" : days + "d";
        }
        if (hours > 0) {
            return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }

    /** "WED 19:00-20:30" (server local time, day-of-week display name). */
    public static String formatWindow(@Nullable BattleWindow window) {
        if (window == null || window.dayOfWeek() == null
                || window.startsAt() == null || window.endsAt() == null) {
            return "(none)";
        }
        String day = window.dayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ROOT);
        return day.toUpperCase(Locale.ROOT) + " " + window.startsAt() + "-" + window.endsAt();
    }

    /** Single-line summary for the War Room detail panel. */
    public static String formatPhase(@Nullable BattleWindowClock.Phase phase) {
        if (phase == null) {
            return "Battle window: unknown";
        }
        if (phase instanceof BattleWindowClock.Phase.Open open) {
            return "Battle window: OPEN " + formatWindow(open.window())
                    + " — closes in " + formatDuration(open.timeUntilTransition());
        }
        if (phase instanceof BattleWindowClock.Phase.Closed closed) {
            if (closed.window() == null) {
                return "Battle window: not scheduled";
            }
            return "Battle window: CLOSED — next " + formatWindow(closed.window())
                    + " in " + formatDuration(closed.timeUntilTransition());
        }
        return "Battle window: unknown";
    }
}
