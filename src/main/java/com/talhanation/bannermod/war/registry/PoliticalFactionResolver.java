package com.talhanation.bannermod.war.registry;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimSaveData;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsTeamSaveData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PoliticalFactionResolver {
    private PoliticalFactionResolver() {
    }

    public static Optional<RecruitsFaction> factionFor(ServerLevel level, PoliticalEntityRecord record) {
        if (level == null || record == null || record.linkedFactionId().isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(RecruitsTeamSaveData.get(level).getTeams().get(record.linkedFactionId()));
    }

    public static Optional<PoliticalEntityRecord> entityForFaction(PoliticalRegistryRuntime registry,
                                                                   RecruitsFaction faction) {
        if (registry == null || faction == null) {
            return Optional.empty();
        }
        return registry.byLinkedFactionId(faction.getStringID());
    }

    public static Optional<UUID> entityIdForFactionId(PoliticalRegistryRuntime registry, String factionId) {
        if (registry == null || factionId == null || factionId.isBlank()) {
            return Optional.empty();
        }
        return registry.byLinkedFactionId(factionId).map(PoliticalEntityRecord::id);
    }

    public static List<RecruitsClaim> claimsForFaction(ServerLevel level, RecruitsFaction faction) {
        if (level == null || faction == null) {
            return List.of();
        }
        List<RecruitsClaim> matches = new ArrayList<>();
        for (RecruitsClaim claim : RecruitsClaimSaveData.get(level).getAllClaims()) {
            if (claim.getOwnerFaction() != null
                    && faction.getStringID().equals(claim.getOwnerFactionStringID())) {
                matches.add(claim);
            }
        }
        return matches;
    }

    public static List<ChunkPos> allChunksForFaction(ServerLevel level, RecruitsFaction faction) {
        List<ChunkPos> chunks = new ArrayList<>();
        for (RecruitsClaim claim : claimsForFaction(level, faction)) {
            chunks.addAll(claim.getClaimedChunks());
        }
        return chunks;
    }

    public static int transferChunks(ServerLevel level,
                                     RecruitsFaction fromFaction,
                                     RecruitsFaction toFaction,
                                     int maxChunks) {
        if (level == null || fromFaction == null || toFaction == null || maxChunks <= 0) {
            return 0;
        }
        int transferred = 0;
        RecruitsClaimSaveData claimData = RecruitsClaimSaveData.get(level);
        for (RecruitsClaim claim : claimData.getAllClaims()) {
            if (transferred >= maxChunks) {
                break;
            }
            if (claim.getOwnerFaction() != null
                    && fromFaction.getStringID().equals(claim.getOwnerFactionStringID())) {
                claim.setOwnerFaction(toFaction);
                transferred += claim.getClaimedChunks().size();
            }
        }
        if (transferred > 0) {
            claimData.setDirty();
        }
        return transferred;
    }
}
