package com.talhanation.bannermod.governance;

import com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.recruits.world.RecruitsClaim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

public class BannerModGovernorService {
    private final BannerModGovernorManager manager;

    public BannerModGovernorService(BannerModGovernorManager manager) {
        this.manager = manager;
    }

    public OperationResult assignGovernor(@Nullable RecruitsClaim claim,
                                          BannerModGovernorAuthority.ActorContext actor,
                                          RecruitGovernorTarget recruit) {
        return assignGovernor(claim, actor, recruit, claim == null ? null : claim.getOwnerFactionStringID(), false);
    }

    public OperationResult assignGovernor(@Nullable RecruitsClaim claim,
                                          BannerModGovernorAuthority.ActorContext actor,
                                          RecruitGovernorTarget recruit,
                                          @Nullable String settlementFactionId,
                                          boolean useSettlementBinding) {
        BannerModGovernorAuthority.Decision authorityDecision = BannerModGovernorAuthority.assignmentDecision(
                actor,
                recruit == null ? null : recruit.ownerUuid(),
                recruit == null ? null : recruit.teamId()
        );
        BannerModSettlementBinding.Binding binding = resolveBinding(claim, settlementFactionId, useSettlementBinding);
        BannerModGovernorRules.Decision governorDecision = BannerModGovernorRules.assignmentDecision(
                binding,
                recruit == null ? null : recruit.recruitUuid(),
                recruit == null ? null : recruit.ownerUuid()
        );

        if (!BannerModGovernorAuthority.isAllowed(authorityDecision) || !BannerModGovernorRules.isAllowed(governorDecision)) {
            return new OperationResult(false, authorityDecision, governorDecision, claim == null ? null : this.manager.getSnapshot(claim.getUUID()));
        }

        BannerModGovernorSnapshot snapshot = getOrCreateSnapshot(claim, settlementFactionId);
        BannerModGovernorSnapshot updated = snapshot
                .withSettlementFactionId(normalizeFactionId(settlementFactionId))
                .withGovernor(recruit.recruitUuid(), recruit.ownerUuid());
        this.manager.putSnapshot(updated);
        return new OperationResult(true, authorityDecision, governorDecision, updated);
    }

    public OperationResult assignGovernor(RecruitsClaim claim,
                                          BannerModGovernorAuthority.ActorContext actor,
                                          AbstractRecruitEntity recruit) {
        return assignGovernor(claim, actor, RecruitGovernorTarget.fromRecruit(recruit));
    }

    public OperationResult assignGovernor(RecruitsClaim claim,
                                          ServerPlayer player,
                                          AbstractRecruitEntity recruit) {
        return assignGovernor(claim, BannerModGovernorAuthority.actor(player), recruit);
    }

    public OperationResult assignGovernor(RecruitsClaim claim,
                                          BannerModGovernorAuthority.ActorContext actor,
                                          com.talhanation.recruits.entities.AbstractRecruitEntity recruit) {
        return assignGovernor(claim, actor, RecruitGovernorTarget.fromRecruit(recruit));
    }

    public OperationResult assignGovernor(RecruitsClaim claim,
                                          ServerPlayer player,
                                          com.talhanation.recruits.entities.AbstractRecruitEntity recruit) {
        return assignGovernor(claim, BannerModGovernorAuthority.actor(player), recruit);
    }

    public OperationResult revokeGovernor(@Nullable RecruitsClaim claim,
                                          BannerModGovernorAuthority.ActorContext actor) {
        BannerModGovernorSnapshot snapshot = claim == null ? null : this.manager.getSnapshot(claim.getUUID());
        BannerModGovernorAuthority.Decision authorityDecision = BannerModGovernorAuthority.revokeDecision(
                actor,
                snapshot == null ? null : snapshot.governorOwnerUuid(),
                snapshot == null ? null : snapshot.settlementFactionId()
        );
        BannerModSettlementBinding.Binding binding = resolveBinding(claim, snapshot == null ? null : snapshot.settlementFactionId(), true);
        BannerModGovernorRules.Decision governorDecision = BannerModGovernorRules.controlDecision(binding, snapshot);

        if (!BannerModGovernorAuthority.isAllowed(authorityDecision) || !BannerModGovernorRules.isAllowed(governorDecision)) {
            return new OperationResult(false, authorityDecision, governorDecision, snapshot);
        }

        BannerModGovernorSnapshot updated = snapshot.withGovernor(null, null);
        this.manager.putSnapshot(updated);
        return new OperationResult(true, authorityDecision, governorDecision, updated);
    }

    public OperationResult revokeGovernor(@Nullable RecruitsClaim claim,
                                          ServerPlayer player) {
        return revokeGovernor(claim, BannerModGovernorAuthority.actor(player));
    }

    public BannerModGovernorSnapshot getOrCreateGovernorSnapshot(RecruitsClaim claim) {
        BannerModGovernorSnapshot snapshot = getOrCreateSnapshot(claim, claim.getOwnerFactionStringID());
        if (!equalsNullable(snapshot.settlementFactionId(), claim.getOwnerFactionStringID())) {
            snapshot = snapshot.withSettlementFactionId(claim.getOwnerFactionStringID());
            this.manager.putSnapshot(snapshot);
        }
        return snapshot;
    }

    public boolean hasGovernor(RecruitsClaim claim) {
        BannerModGovernorSnapshot snapshot = this.manager.getSnapshot(claim.getUUID());
        return snapshot != null && snapshot.hasGovernor();
    }

    @Nullable
    public UUID getGovernorRecruitUuid(RecruitsClaim claim) {
        BannerModGovernorSnapshot snapshot = this.manager.getSnapshot(claim.getUUID());
        return snapshot == null ? null : snapshot.governorRecruitUuid();
    }

    @Nullable
    public BannerModGovernorSnapshot getGovernorSnapshot(UUID claimUuid) {
        return this.manager.getSnapshot(claimUuid);
    }

    private BannerModGovernorSnapshot getOrCreateSnapshot(RecruitsClaim claim, @Nullable String settlementFactionId) {
        ChunkPos anchorChunk = resolveAnchorChunk(claim);
        BannerModGovernorSnapshot fallback = BannerModGovernorSnapshot.create(
                claim.getUUID(),
                anchorChunk,
                normalizeFactionId(settlementFactionId)
        );
        return this.manager.getOrCreateSnapshot(claim.getUUID(), fallback);
    }

    private static BannerModSettlementBinding.Binding resolveBinding(@Nullable RecruitsClaim claim,
                                                                     @Nullable String settlementFactionId,
                                                                     boolean useSettlementBinding) {
        if (claim == null) {
            return BannerModSettlementBinding.resolveSettlementStatus((RecruitsClaim) null, new ChunkPos(0, 0), settlementFactionId);
        }
        ChunkPos anchorChunk = resolveAnchorChunk(claim);
        if (useSettlementBinding) {
            return BannerModSettlementBinding.resolveSettlementStatus(claim, anchorChunk, settlementFactionId);
        }
        return BannerModSettlementBinding.resolveFactionStatus(claim, anchorChunk, settlementFactionId);
    }

    private static ChunkPos resolveAnchorChunk(RecruitsClaim claim) {
        if (claim.getCenter() != null) {
            return claim.getCenter();
        }
        if (!claim.getClaimedChunks().isEmpty()) {
            return claim.getClaimedChunks().get(0);
        }
        return new ChunkPos(0, 0);
    }

    @Nullable
    private static String normalizeFactionId(@Nullable String factionId) {
        if (factionId == null) {
            return null;
        }
        String normalized = factionId.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean equalsNullable(@Nullable Object left, @Nullable Object right) {
        return left == null ? right == null : left.equals(right);
    }

    public record RecruitGovernorTarget(UUID recruitUuid, @Nullable UUID ownerUuid, @Nullable String teamId) {
        public static RecruitGovernorTarget fromRecruit(AbstractRecruitEntity recruit) {
            return new RecruitGovernorTarget(
                    recruit.getUUID(),
                    recruit.getOwnerUUID(),
                    recruit.getTeam() == null ? null : recruit.getTeam().getName()
            );
        }

        public static RecruitGovernorTarget fromRecruit(com.talhanation.recruits.entities.AbstractRecruitEntity recruit) {
            return new RecruitGovernorTarget(
                    recruit.getUUID(),
                    recruit.getOwnerUUID(),
                    recruit.getTeam() == null ? null : recruit.getTeam().getName()
            );
        }
    }

    public record OperationResult(boolean allowed,
                                  BannerModGovernorAuthority.Decision authorityDecision,
                                  BannerModGovernorRules.Decision governorDecision,
                                  @Nullable BannerModGovernorSnapshot snapshot) {
    }
}
