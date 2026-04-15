package com.talhanation.bannermod.settlement;

import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @deprecated Use {@link com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding} instead.
 * Forwarder retained for staged migration per Phase 21 D-05 -- legacy shared-package overlap is
 * documented in MERGE_NOTES.md and is intentionally NOT deduplicated during Phase 21.
 *
 * <p>The nested {@link Status} enum and {@link Binding} record are literal mirrors of the canonical
 * types and delegate every resolver through shared -> legacy mapping helpers. Do not add new
 * members here -- add them to the canonical class and extend the mappers below.
 */
@Deprecated
public final class BannerModSettlementBinding {

    private BannerModSettlementBinding() {
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Status}. */
    @Deprecated
    public enum Status {
        FRIENDLY_CLAIM,
        HOSTILE_CLAIM,
        UNCLAIMED,
        DEGRADED_MISMATCH
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Binding}. */
    @Deprecated
    public record Binding(Status status, @Nullable String settlementFactionId, @Nullable String claimFactionId) {

        public boolean isFriendly() {
            return status == Status.FRIENDLY_CLAIM;
        }
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claimManager, pos, factionId));
    }

    public static Binding resolveFactionStatus(List<RecruitsClaim> claims, BlockPos pos, @Nullable String factionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claims, pos, factionId));
    }

    public static Binding resolveFactionStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String factionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveFactionStatus(claim, chunkPos, factionId));
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String settlementFactionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claimManager, pos, settlementFactionId));
    }

    public static Binding resolveSettlementStatus(List<RecruitsClaim> claims, BlockPos pos, @Nullable String settlementFactionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claims, pos, settlementFactionId));
    }

    public static Binding resolveSettlementStatus(@Nullable RecruitsClaim claim, ChunkPos chunkPos, @Nullable String settlementFactionId) {
        return fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, settlementFactionId));
    }

    public static boolean allowsWorkAreaPlacement(@Nullable RecruitsClaimManager claimManager, BlockPos pos, @Nullable String factionId, boolean claimRestricted) {
        return com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.allowsWorkAreaPlacement(claimManager, pos, factionId, claimRestricted);
    }

    public static boolean allowsSettlementOperation(Binding binding) {
        return com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.allowsSettlementOperation(toShared(binding));
    }

    private static com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Status toShared(Status status) {
        if (status == null) {
            return null;
        }
        return com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Status.valueOf(status.name());
    }

    private static Status fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Status status) {
        if (status == null) {
            return null;
        }
        return Status.valueOf(status.name());
    }

    private static com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Binding toShared(Binding binding) {
        if (binding == null) {
            return null;
        }
        return new com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Binding(toShared(binding.status()), binding.settlementFactionId(), binding.claimFactionId());
    }

    private static Binding fromShared(com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding.Binding binding) {
        if (binding == null) {
            return null;
        }
        return new Binding(fromShared(binding.status()), binding.settlementFactionId(), binding.claimFactionId());
    }
}
