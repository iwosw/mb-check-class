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

public class RevoltRuntime {
    private final Map<UUID, RevoltRecord> recordsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public Optional<RevoltRecord> schedule(UUID occupationId,
                                           UUID rebelEntityId,
                                           UUID occupierEntityId,
                                           long scheduledAtGameTime) {
        if (occupationId == null || rebelEntityId == null || occupierEntityId == null
                || rebelEntityId.equals(occupierEntityId)) {
            return Optional.empty();
        }
        for (RevoltRecord record : recordsById.values()) {
            if (occupationId.equals(record.occupationId()) && record.state() == RevoltState.PENDING) {
                return Optional.empty();
            }
        }
        RevoltRecord record = new RevoltRecord(UUID.randomUUID(), occupationId,
                rebelEntityId, occupierEntityId, scheduledAtGameTime, 0L, RevoltState.PENDING);
        recordsById.put(record.id(), record);
        dirtyListener.run();
        return Optional.of(record);
    }

    public boolean resolve(UUID revoltId, RevoltState newState, long resolvedAtGameTime) {
        RevoltRecord record = recordsById.get(revoltId);
        if (record == null || record.state() != RevoltState.PENDING || newState == RevoltState.PENDING) {
            return false;
        }
        recordsById.put(revoltId, record.withState(newState, resolvedAtGameTime));
        dirtyListener.run();
        return true;
    }

    public Optional<RevoltRecord> byId(UUID id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    public Optional<RevoltRecord> byIdFragment(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return byId(UUID.fromString(token));
        } catch (IllegalArgumentException ignored) {
            String lower = token.toLowerCase(java.util.Locale.ROOT);
            for (RevoltRecord record : recordsById.values()) {
                if (record.id().toString().startsWith(lower)) {
                    return Optional.of(record);
                }
            }
            return Optional.empty();
        }
    }

    public Collection<RevoltRecord> all() {
        return List.copyOf(recordsById.values());
    }

    /**
     * Removes every revolt record whose {@code occupationId} appears in the supplied set.
     * Used by the claim-removal fanout: when occupations get cleared because their claim
     * was deleted, any revolts they spawned must follow.
     *
     * @return list of removed revolt IDs
     */
    public List<UUID> removeForOccupations(Collection<UUID> occupationIds) {
        if (occupationIds == null || occupationIds.isEmpty()) {
            return List.of();
        }
        List<UUID> removed = new ArrayList<>();
        for (Map.Entry<UUID, RevoltRecord> entry : new ArrayList<>(recordsById.entrySet())) {
            if (occupationIds.contains(entry.getValue().occupationId())) {
                removed.add(entry.getKey());
            }
        }
        if (removed.isEmpty()) {
            return List.of();
        }
        for (UUID id : removed) {
            recordsById.remove(id);
        }
        dirtyListener.run();
        return removed;
    }

    public boolean hasPendingRevoltFor(UUID occupationId) {
        if (occupationId == null) {
            return false;
        }
        for (RevoltRecord record : recordsById.values()) {
            if (occupationId.equals(record.occupationId()) && record.state() == RevoltState.PENDING) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (RevoltRecord record : recordsById.values()) {
            list.add(record.toTag());
        }
        tag.put("Revolts", list);
        return tag;
    }

    public static RevoltRuntime fromTag(CompoundTag tag) {
        RevoltRuntime runtime = new RevoltRuntime();
        ListTag list = tag.getList("Revolts", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            RevoltRecord record = RevoltRecord.fromTag(list.getCompound(i));
            runtime.recordsById.put(record.id(), record);
        }
        return runtime;
    }
}
