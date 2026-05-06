package com.talhanation.bannermod.settlement.building;

import com.talhanation.bannermod.persistence.SavedDataVersioning;
import com.talhanation.bannermod.settlement.validation.BuildingValidationResult;
import com.talhanation.bannermod.settlement.validation.ValidatedBuildingSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ValidatedBuildingRegistryData extends SavedData {
    private static final String FILE_ID = "bannermodValidatedBuildingRegistry";
    private static final SavedData.Factory<ValidatedBuildingRegistryData> FACTORY = new SavedData.Factory<>(ValidatedBuildingRegistryData::new, ValidatedBuildingRegistryData::load);

    private static final int CURRENT_VERSION = 1;
    private final Map<UUID, ValidatedBuildingRecord> records = new LinkedHashMap<>();
    private final Map<UUID, Map<BuildingType, List<UUID>>> bySettlementType = new HashMap<>();
    private final Map<Long, List<UUID>> byChunk = new HashMap<>();

    public static ValidatedBuildingRegistryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static ValidatedBuildingRegistryData load(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.migrate(tag, CURRENT_VERSION, "ValidatedBuildingRegistryData");
        ValidatedBuildingRegistryData data = new ValidatedBuildingRegistryData();
        ListTag recordsTag = tag.getList("Records", Tag.TAG_COMPOUND);
        for (Tag entry : recordsTag) {
            if (!(entry instanceof CompoundTag recordTag)) {
                continue;
            }
            ValidatedBuildingRecord record = ValidatedBuildingRecord.fromTag(recordTag);
            data.records.put(record.buildingId(), record);
        }
        data.rebuildIndexes();
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SavedDataVersioning.putVersion(tag, CURRENT_VERSION);
        ListTag recordsTag = new ListTag();
        for (ValidatedBuildingRecord record : this.records.values()) {
            recordsTag.add(record.toTag());
        }
        tag.put("Records", recordsTag);
        return tag;
    }

    public void registerBuilding(ValidatedBuildingRecord record) {
        if (record == null) {
            return;
        }
        this.records.put(record.buildingId(), record);
        rebuildIndexes();
        setDirty();
    }

    public boolean removeBuilding(UUID buildingId) {
        if (buildingId == null) {
            return false;
        }
        ValidatedBuildingRecord removed = this.records.remove(buildingId);
        if (removed == null) {
            return false;
        }
        rebuildIndexes();
        setDirty();
        return true;
    }

    public boolean updateValidationState(UUID buildingId, BuildingValidationState state, long checkedGameTime) {
        if (buildingId == null || state == null) {
            return false;
        }
        ValidatedBuildingRecord current = this.records.get(buildingId);
        if (current == null) {
            return false;
        }
        long invalidSince = state == BuildingValidationState.VALID ? 0L : invalidSince(current, checkedGameTime);
        ValidatedBuildingRecord updated = new ValidatedBuildingRecord(
                current.buildingId(),
                current.settlementId(),
                current.type(),
                current.dimension(),
                current.anchorPos(),
                current.zones(),
                current.bounds(),
                state,
                current.capacity(),
                current.qualityScore(),
                current.validatedAtGameTime(),
                checkedGameTime,
                invalidSince
        );
        this.records.put(buildingId, updated);
        setDirty();
        return true;
    }

    public boolean applyRevalidationResult(UUID buildingId,
                                           BuildingValidationResult result,
                                           BuildingValidationState state,
                                           long checkedGameTime) {
        if (buildingId == null || state == null || result == null) {
            return false;
        }
        ValidatedBuildingRecord current = this.records.get(buildingId);
        if (current == null) {
            return false;
        }
        long invalidSince = state == BuildingValidationState.VALID ? 0L : invalidSince(current, checkedGameTime);
        long validatedAt = state == BuildingValidationState.VALID ? checkedGameTime : current.validatedAtGameTime();
        ValidatedBuildingSnapshot snapshot = result.snapshot();
        boolean hasSnapshot = snapshot != null;
        ValidatedBuildingRecord updated = new ValidatedBuildingRecord(
                current.buildingId(),
                current.settlementId(),
                current.type(),
                current.dimension(),
                hasSnapshot ? snapshot.anchorPos() : current.anchorPos(),
                hasSnapshot ? snapshot.zones() : current.zones(),
                hasSnapshot ? snapshot.bounds() : current.bounds(),
                state,
                result.capacity(),
                result.qualityScore(),
                validatedAt,
                checkedGameTime,
                invalidSince
        );
        this.records.put(buildingId, updated);
        if (hasSnapshot) {
            rebuildIndexes();
        }
        setDirty();
        return true;
    }

    public ValidatedBuildingRecord getById(UUID buildingId) {
        return buildingId == null ? null : this.records.get(buildingId);
    }

    public List<ValidatedBuildingRecord> getBuildings(UUID settlementId, BuildingType type) {
        if (settlementId == null || type == null) {
            return List.of();
        }
        Map<BuildingType, List<UUID>> byType = this.bySettlementType.get(settlementId);
        if (byType == null) {
            return List.of();
        }
        List<UUID> ids = byType.get(type);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ValidatedBuildingRecord> result = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            ValidatedBuildingRecord record = this.records.get(id);
            if (record != null) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    public List<ValidatedBuildingRecord> getIntersecting(ChunkPos chunkPos) {
        if (chunkPos == null) {
            return List.of();
        }
        List<UUID> ids = this.byChunk.get(chunkPos.toLong());
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ValidatedBuildingRecord> result = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            ValidatedBuildingRecord record = this.records.get(id);
            if (record != null) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    public ValidatedBuildingRecord getByAnchor(BlockPos anchorPos) {
        if (anchorPos == null) {
            return null;
        }
        for (ValidatedBuildingRecord record : this.records.values()) {
            if (record.anchorPos().equals(anchorPos)) {
                return record;
            }
        }
        return null;
    }

    public List<ValidatedBuildingRecord> findIntersecting(AABB bounds) {
        if (bounds == null) {
            return List.of();
        }
        int minChunkX = ((int) Math.floor(bounds.minX)) >> 4;
        int maxChunkX = ((int) Math.floor(bounds.maxX)) >> 4;
        int minChunkZ = ((int) Math.floor(bounds.minZ)) >> 4;
        int maxChunkZ = ((int) Math.floor(bounds.maxZ)) >> 4;
        Set<UUID> candidates = new HashSet<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                List<UUID> ids = this.byChunk.get(ChunkPos.asLong(chunkX, chunkZ));
                if (ids != null) {
                    candidates.addAll(ids);
                }
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<ValidatedBuildingRecord> result = new ArrayList<>();
        for (UUID id : candidates) {
            ValidatedBuildingRecord record = this.records.get(id);
            if (record != null && record.bounds().intersects(bounds)) {
                result.add(record);
            }
        }
        return List.copyOf(result);
    }

    public Collection<ValidatedBuildingRecord> allRecords() {
        return List.copyOf(this.records.values());
    }

    private void rebuildIndexes() {
        this.bySettlementType.clear();
        this.byChunk.clear();

        for (ValidatedBuildingRecord record : this.records.values()) {
            this.bySettlementType
                    .computeIfAbsent(record.settlementId(), ignored -> new EnumMap<>(BuildingType.class))
                    .computeIfAbsent(record.type(), ignored -> new ArrayList<>())
                    .add(record.buildingId());

            int minChunkX = ((int) Math.floor(record.bounds().minX)) >> 4;
            int maxChunkX = ((int) Math.floor(record.bounds().maxX)) >> 4;
            int minChunkZ = ((int) Math.floor(record.bounds().minZ)) >> 4;
            int maxChunkZ = ((int) Math.floor(record.bounds().maxZ)) >> 4;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
                    this.byChunk.computeIfAbsent(chunkKey, ignored -> new ArrayList<>()).add(record.buildingId());
                }
            }
        }
    }

    private static long invalidSince(ValidatedBuildingRecord current, long checkedGameTime) {
        return current.invalidSinceGameTime() > 0L ? current.invalidSinceGameTime() : checkedGameTime;
    }
}
