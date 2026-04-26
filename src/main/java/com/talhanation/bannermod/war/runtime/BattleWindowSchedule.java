package com.talhanation.bannermod.war.runtime;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public record BattleWindowSchedule(List<BattleWindow> windows) {
    public BattleWindowSchedule {
        windows = windows == null ? List.of() : List.copyOf(windows);
    }

    public static BattleWindowSchedule defaultSchedule() {
        return new BattleWindowSchedule(List.of(
                new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30)),
                new BattleWindow(DayOfWeek.FRIDAY, LocalTime.of(19, 0), LocalTime.of(20, 30)),
                new BattleWindow(DayOfWeek.SUNDAY, LocalTime.of(18, 0), LocalTime.of(19, 30))
        ));
    }

    public boolean isOpen(ZonedDateTime time) {
        for (BattleWindow window : windows) {
            if (window.isOpen(time)) {
                return true;
            }
        }
        return false;
    }
}
