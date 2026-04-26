package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.registry.civilian.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;

final class BuildingRequestSecurity {
    private static final double MAX_REACH = 64.0D;
    private static final double MAX_REACH_SQUARED = MAX_REACH * MAX_REACH;

    private BuildingRequestSecurity() {
    }

    static boolean canUseWandAt(ServerPlayer player, BlockPos... positions) {
        if (player == null || positions == null || !hasWandInHand(player)) {
            return false;
        }
        for (BlockPos pos : positions) {
            if (pos == null || !withinReach(player, pos) || !canBuildAt(player, pos)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasWandInHand(ServerPlayer player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.BUILDING_PLACEMENT_WAND.get())
                || player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.BUILDING_PLACEMENT_WAND.get());
    }

    private static boolean withinReach(ServerPlayer player, BlockPos pos) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= MAX_REACH_SQUARED;
    }

    private static boolean canBuildAt(ServerPlayer player, BlockPos pos) {
        if (player.isCreative() && player.hasPermissions(2)) {
            return true;
        }
        if (ClaimEvents.recruitsClaimManager == null) {
            return true;
        }
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(pos));
        if (claim == null) {
            return true;
        }
        if (claim.getPlayerInfo() != null && player.getUUID().equals(claim.getPlayerInfo().getUUID())) {
            return true;
        }
        if (player.getTeam() != null && claim.getOwnerFaction() != null
                && player.getTeam().getName().equals(claim.getOwnerFactionStringID())) {
            return true;
        }
        return claim.isBlockPlacementAllowed();
    }
}
