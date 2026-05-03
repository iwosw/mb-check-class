package com.talhanation.bannermod.society;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NpcMemoryRuntime {
    private static final int MAX_MEMORIES_PER_RESIDENT = 12;
    private static final long REFRESH_COOLDOWN_TICKS = 2400L;

    private final Map<UUID, List<NpcMemoryRecord>> memoriesByResident = new LinkedHashMap<>();
    private Runnable dirtyListener = () -> {
    };

    public void setDirtyListener(Runnable dirtyListener) {
        this.dirtyListener = dirtyListener == null ? () -> {
        } : dirtyListener;
    }

    public List<NpcMemoryRecord> memoriesFor(UUID residentUuid, long gameTime) {
        if (residentUuid == null) {
            return List.of();
        }
        pruneExpired(residentUuid, gameTime);
        List<NpcMemoryRecord> stored = this.memoriesByResident.get(residentUuid);
        return stored == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(stored));
    }

    public boolean remember(NpcMemoryRecord candidate, long gameTime) {
        if (candidate == null || candidate.residentUuid() == null) {
            return false;
        }
        UUID residentUuid = candidate.residentUuid();
        List<NpcMemoryRecord> stored = new ArrayList<>(this.memoriesByResident.getOrDefault(residentUuid, List.of()));
        boolean changed = removeExpiredInternal(stored, gameTime);
        int mergeIndex = -1;
        for (int i = 0; i < stored.size(); i++) {
            NpcMemoryRecord existing = stored.get(i);
            if (existing.sameKey(candidate)) {
                mergeIndex = i;
                break;
            }
        }
        if (mergeIndex >= 0) {
            NpcMemoryRecord existing = stored.get(mergeIndex);
            boolean shouldRefresh = existing.isExpired(gameTime)
                    || candidate.intensity() >= existing.intensity() + 5
                    || gameTime - existing.lastUpdatedGameTime() >= REFRESH_COOLDOWN_TICKS;
            if (!shouldRefresh) {
                if (changed) {
                    storeResidentMemories(residentUuid, stored);
                    markDirty();
                }
                return changed;
            }
            NpcMemoryRecord merged = existing.mergeRefresh(candidate, gameTime);
            if (!merged.equals(existing)) {
                stored.set(mergeIndex, merged);
                changed = true;
            }
        } else {
            stored.add(candidate);
            changed = true;
        }
        sortAndTrim(stored);
        storeResidentMemories(residentUuid, stored);
        if (changed) {
            markDirty();
        }
        return changed;
    }

    public void moveResident(UUID fromResidentUuid, UUID toResidentUuid, long gameTime) {
        if (fromResidentUuid == null || toResidentUuid == null || fromResidentUuid.equals(toResidentUuid)) {
            return;
        }
        List<NpcMemoryRecord> existing = this.memoriesByResident.remove(fromResidentUuid);
        if (existing == null || existing.isEmpty()) {
            return;
        }
        List<NpcMemoryRecord> moved = new ArrayList<>();
        for (NpcMemoryRecord record : existing) {
            moved.add(record.moveResident(toResidentUuid, gameTime));
        }
        List<NpcMemoryRecord> merged = new ArrayList<>(this.memoriesByResident.getOrDefault(toResidentUuid, List.of()));
        merged.addAll(moved);
        sortAndTrim(merged);
        storeResidentMemories(toResidentUuid, merged);
        markDirty();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag memories = new ListTag();
        for (List<NpcMemoryRecord> records : this.memoriesByResident.values()) {
            for (NpcMemoryRecord record : records) {
                memories.add(record.toTag());
            }
        }
        tag.put("Memories", memories);
        return tag;
    }

    public static NpcMemoryRuntime fromTag(CompoundTag tag) {
        NpcMemoryRuntime runtime = new NpcMemoryRuntime();
        List<NpcMemoryRecord> records = new ArrayList<>();
        for (Tag entry : tag.getList("Memories", Tag.TAG_COMPOUND)) {
            records.add(NpcMemoryRecord.fromTag((CompoundTag) entry));
        }
        runtime.restoreSnapshot(records);
        return runtime;
    }

    public void restoreSnapshot(@Nullable Collection<NpcMemoryRecord> records) {
        this.memoriesByResident.clear();
        if (records == null) {
            return;
        }
        for (NpcMemoryRecord record : records) {
            if (record == null || record.residentUuid() == null) {
                continue;
            }
            this.memoriesByResident.computeIfAbsent(record.residentUuid(), ignored -> new ArrayList<>()).add(record);
        }
        for (List<NpcMemoryRecord> recordsByResident : this.memoriesByResident.values()) {
            sortAndTrim(recordsByResident);
        }
    }

    public void reset() {
        if (this.memoriesByResident.isEmpty()) {
            return;
        }
        this.memoriesByResident.clear();
        markDirty();
    }

    private void pruneExpired(UUID residentUuid, long gameTime) {
        List<NpcMemoryRecord> stored = this.memoriesByResident.get(residentUuid);
        if (stored == null) {
            return;
        }
        if (removeExpiredInternal(stored, gameTime)) {
            storeResidentMemories(residentUuid, stored);
            markDirty();
        }
    }

    private boolean removeExpiredInternal(List<NpcMemoryRecord> records, long gameTime) {
        return records.removeIf(record -> record == null || record.isExpired(gameTime));
    }

    private void storeResidentMemories(UUID residentUuid, List<NpcMemoryRecord> records) {
        if (records.isEmpty()) {
            this.memoriesByResident.remove(residentUuid);
            return;
        }
        this.memoriesByResident.put(residentUuid, records);
    }

    private static void sortAndTrim(List<NpcMemoryRecord> records) {
        records.sort(Comparator
                .comparingLong(NpcMemoryRecord::createdGameTime).reversed()
                .thenComparing(Comparator.comparingInt(NpcMemoryRecord::intensity).reversed()));
        if (records.size() > MAX_MEMORIES_PER_RESIDENT) {
            records.subList(MAX_MEMORIES_PER_RESIDENT, records.size()).clear();
        }
    }

    private void markDirty() {
        this.dirtyListener.run();
    }
}
