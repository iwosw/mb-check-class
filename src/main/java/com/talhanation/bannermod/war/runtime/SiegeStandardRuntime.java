package com.talhanation.bannermod.war.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SiegeStandardRuntime {
    private final Map<UUID, SiegeStandardRecord> standardsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public Optional<SiegeStandardRecord> place(UUID warId,
                                               UUID sidePoliticalEntityId,
                                               BlockPos pos,
                                               int radius,
                                               long gameTime) {
        if (warId == null || sidePoliticalEntityId == null || pos == null) {
            return Optional.empty();
        }
        SiegeStandardRecord record = new SiegeStandardRecord(
                UUID.randomUUID(),
                warId,
                sidePoliticalEntityId,
                pos.immutable(),
                radius,
                gameTime
        );
        standardsById.put(record.id(), record);
        dirtyListener.run();
        return Optional.of(record);
    }

    public boolean remove(UUID id) {
        boolean removed = standardsById.remove(id) != null;
        if (removed) {
            dirtyListener.run();
        }
        return removed;
    }

    public Optional<SiegeStandardRecord> byId(UUID id) {
        return Optional.ofNullable(standardsById.get(id));
    }

    public Collection<SiegeStandardRecord> all() {
        return List.copyOf(standardsById.values());
    }

    public Collection<SiegeStandardRecord> forWar(UUID warId) {
        if (warId == null) {
            return List.of();
        }
        List<SiegeStandardRecord> matching = new ArrayList<>();
        for (SiegeStandardRecord record : standardsById.values()) {
            if (warId.equals(record.warId())) {
                matching.add(record);
            }
        }
        return matching;
    }

    public boolean isInsideAnyZone(BlockPos point, Collection<UUID> activeWarIds) {
        if (point == null || activeWarIds == null || activeWarIds.isEmpty()) {
            return false;
        }
        for (SiegeStandardRecord record : standardsById.values()) {
            if (activeWarIds.contains(record.warId()) && record.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (SiegeStandardRecord record : standardsById.values()) {
            list.add(record.toTag());
        }
        tag.put("SiegeStandards", list);
        return tag;
    }

    public static SiegeStandardRuntime fromTag(CompoundTag tag) {
        SiegeStandardRuntime runtime = new SiegeStandardRuntime();
        ListTag list = tag.getList("SiegeStandards", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            SiegeStandardRecord record = SiegeStandardRecord.fromTag(list.getCompound(i));
            runtime.standardsById.put(record.id(), record);
        }
        return runtime;
    }
}
