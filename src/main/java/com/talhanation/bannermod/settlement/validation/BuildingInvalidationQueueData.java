package com.talhanation.bannermod.settlement.validation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuildingInvalidationQueueData extends SavedData {
    private static final String FILE_ID = "bannermodBuildingInvalidationQueue";
    private static final SavedData.Factory<BuildingInvalidationQueueData> FACTORY = new SavedData.Factory<>(BuildingInvalidationQueueData::new, BuildingInvalidationQueueData::load);

    private final Map<UUID, QueueEntry> queueByBuildingId = new LinkedHashMap<>();

    public static BuildingInvalidationQueueData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BuildingInvalidationQueueData load(CompoundTag tag, HolderLookup.Provider registries) {
        BuildingInvalidationQueueData data = new BuildingInvalidationQueueData();
        ListTag queueTag = tag.getList("Queue", Tag.TAG_COMPOUND);
        for (Tag entry : queueTag) {
            if (!(entry instanceof CompoundTag entryTag)) {
                continue;
            }
            QueueEntry queueEntry = QueueEntry.fromTag(entryTag);
            data.queueByBuildingId.put(queueEntry.buildingId(), queueEntry);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag queueTag = new ListTag();
        for (QueueEntry entry : this.queueByBuildingId.values()) {
            queueTag.add(entry.toTag());
        }
        tag.put("Queue", queueTag);
        return tag;
    }

    public void enqueue(UUID buildingId, BuildingInvalidationReason reason, long gameTime) {
        if (buildingId == null) {
            return;
        }
        QueueEntry existing = this.queueByBuildingId.get(buildingId);
        if (existing == null) {
            this.queueByBuildingId.put(buildingId, new QueueEntry(buildingId, reason, gameTime));
            setDirty();
            return;
        }
        BuildingInvalidationReason mergedReason = existing.reason() == BuildingInvalidationReason.UNKNOWN ? reason : existing.reason();
        if (mergedReason != existing.reason()) {
            this.queueByBuildingId.put(buildingId, new QueueEntry(buildingId, mergedReason, existing.enqueuedAtGameTime()));
            setDirty();
        }
    }

    public List<QueueEntry> drainBatch(int maxItems) {
        if (maxItems <= 0 || this.queueByBuildingId.isEmpty()) {
            return List.of();
        }
        int count = 0;
        List<UUID> toRemove = new ArrayList<>(Math.min(maxItems, this.queueByBuildingId.size()));
        List<QueueEntry> batch = new ArrayList<>(Math.min(maxItems, this.queueByBuildingId.size()));
        for (Map.Entry<UUID, QueueEntry> entry : this.queueByBuildingId.entrySet()) {
            if (count >= maxItems) {
                break;
            }
            toRemove.add(entry.getKey());
            batch.add(entry.getValue());
            count++;
        }
        for (UUID id : toRemove) {
            this.queueByBuildingId.remove(id);
        }
        if (!toRemove.isEmpty()) {
            setDirty();
        }
        return List.copyOf(batch);
    }

    public int size() {
        return this.queueByBuildingId.size();
    }

    public record QueueEntry(UUID buildingId, BuildingInvalidationReason reason, long enqueuedAtGameTime) {
        public QueueEntry {
            buildingId = buildingId == null ? UUID.randomUUID() : buildingId;
            reason = reason == null ? BuildingInvalidationReason.UNKNOWN : reason;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("BuildingId", this.buildingId);
            tag.putString("Reason", this.reason.name());
            tag.putLong("EnqueuedAtGameTime", this.enqueuedAtGameTime);
            return tag;
        }

        private static QueueEntry fromTag(CompoundTag tag) {
            UUID buildingId = tag.hasUUID("BuildingId") ? tag.getUUID("BuildingId") : UUID.randomUUID();
            BuildingInvalidationReason reason = parseReason(tag.getString("Reason"));
            long enqueuedAtGameTime = tag.getLong("EnqueuedAtGameTime");
            return new QueueEntry(buildingId, reason, enqueuedAtGameTime);
        }

        private static BuildingInvalidationReason parseReason(String rawReason) {
            try {
                return BuildingInvalidationReason.valueOf(rawReason);
            } catch (IllegalArgumentException ex) {
                return BuildingInvalidationReason.UNKNOWN;
            }
        }
    }
}
