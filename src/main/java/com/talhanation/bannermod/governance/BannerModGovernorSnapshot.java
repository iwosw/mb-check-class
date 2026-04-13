package com.talhanation.bannermod.governance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record BannerModGovernorSnapshot(
        UUID claimUuid,
        int anchorChunkX,
        int anchorChunkZ,
        @Nullable String settlementFactionId,
        @Nullable UUID governorRecruitUuid,
        @Nullable UUID governorOwnerUuid,
        long lastHeartbeatTick,
        long lastCollectionTick,
        int citizenCount,
        int taxesDue,
        int taxesCollected,
        List<String> incidentTokens,
        List<String> recommendationTokens
) {
    public BannerModGovernorSnapshot {
        incidentTokens = copyTokens(incidentTokens);
        recommendationTokens = copyTokens(recommendationTokens);
    }

    public ChunkPos anchorChunk() {
        return new ChunkPos(this.anchorChunkX, this.anchorChunkZ);
    }

    public boolean hasGovernor() {
        return this.governorRecruitUuid != null && this.governorOwnerUuid != null;
    }

    public BannerModGovernorSnapshot withGovernor(@Nullable UUID governorRecruitUuid, @Nullable UUID governorOwnerUuid) {
        return new BannerModGovernorSnapshot(
                this.claimUuid,
                this.anchorChunkX,
                this.anchorChunkZ,
                this.settlementFactionId,
                governorRecruitUuid,
                governorOwnerUuid,
                this.lastHeartbeatTick,
                this.lastCollectionTick,
                this.citizenCount,
                this.taxesDue,
                this.taxesCollected,
                this.incidentTokens,
                this.recommendationTokens
        );
    }

    public BannerModGovernorSnapshot withSettlementFactionId(@Nullable String settlementFactionId) {
        return new BannerModGovernorSnapshot(
                this.claimUuid,
                this.anchorChunkX,
                this.anchorChunkZ,
                settlementFactionId,
                this.governorRecruitUuid,
                this.governorOwnerUuid,
                this.lastHeartbeatTick,
                this.lastCollectionTick,
                this.citizenCount,
                this.taxesDue,
                this.taxesCollected,
                this.incidentTokens,
                this.recommendationTokens
        );
    }

    public BannerModGovernorSnapshot withHeartbeatReport(long heartbeatTick, long collectionTick, int citizenCount,
                                                         int taxesDue, int taxesCollected,
                                                         List<String> incidentTokens,
                                                         List<String> recommendationTokens) {
        return new BannerModGovernorSnapshot(
                this.claimUuid,
                this.anchorChunkX,
                this.anchorChunkZ,
                this.settlementFactionId,
                this.governorRecruitUuid,
                this.governorOwnerUuid,
                heartbeatTick,
                collectionTick,
                citizenCount,
                taxesDue,
                taxesCollected,
                incidentTokens,
                recommendationTokens
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ClaimUuid", this.claimUuid);
        tag.putInt("AnchorChunkX", this.anchorChunkX);
        tag.putInt("AnchorChunkZ", this.anchorChunkZ);
        if (this.settlementFactionId != null && !this.settlementFactionId.isBlank()) {
            tag.putString("SettlementFactionId", this.settlementFactionId);
        }
        if (this.governorRecruitUuid != null) {
            tag.putUUID("GovernorRecruitUuid", this.governorRecruitUuid);
        }
        if (this.governorOwnerUuid != null) {
            tag.putUUID("GovernorOwnerUuid", this.governorOwnerUuid);
        }
        tag.putLong("LastHeartbeatTick", this.lastHeartbeatTick);
        tag.putLong("LastCollectionTick", this.lastCollectionTick);
        tag.putInt("CitizenCount", this.citizenCount);
        tag.putInt("TaxesDue", this.taxesDue);
        tag.putInt("TaxesCollected", this.taxesCollected);
        tag.put("IncidentTokens", writeTokens(this.incidentTokens));
        tag.put("RecommendationTokens", writeTokens(this.recommendationTokens));
        return tag;
    }

    public static BannerModGovernorSnapshot create(UUID claimUuid, ChunkPos anchorChunk, @Nullable String settlementFactionId) {
        return new BannerModGovernorSnapshot(
                claimUuid,
                anchorChunk.x,
                anchorChunk.z,
                settlementFactionId,
                null,
                null,
                0L,
                0L,
                0,
                0,
                0,
                List.of(),
                List.of()
        );
    }

    public static BannerModGovernorSnapshot fromTag(CompoundTag tag) {
        UUID claimUuid = tag.getUUID("ClaimUuid");
        String settlementFactionId = tag.contains("SettlementFactionId", Tag.TAG_STRING) ? tag.getString("SettlementFactionId") : null;
        UUID governorRecruitUuid = tag.hasUUID("GovernorRecruitUuid") ? tag.getUUID("GovernorRecruitUuid") : null;
        UUID governorOwnerUuid = tag.hasUUID("GovernorOwnerUuid") ? tag.getUUID("GovernorOwnerUuid") : null;
        return new BannerModGovernorSnapshot(
                claimUuid,
                tag.getInt("AnchorChunkX"),
                tag.getInt("AnchorChunkZ"),
                settlementFactionId,
                governorRecruitUuid,
                governorOwnerUuid,
                tag.getLong("LastHeartbeatTick"),
                tag.getLong("LastCollectionTick"),
                tag.getInt("CitizenCount"),
                tag.getInt("TaxesDue"),
                tag.getInt("TaxesCollected"),
                readTokens(tag.getList("IncidentTokens", Tag.TAG_STRING)),
                readTokens(tag.getList("RecommendationTokens", Tag.TAG_STRING))
        );
    }

    private static ListTag writeTokens(List<String> tokens) {
        ListTag list = new ListTag();
        for (String token : copyTokens(tokens)) {
            list.add(StringTag.valueOf(token));
        }
        return list;
    }

    private static List<String> readTokens(ListTag list) {
        List<String> tokens = new ArrayList<>();
        for (Tag tag : list) {
            tokens.add(tag.getAsString());
        }
        return Collections.unmodifiableList(tokens);
    }

    private static List<String> copyTokens(List<String> tokens) {
        List<String> copied = new ArrayList<>();
        if (tokens != null) {
            for (String token : tokens) {
                if (token != null && !token.isBlank()) {
                    copied.add(token);
                }
            }
        }
        return Collections.unmodifiableList(copied);
    }
}
