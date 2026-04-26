package com.talhanation.bannermod.war.runtime;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public record BattleWindow(DayOfWeek dayOfWeek, LocalTime startsAt, LocalTime endsAt) {
    public boolean isOpen(ZonedDateTime time) {
        if (time == null || dayOfWeek == null || startsAt == null || endsAt == null) {
            return false;
        }
        if (time.getDayOfWeek() != dayOfWeek) {
            return false;
        }
        LocalTime localTime = time.toLocalTime();
        return !localTime.isBefore(startsAt) && localTime.isBefore(endsAt);
    }
}
