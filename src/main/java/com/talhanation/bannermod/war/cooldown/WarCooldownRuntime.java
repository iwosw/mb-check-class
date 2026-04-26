package com.talhanation.bannermod.war.cooldown;

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

/**
 * Per-level registry of {@link WarCooldownRecord} entries. Records are keyed by their own
 * UUID; secondary lookups iterate the value collection because the registry is small (one
 * cooldown per (entity, kind) maximum).
 */
public class WarCooldownRuntime {

    private final Map<UUID, WarCooldownRecord> recordsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    /**
     * Grant or extend the cooldown for {@code (politicalEntityId, kind)}. If a record
     * already exists for that pair the longer expiry wins; otherwise a fresh record is added.
     * Returns the record that is now active for the pair.
     */
    public WarCooldownRecord grant(UUID politicalEntityId, WarCooldownKind kind, long endsAtGameTime) {
        Optional<WarCooldownRecord> existing = findActiveOrLatest(politicalEntityId, kind);
        if (existing.isPresent()) {
            WarCooldownRecord previous = existing.get();
            if (endsAtGameTime <= previous.endsAtGameTime()) {
                return previous;
            }
            recordsById.remove(previous.id());
        }
        WarCooldownRecord record = new WarCooldownRecord(
                UUID.randomUUID(), politicalEntityId, kind, endsAtGameTime);
        recordsById.put(record.id(), record);
        dirtyListener.run();
        return record;
    }

    /** Returns the absolute end-game-time of the active cooldown, or {@code 0} if none. */
    public long endsAtFor(UUID politicalEntityId, WarCooldownKind kind, long nowGameTime) {
        return findActiveOrLatest(politicalEntityId, kind)
                .filter(record -> record.endsAtGameTime() > nowGameTime)
                .map(WarCooldownRecord::endsAtGameTime)
                .orElse(0L);
    }

    public boolean isActive(UUID politicalEntityId, WarCooldownKind kind, long nowGameTime) {
        return endsAtFor(politicalEntityId, kind, nowGameTime) > nowGameTime;
    }

    private Optional<WarCooldownRecord> findActiveOrLatest(UUID politicalEntityId, WarCooldownKind kind) {
        WarCooldownRecord best = null;
        for (WarCooldownRecord record : recordsById.values()) {
            if (!record.politicalEntityId().equals(politicalEntityId)) continue;
            if (record.kind() != kind) continue;
            if (best == null || record.endsAtGameTime() > best.endsAtGameTime()) {
                best = record;
            }
        }
        return Optional.ofNullable(best);
    }

    public int pruneExpired(long nowGameTime) {
        List<UUID> toRemove = new ArrayList<>();
        for (WarCooldownRecord record : recordsById.values()) {
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

    public Optional<WarCooldownRecord> byId(UUID id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    public Collection<WarCooldownRecord> all() {
        return List.copyOf(recordsById.values());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (WarCooldownRecord record : recordsById.values()) {
            list.add(record.toTag());
        }
        tag.put("WarCooldowns", list);
        return tag;
    }

    public static WarCooldownRuntime fromTag(CompoundTag tag) {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        ListTag list = tag.getList("WarCooldowns", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            WarCooldownRecord record = WarCooldownRecord.fromTag(list.getCompound(i));
            runtime.recordsById.put(record.id(), record);
        }
        return runtime;
    }
}
