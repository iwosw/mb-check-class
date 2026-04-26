package com.talhanation.bannermod.settlement.bootstrap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import java.util.UUID;

public record SettlementRecord(
        UUID settlementId,
        UUID ownerPlayerId,
        String factionId,
        UUID claimId,
        ResourceKey<Level> dimension,
        BlockPos authorityPos,
        BlockPos bannerPos,
        UUID fortBuildingId,
        SettlementStatus status,
        long createdGameTime
) {
    public SettlementRecord {
        settlementId = settlementId == null ? UUID.randomUUID() : settlementId;
        ownerPlayerId = ownerPlayerId == null ? new UUID(0L, 0L) : ownerPlayerId;
        factionId = factionId == null ? "" : factionId;
        claimId = claimId == null ? new UUID(0L, 0L) : claimId;
        dimension = dimension == null ? Level.OVERWORLD : dimension;
        authorityPos = authorityPos == null ? BlockPos.ZERO : authorityPos;
        bannerPos = bannerPos == null ? authorityPos : bannerPos;
        fortBuildingId = fortBuildingId == null ? new UUID(0L, 0L) : fortBuildingId;
        status = status == null ? SettlementStatus.ACTIVE : status;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("SettlementId", this.settlementId);
        tag.putUUID("OwnerPlayerId", this.ownerPlayerId);
        tag.putString("FactionId", this.factionId);
        tag.putUUID("ClaimId", this.claimId);
        tag.putString("Dimension", this.dimension.location().toString());
        tag.putLong("AuthorityPos", this.authorityPos.asLong());
        tag.putLong("BannerPos", this.bannerPos.asLong());
        tag.putUUID("FortBuildingId", this.fortBuildingId);
        tag.putString("Status", this.status.name());
        tag.putLong("CreatedGameTime", this.createdGameTime);
        return tag;
    }

    public static SettlementRecord fromTag(CompoundTag tag) {
        UUID settlementId = tag.hasUUID("SettlementId") ? tag.getUUID("SettlementId") : UUID.randomUUID();
        UUID ownerPlayerId = tag.hasUUID("OwnerPlayerId") ? tag.getUUID("OwnerPlayerId") : new UUID(0L, 0L);
        String factionId = tag.getString("FactionId");
        UUID claimId = tag.hasUUID("ClaimId") ? tag.getUUID("ClaimId") : new UUID(0L, 0L);
        ResourceKey<Level> dimension = parseDimension(tag.getString("Dimension"));
        BlockPos authorityPos = tag.contains("AuthorityPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("AuthorityPos")) : BlockPos.ZERO;
        BlockPos bannerPos = tag.contains("BannerPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("BannerPos")) : authorityPos;
        UUID fortBuildingId = tag.hasUUID("FortBuildingId") ? tag.getUUID("FortBuildingId") : new UUID(0L, 0L);
        SettlementStatus status = parseStatus(tag.getString("Status"));
        long createdGameTime = tag.getLong("CreatedGameTime");
        return new SettlementRecord(
                settlementId,
                ownerPlayerId,
                factionId,
                claimId,
                dimension,
                authorityPos,
                bannerPos,
                fortBuildingId,
                status,
                createdGameTime
        );
    }

    private static ResourceKey<Level> parseDimension(String rawDimension) {
        if (rawDimension == null || rawDimension.isBlank()) {
            return Level.OVERWORLD;
        }
        return ResourceLocation.tryParse(rawDimension) == null
                ? Level.OVERWORLD
                : ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(rawDimension));
    }

    private static SettlementStatus parseStatus(String rawStatus) {
        try {
            return SettlementStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException ex) {
            return SettlementStatus.ACTIVE;
        }
    }
}
