package com.talhanation.bannermod.war.runtime;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleWindowClockTest {
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void openWindowReturnsRemainingDuration() {
        BattleWindow wed = new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of(wed));
        ZonedDateTime now = ZonedDateTime.of(2026, 4, 22, 19, 30, 0, 0, UTC); // wed 19:30

        BattleWindowClock.Phase phase = BattleWindowClock.compute(schedule, now);

        assertTrue(phase instanceof BattleWindowClock.Phase.Open);
        assertEquals(Duration.ofMinutes(60), phase.timeUntilTransition());
        assertEquals(wed, phase.window());
    }

    @Test
    void closedScheduleReturnsNextOpening() {
        BattleWindow wed = new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        BattleWindow fri = new BattleWindow(DayOfWeek.FRIDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of(wed, fri));
        ZonedDateTime now = ZonedDateTime.of(2026, 4, 22, 21, 0, 0, 0, UTC); // wed after close

        BattleWindowClock.Phase phase = BattleWindowClock.compute(schedule, now);

        assertTrue(phase instanceof BattleWindowClock.Phase.Closed);
        assertEquals(fri, phase.window());
        assertEquals(Duration.ofHours(46), phase.timeUntilTransition());
    }

    @Test
    void todayBeforeOpenReturnsTodayOpen() {
        BattleWindow wed = new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of(wed));
        ZonedDateTime now = ZonedDateTime.of(2026, 4, 22, 18, 0, 0, 0, UTC); // wed 18:00

        BattleWindowClock.Phase phase = BattleWindowClock.compute(schedule, now);

        assertTrue(phase instanceof BattleWindowClock.Phase.Closed);
        assertEquals(Duration.ofHours(1), phase.timeUntilTransition());
    }

    @Test
    void emptyScheduleReturnsClosedZeroDuration() {
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of());
        ZonedDateTime now = ZonedDateTime.of(2026, 4, 22, 18, 0, 0, 0, UTC);

        BattleWindowClock.Phase phase = BattleWindowClock.compute(schedule, now);

        assertTrue(phase instanceof BattleWindowClock.Phase.Closed);
        assertNull(phase.window());
        assertEquals(Duration.ZERO, phase.timeUntilTransition());
    }

    @Test
    void afterFinalWindowSelectsNextWeekOpening() {
        BattleWindow sun = new BattleWindow(DayOfWeek.SUNDAY, LocalTime.of(18, 0), LocalTime.of(19, 30));
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of(sun));
        ZonedDateTime now = ZonedDateTime.of(2026, 4, 26, 20, 0, 0, 0, UTC); // sun after close

        BattleWindowClock.Phase phase = BattleWindowClock.compute(schedule, now);

        assertTrue(phase instanceof BattleWindowClock.Phase.Closed);
        assertNotNull(phase.window());
        assertEquals(Duration.ofDays(6).plusHours(22), phase.timeUntilTransition());
    }
}
