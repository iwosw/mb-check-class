package com.talhanation.bannermod.settlement;

import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;

@Deprecated(forRemoval = false)
public final class BannerModSettlementBinding {

    private BannerModSettlementBinding() {
    }

    public enum Status {
        FRIENDLY_CLAIM,
        HOSTILE_CLAIM,
        UNCLAIMED,
        DEGRADED_MISMATCH;

        private static Status fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Status status) {
            return Status.valueOf(status.name());
        }

        private com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Status toShared() {
            return com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Status.valueOf(this.name());
        }
    }

    public record Binding(Status status, @Nullable String settlementFactionId, @Nullable String claimFactionId) {

        public boolean isFriendly() {
            return status == Status.FRIENDLY_CLAIM;
        }

        private static Binding fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Binding binding) {
            return new Binding(Status.fromShared(binding.status()), binding.settlementFactionId(), binding.claimFactionId());
        }

        private com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Binding toShared() {
            return new com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.Binding(this.status.toShared(), this.settlementFactionId, this.claimFactionId);
        }
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claimManager, pos, factionId));
    }

    public static Binding resolveFactionStatus(List<?> claims, BlockPos pos, @Nullable String factionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claims, pos, factionId));
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String factionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claim, chunkPos, factionId));
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String settlementFactionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claimManager, pos, settlementFactionId));
    }

    public static Binding resolveSettlementStatus(List<?> claims, BlockPos pos, @Nullable String settlementFactionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claims, pos, settlementFactionId));
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String settlementFactionId) {
        return Binding.fromShared(com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, settlementFactionId));
    }

    public static boolean allowsWorkAreaPlacement(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId, boolean claimRestricted) {
        return com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.allowsWorkAreaPlacement(claimManager, pos, factionId, claimRestricted);
    }

    public static boolean allowsSettlementOperation(Binding binding) {
        return binding != null && com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding.allowsSettlementOperation(binding.toShared());
    }
}
