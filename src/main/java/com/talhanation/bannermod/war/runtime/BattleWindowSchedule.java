package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    public ListTag toListTag() {
        ListTag list = new ListTag();
        for (BattleWindow window : windows) {
            if (window != null) list.add(window.toTag());
        }
        return list;
    }

    public static BattleWindowSchedule fromListTag(ListTag list) {
        if (list == null || list.isEmpty()) return new BattleWindowSchedule(List.of());
        List<BattleWindow> parsed = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            BattleWindow window = BattleWindow.fromTag(tag);
            if (window != null) parsed.add(window);
        }
        return new BattleWindowSchedule(parsed);
    }

    public static ListTag readListFromCompound(CompoundTag tag, String key) {
        if (tag == null) return new ListTag();
        return tag.getList(key, Tag.TAG_COMPOUND);
    }
}
