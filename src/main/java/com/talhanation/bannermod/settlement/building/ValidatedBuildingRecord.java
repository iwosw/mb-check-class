package com.talhanation.bannermod.settlement.building;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ValidatedBuildingRecord(
        UUID buildingId,
        UUID settlementId,
        BuildingType type,
        ResourceKey<Level> dimension,
        BlockPos anchorPos,
        List<ZoneSelection> zones,
        AABB bounds,
        BuildingValidationState state,
        int capacity,
        int qualityScore,
        List<UUID> assignedCitizenIds,
        long validatedAtGameTime,
        long lastCheckedGameTime,
        long invalidSinceGameTime
) {
    public ValidatedBuildingRecord {
        buildingId = buildingId == null ? UUID.randomUUID() : buildingId;
        settlementId = settlementId == null ? new UUID(0L, 0L) : settlementId;
        type = type == null ? BuildingType.HOUSE : type;
        dimension = dimension == null ? Level.OVERWORLD : dimension;
        anchorPos = anchorPos == null ? BlockPos.ZERO : anchorPos;
        zones = List.copyOf(zones == null ? List.of() : zones);
        bounds = bounds == null ? new AABB(anchorPos) : bounds;
        state = state == null ? BuildingValidationState.VALID : state;
        capacity = Math.max(0, capacity);
        qualityScore = Math.max(0, qualityScore);
        assignedCitizenIds = List.copyOf(assignedCitizenIds == null ? List.of() : assignedCitizenIds);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("BuildingId", this.buildingId);
        tag.putUUID("SettlementId", this.settlementId);
        tag.putString("Type", this.type.name());
        tag.putString("Dimension", this.dimension.location().toString());
        tag.putLong("AnchorPos", this.anchorPos.asLong());

        ListTag zonesTag = new ListTag();
        for (ZoneSelection zone : this.zones) {
            zonesTag.add(zone.toTag());
        }
        tag.put("Zones", zonesTag);

        CompoundTag boundsTag = new CompoundTag();
        boundsTag.putDouble("MinX", this.bounds.minX);
        boundsTag.putDouble("MinY", this.bounds.minY);
        boundsTag.putDouble("MinZ", this.bounds.minZ);
        boundsTag.putDouble("MaxX", this.bounds.maxX);
        boundsTag.putDouble("MaxY", this.bounds.maxY);
        boundsTag.putDouble("MaxZ", this.bounds.maxZ);
        tag.put("Bounds", boundsTag);

        tag.putString("State", this.state.name());
        tag.putInt("Capacity", this.capacity);
        tag.putInt("QualityScore", this.qualityScore);

        ListTag assignedTag = new ListTag();
        for (UUID citizenId : this.assignedCitizenIds) {
            CompoundTag assignedEntry = new CompoundTag();
            assignedEntry.putUUID("CitizenId", citizenId);
            assignedTag.add(assignedEntry);
        }
        tag.put("AssignedCitizenIds", assignedTag);

        tag.putLong("ValidatedAtGameTime", this.validatedAtGameTime);
        tag.putLong("LastCheckedGameTime", this.lastCheckedGameTime);
        tag.putLong("InvalidSinceGameTime", this.invalidSinceGameTime);
        return tag;
    }

    public static ValidatedBuildingRecord fromTag(CompoundTag tag) {
        UUID buildingId = tag.hasUUID("BuildingId") ? tag.getUUID("BuildingId") : UUID.randomUUID();
        UUID settlementId = tag.hasUUID("SettlementId") ? tag.getUUID("SettlementId") : new UUID(0L, 0L);
        BuildingType type = parseBuildingType(tag.getString("Type"));
        ResourceKey<Level> dimension = parseDimension(tag.getString("Dimension"));
        BlockPos anchorPos = tag.contains("AnchorPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("AnchorPos")) : BlockPos.ZERO;
        List<ZoneSelection> zones = readZones(tag.getList("Zones", Tag.TAG_COMPOUND));
        AABB bounds = readBounds(tag.getCompound("Bounds"), anchorPos);
        BuildingValidationState state = parseState(tag.getString("State"));
        int capacity = tag.getInt("Capacity");
        int qualityScore = tag.getInt("QualityScore");
        List<UUID> assignedCitizenIds = readAssignedCitizenIds(tag.getList("AssignedCitizenIds", Tag.TAG_COMPOUND));
        long validatedAtGameTime = tag.getLong("ValidatedAtGameTime");
        long lastCheckedGameTime = tag.getLong("LastCheckedGameTime");
        long invalidSinceGameTime = tag.getLong("InvalidSinceGameTime");
        return new ValidatedBuildingRecord(
                buildingId,
                settlementId,
                type,
                dimension,
                anchorPos,
                zones,
                bounds,
                state,
                capacity,
                qualityScore,
                assignedCitizenIds,
                validatedAtGameTime,
                lastCheckedGameTime,
                invalidSinceGameTime
        );
    }

    private static List<ZoneSelection> readZones(ListTag listTag) {
        List<ZoneSelection> zones = new ArrayList<>();
        for (Tag entry : listTag) {
            if (entry instanceof CompoundTag zoneTag) {
                zones.add(ZoneSelection.fromTag(zoneTag));
            }
        }
        return zones;
    }

    private static AABB readBounds(CompoundTag boundsTag, BlockPos fallbackPos) {
        if (boundsTag.isEmpty()) {
            return new AABB(fallbackPos);
        }
        return new AABB(
                boundsTag.getDouble("MinX"),
                boundsTag.getDouble("MinY"),
                boundsTag.getDouble("MinZ"),
                boundsTag.getDouble("MaxX"),
                boundsTag.getDouble("MaxY"),
                boundsTag.getDouble("MaxZ")
        );
    }

    private static List<UUID> readAssignedCitizenIds(ListTag listTag) {
        List<UUID> ids = new ArrayList<>();
        for (Tag entry : listTag) {
            if (!(entry instanceof CompoundTag assigned)) {
                continue;
            }
            if (assigned.hasUUID("CitizenId")) {
                ids.add(assigned.getUUID("CitizenId"));
            }
        }
        return ids;
    }

    private static ResourceKey<Level> parseDimension(String rawDimension) {
        if (rawDimension == null || rawDimension.isBlank()) {
            return Level.OVERWORLD;
        }
        return ResourceLocation.tryParse(rawDimension) == null
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(rawDimension));
    }

    private static BuildingType parseBuildingType(String rawType) {
        try {
            return BuildingType.valueOf(rawType);
        } catch (IllegalArgumentException ex) {
            return BuildingType.HOUSE;
        }
    }

    private static BuildingValidationState parseState(String rawState) {
        try {
            return BuildingValidationState.valueOf(rawState);
        } catch (IllegalArgumentException ex) {
            return BuildingValidationState.INVALID;
        }
    }
}
