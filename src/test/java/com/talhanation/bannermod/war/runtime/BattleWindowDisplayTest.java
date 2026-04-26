package com.talhanation.bannermod.war.runtime;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BattleWindowDisplayTest {

    @Test
    void formatsZeroAsNow() {
        assertEquals("now", BattleWindowDisplay.formatDuration(null));
        assertEquals("now", BattleWindowDisplay.formatDuration(Duration.ZERO));
        assertEquals("now", BattleWindowDisplay.formatDuration(Duration.ofSeconds(-5)));
    }

    @Test
    void formatsBelowOneMinuteAsSeconds() {
        assertEquals("45s", BattleWindowDisplay.formatDuration(Duration.ofSeconds(45)));
    }

    @Test
    void formatsMinutesOnly() {
        assertEquals("12m", BattleWindowDisplay.formatDuration(Duration.ofMinutes(12)));
    }

    @Test
    void formatsHoursAndMinutes() {
        assertEquals("3h 15m", BattleWindowDisplay.formatDuration(Duration.ofMinutes(195)));
    }

    @Test
    void formatsDaysAndHours() {
        assertEquals("2d 3h", BattleWindowDisplay.formatDuration(Duration.ofHours(51)));
    }

    @Test
    void dropsMinutesOnceDaysAppear() {
        assertEquals("2d", BattleWindowDisplay.formatDuration(Duration.ofDays(2).plusMinutes(7)));
    }

    @Test
    void formatsWindowDayAndTimes() {
        BattleWindow window = new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        assertEquals("WED 19:00-20:30", BattleWindowDisplay.formatWindow(window));
    }

    @Test
    void formatsOpenPhaseLine() {
        BattleWindow window = new BattleWindow(DayOfWeek.FRIDAY, LocalTime.of(19, 0), LocalTime.of(20, 30));
        BattleWindowClock.Phase phase = new BattleWindowClock.Phase.Open(window, Duration.ofMinutes(45));
        assertEquals(
                "Battle window: OPEN FRI 19:00-20:30 — closes in 45m",
                BattleWindowDisplay.formatPhase(phase));
    }

    @Test
    void formatsClosedPhaseLine() {
        BattleWindow window = new BattleWindow(DayOfWeek.SUNDAY, LocalTime.of(18, 0), LocalTime.of(19, 30));
        BattleWindowClock.Phase phase = new BattleWindowClock.Phase.Closed(window, Duration.ofHours(46));
        assertEquals(
                "Battle window: CLOSED — next SUN 18:00-19:30 in 1d 22h",
                BattleWindowDisplay.formatPhase(phase));
    }

    @Test
    void formatsEmptyClosedPhaseAsUnscheduled() {
        BattleWindowClock.Phase phase = new BattleWindowClock.Phase.Closed(null, Duration.ZERO);
        assertEquals("Battle window: not scheduled", BattleWindowDisplay.formatPhase(phase));
    }
}
