package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BattleWindowScheduleNbtTest {

    @Test
    void roundTripsThroughListTag() {
        BattleWindowSchedule schedule = new BattleWindowSchedule(List.of(
                new BattleWindow(DayOfWeek.WEDNESDAY, LocalTime.of(19, 0), LocalTime.of(20, 30)),
                new BattleWindow(DayOfWeek.SUNDAY, LocalTime.of(18, 0), LocalTime.of(19, 30))
        ));

        ListTag list = schedule.toListTag();
        BattleWindowSchedule decoded = BattleWindowSchedule.fromListTag(list);

        assertEquals(schedule.windows(), decoded.windows());
    }

    @Test
    void emptyListDecodesToEmptySchedule() {
        BattleWindowSchedule decoded = BattleWindowSchedule.fromListTag(new ListTag());
        assertTrue(decoded.windows().isEmpty());
    }

    @Test
    void nullListDecodesToEmptySchedule() {
        BattleWindowSchedule decoded = BattleWindowSchedule.fromListTag(null);
        assertTrue(decoded.windows().isEmpty());
    }

    @Test
    void malformedEntriesAreSkipped() {
        ListTag list = new ListTag();
        // Valid entry
        list.add(new BattleWindow(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0)).toTag());
        // Invalid entry — bad day
        CompoundTag bad = new CompoundTag();
        bad.putString("Day", "NOTADAY");
        bad.putString("Start", "08:00");
        bad.putString("End", "09:00");
        list.add(bad);

        BattleWindowSchedule decoded = BattleWindowSchedule.fromListTag(list);

        assertEquals(1, decoded.windows().size());
        assertEquals(DayOfWeek.MONDAY, decoded.windows().get(0).dayOfWeek());
    }

    @Test
    void invalidTagReturnsNullWindow() {
        CompoundTag bad = new CompoundTag();
        bad.putString("Day", "WEDNESDAY");
        bad.putString("Start", "not-a-time");
        bad.putString("End", "20:30");
        assertNull(BattleWindow.fromTag(bad));
    }
}
