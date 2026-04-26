package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;

final class ClaimProtectionPolicy {

    private final RecruitsClaimManager claimManager;

    ClaimProtectionPolicy(RecruitsClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    boolean shouldDenyBlockBreak(LevelAccessor level, BlockPos pos, Player player) {
        if (ClaimAccessQueries.hasAdminBypass(player)) return false;
        RecruitsClaim claim = ClaimAccessQueries.getClaim(claimManager, level, pos);
        if (claim == null) {
            return RecruitsServerConfig.BlockPlacingBreakingOnlyWhenClaimed.get();
        }
        return !claim.isBlockBreakingAllowed() && !ClaimAccessQueries.isFriendlyToClaim(player, claim);
    }

    boolean shouldDenyBlockPlacement(LevelAccessor level, BlockPos pos, Entity entity) {
        if (ClaimAccessQueries.hasAdminBypass(entity)) return false;
        RecruitsClaim claim = ClaimAccessQueries.getClaim(claimManager, level, pos);
        if (claim == null) {
            return RecruitsServerConfig.BlockPlacingBreakingOnlyWhenClaimed.get();
        }
        return !claim.isBlockPlacementAllowed() && !ClaimAccessQueries.isFriendlyToClaim(entity, claim);
    }

    boolean shouldDenyFluidPlacement(LevelAccessor level, BlockPos targetPos, BlockPos sourcePos) {
        RecruitsClaim targetClaim = ClaimAccessQueries.getClaim(claimManager, level, targetPos);
        if (targetClaim == null) {
            return RecruitsServerConfig.BlockPlacingBreakingOnlyWhenClaimed.get();
        }
        if (targetClaim.isBlockPlacementAllowed()) {
            return false;
        }

        RecruitsClaim sourceClaim = ClaimAccessQueries.getClaim(claimManager, level, sourcePos);
        if (sourceClaim == null) {
            return true;
        }

        return !sourceClaim.getUUID().equals(targetClaim.getUUID());
    }

    boolean shouldDenyBlockInteraction(LevelAccessor level, BlockPos pos, Player player, InteractionHand hand) {
        if (ClaimAccessQueries.hasAdminBypass(player)) return false;
        RecruitsClaim claim = ClaimAccessQueries.getClaim(claimManager, level, pos);
        if (claim == null || ClaimAccessQueries.isFriendlyToClaim(player, claim)) return false;

        if (!claim.isBlockInteractionAllowed()) {
            return true;
        }

        return !claim.isBlockPlacementAllowed() && ClaimInteractionTargetResolver.handTriggersPlacement(player, hand);
    }

    boolean shouldDenyEntityInteraction(Player player, Entity target) {
        if (ClaimAccessQueries.hasAdminBypass(player)) return false;
        RecruitsClaim claim = ClaimAccessQueries.getClaim(claimManager, player.level(), target.blockPosition());
        return claim != null && !claim.isBlockInteractionAllowed() && !ClaimAccessQueries.isFriendlyToClaim(player, claim);
    }

    boolean shouldDenyEntityAttack(Player player, Entity target) {
        if (ClaimAccessQueries.hasAdminBypass(player)) return false;
        RecruitsClaim claim = ClaimAccessQueries.getClaim(claimManager, player.level(), target.blockPosition());
        return claim != null && !ClaimAccessQueries.isFriendlyToClaim(player, claim);
    }
}
