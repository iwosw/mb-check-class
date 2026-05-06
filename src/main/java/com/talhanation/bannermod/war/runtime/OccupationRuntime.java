package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class OccupationRuntime {
    private final Map<UUID, OccupationRecord> recordsById = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> { };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> { } : dirtyListener;
    }

    public Optional<OccupationRecord> place(UUID warId,
                                            UUID occupierEntityId,
                                            UUID occupiedEntityId,
                                            List<ChunkPos> chunks,
                                            long gameTime) {
        if (warId == null || occupierEntityId == null || occupiedEntityId == null
                || occupierEntityId.equals(occupiedEntityId)
                || chunks == null || chunks.isEmpty()) {
            return Optional.empty();
        }
        OccupationRecord record = new OccupationRecord(
                UUID.randomUUID(), warId, occupierEntityId, occupiedEntityId,
                chunks, gameTime);
        recordsById.put(record.id(), record);
        enforceCap();
        dirtyListener.run();
        return Optional.of(record);
    }

    /**
     * Drop occupations whose {@link OccupationRecord#warId()} appears in
     * {@code resolvedWarIds} and whose {@link OccupationRecord#startedAtGameTime()} is older
     * than {@code currentGameTime - retentionTicks}. Returns the number of records removed.
     */
    public int pruneResolved(Collection<UUID> resolvedWarIds, long currentGameTime, long retentionTicks) {
        if (resolvedWarIds == null || resolvedWarIds.isEmpty() || retentionTicks <= 0L) {
            return 0;
        }
        Set<UUID> resolvedSet = Set.copyOf(resolvedWarIds);
        long cutoff = currentGameTime - retentionTicks;
        int removed = 0;
        Iterator<Map.Entry<UUID, OccupationRecord>> iter = recordsById.entrySet().iterator();
        while (iter.hasNext()) {
            OccupationRecord record = iter.next().getValue();
            if (record.warId() != null
                    && resolvedSet.contains(record.warId())
                    && record.startedAtGameTime() < cutoff) {
                iter.remove();
                removed++;
            }
        }
        if (removed > 0) {
            dirtyListener.run();
        }
        return removed;
    }

    private void enforceCap() {
        if (recordsById.size() <= WarRetentionPolicy.MAX_OCCUPATIONS) {
            return;
        }
        Iterator<Map.Entry<UUID, OccupationRecord>> iter = recordsById.entrySet().iterator();
        while (recordsById.size() > WarRetentionPolicy.MAX_OCCUPATIONS && iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }

    public boolean remove(UUID id) {
        boolean removed = recordsById.remove(id) != null;
        if (removed) {
            dirtyListener.run();
        }
        return removed;
    }

    /**
     * Removes every occupation record whose chunk set intersects the supplied claim chunks.
     * Used by the claim-removal fanout so deleted claims do not leave dangling occupation
     * records keyed on chunks the claim manager will never publish again.
     *
     * @return list of removed occupation IDs (empty if none matched)
     */
    public List<UUID> removeForClaim(List<ChunkPos> claimChunks) {
        if (claimChunks == null || claimChunks.isEmpty()) {
            return List.of();
        }
        List<UUID> removed = new ArrayList<>();
        for (Map.Entry<UUID, OccupationRecord> entry : new ArrayList<>(recordsById.entrySet())) {
            OccupationRecord record = entry.getValue();
            for (ChunkPos chunk : claimChunks) {
                if (record.chunks().contains(chunk)) {
                    removed.add(entry.getKey());
                    break;
                }
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

    public Optional<OccupationRecord> updateLastTaxedAt(UUID id, long lastTaxedAtGameTime) {
        OccupationRecord existing = recordsById.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        if (existing.lastTaxedAtGameTime() == lastTaxedAtGameTime) {
            return Optional.of(existing);
        }
        OccupationRecord updated = existing.withLastTaxedAtGameTime(lastTaxedAtGameTime);
        recordsById.put(id, updated);
        dirtyListener.run();
        return Optional.of(updated);
    }

    public Optional<OccupationRecord> byId(UUID id) {
        return Optional.ofNullable(recordsById.get(id));
    }

    public Optional<OccupationRecord> byIdFragment(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return byId(UUID.fromString(token));
        } catch (IllegalArgumentException ignored) {
            String lower = token.toLowerCase(java.util.Locale.ROOT);
            for (OccupationRecord record : recordsById.values()) {
                if (record.id().toString().startsWith(lower)) {
                    return Optional.of(record);
                }
            }
            return Optional.empty();
        }
    }

    public Collection<OccupationRecord> all() {
        return List.copyOf(recordsById.values());
    }

    public Collection<OccupationRecord> forOccupied(UUID occupiedEntityId) {
        if (occupiedEntityId == null) {
            return List.of();
        }
        List<OccupationRecord> matches = new ArrayList<>();
        for (OccupationRecord record : recordsById.values()) {
            if (occupiedEntityId.equals(record.occupiedEntityId())) {
                matches.add(record);
            }
        }
        return matches;
    }

    public Collection<OccupationRecord> forOccupiedClaim(UUID occupiedEntityId, List<ChunkPos> claimChunks) {
        if (occupiedEntityId == null || claimChunks == null || claimChunks.isEmpty()) {
            return List.of();
        }
        List<OccupationRecord> matches = new ArrayList<>();
        for (OccupationRecord record : recordsById.values()) {
            if (!occupiedEntityId.equals(record.occupiedEntityId())) {
                continue;
            }
            for (ChunkPos chunk : claimChunks) {
                if (record.chunks().contains(chunk)) {
                    matches.add(record);
                    break;
                }
            }
        }
        return matches;
    }

    public Collection<OccupationRecord> forOccupiedClaimChunk(UUID occupiedEntityId, ChunkPos claimChunk) {
        if (occupiedEntityId == null || claimChunk == null) {
            return List.of();
        }
        List<OccupationRecord> matches = new ArrayList<>();
        for (OccupationRecord record : recordsById.values()) {
            if (occupiedEntityId.equals(record.occupiedEntityId()) && record.chunks().contains(claimChunk)) {
                matches.add(record);
            }
        }
        return matches;
    }

    public Collection<OccupationRecord> forOccupier(UUID occupierEntityId) {
        if (occupierEntityId == null) {
            return List.of();
        }
        List<OccupationRecord> matches = new ArrayList<>();
        for (OccupationRecord record : recordsById.values()) {
            if (occupierEntityId.equals(record.occupierEntityId())) {
                matches.add(record);
            }
        }
        return matches;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (OccupationRecord record : recordsById.values()) {
            list.add(record.toTag());
        }
        tag.put("Occupations", list);
        return tag;
    }

    public static OccupationRuntime fromTag(CompoundTag tag) {
        OccupationRuntime runtime = new OccupationRuntime();
        ListTag list = tag.getList("Occupations", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            OccupationRecord record = OccupationRecord.fromTag(list.getCompound(i));
            runtime.recordsById.put(record.id(), record);
        }
        runtime.enforceCap();
        return runtime;
    }
}
