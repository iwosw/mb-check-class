package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.Iterator;
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

    /**
     * Backward-compatible overload — schedules a revolt with no recorded warId. New callers
     * should prefer {@link #schedule(UUID, UUID, UUID, UUID, long)} so per-war eviction can
     * cap runaway revolt counts under {@link WarRetentionPolicy#MAX_REVOLTS_PER_WAR}.
     */
    public Optional<RevoltRecord> schedule(UUID occupationId,
                                           UUID rebelEntityId,
                                           UUID occupierEntityId,
                                           long scheduledAtGameTime) {
        return schedule(null, occupationId, rebelEntityId, occupierEntityId, scheduledAtGameTime);
    }

    public Optional<RevoltRecord> schedule(UUID warId,
                                           UUID occupationId,
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
        RevoltRecord record = new RevoltRecord(UUID.randomUUID(), warId, occupationId,
                rebelEntityId, occupierEntityId, scheduledAtGameTime, 0L, RevoltState.PENDING);
        recordsById.put(record.id(), record);
        if (warId != null) {
            enforcePerWarCap(warId);
        }
        dirtyListener.run();
        return Optional.of(record);
    }

    /**
     * Drop the oldest revolts for {@code warId} until the count is at or below
     * {@link WarRetentionPolicy#MAX_REVOLTS_PER_WAR}. Iteration order is insertion order
     * thanks to the backing {@link LinkedHashMap}.
     */
    private void enforcePerWarCap(UUID warId) {
        int count = 0;
        for (RevoltRecord record : recordsById.values()) {
            if (warId.equals(record.warId())) {
                count++;
            }
        }
        if (count <= WarRetentionPolicy.MAX_REVOLTS_PER_WAR) {
            return;
        }
        int toDrop = count - WarRetentionPolicy.MAX_REVOLTS_PER_WAR;
        Iterator<Map.Entry<UUID, RevoltRecord>> iter = recordsById.entrySet().iterator();
        while (iter.hasNext() && toDrop > 0) {
            RevoltRecord record = iter.next().getValue();
            if (warId.equals(record.warId())) {
                iter.remove();
                toDrop--;
            }
        }
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
