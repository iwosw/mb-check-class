package com.talhanation.bannerlord.shared.settlement;

import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;

public final class BannerModSettlementBinding {

    private BannerModSettlementBinding() {
    }

    public enum Status {
        FRIENDLY_CLAIM,
        HOSTILE_CLAIM,
        UNCLAIMED,
        DEGRADED_MISMATCH
    }

    public record Binding(Status status, @Nullable String settlementFactionId, @Nullable String claimFactionId) {

        public boolean isFriendly() {
            return status == Status.FRIENDLY_CLAIM;
        }
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId) {
        if (claimManager == null || pos == null) {
            return new Binding(Status.UNCLAIMED, normalizeFactionId(factionId), null);
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        return resolveFactionStatus(claimManager.getClaim(chunkPos), chunkPos, factionId);
    }

    public static Binding resolveFactionStatus(List<?> claims, BlockPos pos, @Nullable String factionId) {
        if (claims == null || pos == null) {
            return new Binding(Status.UNCLAIMED, normalizeFactionId(factionId), null);
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        Object claim = resolveClaimAt(chunkPos, claims);
        if (claim instanceof RecruitsClaim recruitsClaim) {
            return resolveFactionStatus(recruitsClaim, chunkPos, factionId);
        }
        if (claim instanceof com.talhanation.bannerlord.persistence.military.RecruitsClaim bannerlordClaim) {
            return resolveFactionStatus(bannerlordClaim, chunkPos, factionId);
        }
        return new Binding(Status.UNCLAIMED, normalizeFactionId(factionId), null);
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String factionId) {
        String normalizedFactionId = normalizeFactionId(factionId);
        String claimFactionId = resolveClaimFactionId(claim, chunkPos);
        if (claimFactionId == null) {
            return new Binding(Status.UNCLAIMED, normalizedFactionId, null);
        }
        if (normalizedFactionId != null && normalizedFactionId.equals(claimFactionId)) {
            return new Binding(Status.FRIENDLY_CLAIM, normalizedFactionId, claimFactionId);
        }
        return new Binding(Status.HOSTILE_CLAIM, normalizedFactionId, claimFactionId);
    }

    public static Binding resolveFactionStatus(@Nullable com.talhanation.bannerlord.persistence.military.RecruitsClaim claim, ChunkPos chunkPos, @Nullable String factionId) {
        String normalizedFactionId = normalizeFactionId(factionId);
        String claimFactionId = resolveClaimFactionId(claim, chunkPos);
        if (claimFactionId == null) {
            return new Binding(Status.UNCLAIMED, normalizedFactionId, null);
        }
        if (normalizedFactionId != null && normalizedFactionId.equals(claimFactionId)) {
            return new Binding(Status.FRIENDLY_CLAIM, normalizedFactionId, claimFactionId);
        }
        return new Binding(Status.HOSTILE_CLAIM, normalizedFactionId, claimFactionId);
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String settlementFactionId) {
        if (claimManager == null || pos == null) {
            return new Binding(Status.UNCLAIMED, normalizeFactionId(settlementFactionId), null);
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        return resolveSettlementStatus(claimManager.getClaim(chunkPos), chunkPos, settlementFactionId);
    }

    public static Binding resolveSettlementStatus(List<?> claims, BlockPos pos, @Nullable String settlementFactionId) {
        if (claims == null || pos == null) {
            return new Binding(Status.UNCLAIMED, normalizeFactionId(settlementFactionId), null);
        }
        ChunkPos chunkPos = new ChunkPos(pos);
        Object claim = resolveClaimAt(chunkPos, claims);
        if (claim instanceof RecruitsClaim recruitsClaim) {
            return resolveSettlementStatus(recruitsClaim, chunkPos, settlementFactionId);
        }
        if (claim instanceof com.talhanation.bannerlord.persistence.military.RecruitsClaim bannerlordClaim) {
            return resolveSettlementStatus(bannerlordClaim, chunkPos, settlementFactionId);
        }
        return new Binding(Status.UNCLAIMED, normalizeFactionId(settlementFactionId), null);
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String settlementFactionId) {
        String normalizedSettlementFactionId = normalizeFactionId(settlementFactionId);
        String claimFactionId = resolveClaimFactionId(claim, chunkPos);
        if (claimFactionId == null) {
            return new Binding(Status.UNCLAIMED, normalizedSettlementFactionId, null);
        }
        if (normalizedSettlementFactionId != null && normalizedSettlementFactionId.equals(claimFactionId)) {
            return new Binding(Status.FRIENDLY_CLAIM, normalizedSettlementFactionId, claimFactionId);
        }
        return new Binding(Status.DEGRADED_MISMATCH, normalizedSettlementFactionId, claimFactionId);
    }

    public static Binding resolveSettlementStatus(@Nullable com.talhanation.bannerlord.persistence.military.RecruitsClaim claim, ChunkPos chunkPos, @Nullable String settlementFactionId) {
        String normalizedSettlementFactionId = normalizeFactionId(settlementFactionId);
        String claimFactionId = resolveClaimFactionId(claim, chunkPos);
        if (claimFactionId == null) {
            return new Binding(Status.UNCLAIMED, normalizedSettlementFactionId, null);
        }
        if (normalizedSettlementFactionId != null && normalizedSettlementFactionId.equals(claimFactionId)) {
            return new Binding(Status.FRIENDLY_CLAIM, normalizedSettlementFactionId, claimFactionId);
        }
        return new Binding(Status.DEGRADED_MISMATCH, normalizedSettlementFactionId, claimFactionId);
    }

    public static boolean allowsWorkAreaPlacement(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId, boolean claimRestricted) {
        if (!claimRestricted) {
            return true;
        }
        return resolveFactionStatus(claimManager, pos, factionId).isFriendly();
    }

    public static boolean allowsSettlementOperation(Binding binding) {
        return binding.status() == Status.FRIENDLY_CLAIM;
    }

    @Nullable
    private static String resolveClaimFactionId(@Nullable RecruitsClaim claim, ChunkPos chunkPos) {
        if (claim == null || !claim.containsChunk(chunkPos) || claim.getOwnerFaction() == null) {
            return null;
        }
        return normalizeFactionId(claim.getOwnerFactionStringID());
    }

    @Nullable
    private static String resolveClaimFactionId(@Nullable com.talhanation.bannerlord.persistence.military.RecruitsClaim claim, ChunkPos chunkPos) {
        if (claim == null || !claim.containsChunk(chunkPos) || claim.getOwnerFaction() == null) {
            return null;
        }
        return normalizeFactionId(claim.getOwnerFactionStringID());
    }

    @Nullable
    private static Object resolveClaimAt(ChunkPos chunkPos, List<?> claims) {
        for (Object claim : claims) {
            if (claim instanceof RecruitsClaim recruitsClaim && recruitsClaim.containsChunk(chunkPos)) {
                return recruitsClaim;
            }
            if (claim instanceof com.talhanation.bannerlord.persistence.military.RecruitsClaim bannerlordClaim && bannerlordClaim.containsChunk(chunkPos)) {
                return bannerlordClaim;
            }
        }
        return null;
    }

    @Nullable
    private static String normalizeFactionId(@Nullable String factionId) {
        if (factionId == null) {
            return null;
        }
        String normalizedFactionId = factionId.trim();
        return normalizedFactionId.isEmpty() ? null : normalizedFactionId;
    }
}
