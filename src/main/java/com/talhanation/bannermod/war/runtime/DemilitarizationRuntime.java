package com.talhanation.bannermod.war.runtime;

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

public class DemilitarizationRuntime {
    private final Map<UUID, DemilitarizationRecord> recordsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public Optional<DemilitarizationRecord> impose(UUID politicalEntityId,
                                                   UUID imposedByWarId,
                                                   long endsAtGameTime) {
        if (politicalEntityId == null) {
            return Optional.empty();
        }
        DemilitarizationRecord record = new DemilitarizationRecord(
                UUID.randomUUID(), politicalEntityId, imposedByWarId, endsAtGameTime);
        recordsById.put(record.id(), record);
        dirtyListener.run();
        return Optional.of(record);
    }

    public boolean remove(UUID id) {
        boolean removed = recordsById.remove(id) != null;
        if (removed) {
            dirtyListener.run();
        }
        return removed;
    }

    public Optional<DemilitarizationRecord> byId(UUID id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    public Collection<DemilitarizationRecord> all() {
        return List.copyOf(recordsById.values());
    }

    public boolean isDemilitarized(UUID politicalEntityId, long nowGameTime) {
        if (politicalEntityId == null) {
            return false;
        }
        for (DemilitarizationRecord record : recordsById.values()) {
            if (politicalEntityId.equals(record.politicalEntityId())
                    && record.endsAtGameTime() > nowGameTime) {
                return true;
            }
        }
        return false;
    }

    public int pruneExpired(long nowGameTime) {
        List<UUID> toRemove = new ArrayList<>();
        for (DemilitarizationRecord record : recordsById.values()) {
            if (record.endsAtGameTime() <= nowGameTime) {
                toRemove.add(record.id());
            }
        }
        for (UUID id : toRemove) {
            recordsById.remove(id);
        }
        if (!toRemove.isEmpty()) {
            dirtyListener.run();
        }
        return toRemove.size();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (DemilitarizationRecord record : recordsById.values()) {
            list.add(record.toTag());
        }
        tag.put("Demilitarizations", list);
        return tag;
    }

    public static DemilitarizationRuntime fromTag(CompoundTag tag) {
        DemilitarizationRuntime runtime = new DemilitarizationRuntime();
        ListTag list = tag.getList("Demilitarizations", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            DemilitarizationRecord record = DemilitarizationRecord.fromTag(list.getCompound(i));
            runtime.recordsById.put(record.id(), record);
        }
        return runtime;
    }
}
