package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

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

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Day", dayOfWeek == null ? "" : dayOfWeek.name());
        tag.putString("Start", startsAt == null ? "" : startsAt.toString());
        tag.putString("End", endsAt == null ? "" : endsAt.toString());
        return tag;
    }

    public static BattleWindow fromTag(CompoundTag tag) {
        if (tag == null) return null;
        try {
            DayOfWeek day = DayOfWeek.valueOf(tag.getString("Day").toUpperCase(Locale.ROOT));
            LocalTime start = LocalTime.parse(tag.getString("Start"));
            LocalTime end = LocalTime.parse(tag.getString("End"));
            return new BattleWindow(day, start, end);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            return null;
        }
    }
}
