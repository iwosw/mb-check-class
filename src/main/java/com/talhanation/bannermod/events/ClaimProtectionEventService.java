package com.talhanation.bannermod.events;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationReason;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

final class ClaimProtectionEventService {
    void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyBlockBreak(event.getLevel(), event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            return;
        }
        if (event.getLevel() instanceof ServerLevel level) {
            BuildingInvalidationRuntime.enqueueByBlockChange(level, event.getPos(), BuildingInvalidationReason.BLOCK_BROKEN);
        }
    }

    void onBlockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyBlockPlacement(event.getLevel(), event.getPos(), event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (event.getLevel() instanceof ServerLevel level) {
            BuildingInvalidationRuntime.enqueueByBlockChange(level, event.getPos(), BuildingInvalidationReason.BLOCK_PLACED);
        }
    }

    void onFluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent event) {
        LevelAccessor level = event.getLevel();
        if(level.isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyFluidPlacement(level, event.getPos(), event.getLiquidPos())) {
            event.setCanceled(true);
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            BuildingInvalidationRuntime.enqueueByBlockChange(serverLevel, event.getPos(), BuildingInvalidationReason.FLUID_CHANGED);
        }
    }

    void onExplosion(ExplosionEvent.Start event) {
        if(event.getLevel().isClientSide()) return;
        Vec3 vec = event.getExplosion().center();
        BlockPos pos = new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
        ChunkAccess access = ClaimEvents.server.overworld().getChunk(pos);
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(access.getPos());

        Entity entity = event.getExplosion().getDirectSourceEntity();
        if(entity instanceof Player player && player.isCreative() && player.hasPermissions(2)){
            return;
        }

        if(claim != null && RecruitsServerConfig.ExplosionProtectionInClaims.get()){
            event.setCanceled(true);
            return;
        }
        if (event.getLevel() instanceof ServerLevel level) {
            BuildingInvalidationRuntime.enqueueByBlockChange(level, pos, BuildingInvalidationReason.EXPLOSION);
        }
    }

    void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if(event.getLevel().isClientSide()) return;
        Player player = event.getEntity();
        if(claimProtectionPolicy().shouldDenyBlockInteraction(event.getLevel(), event.getPos(), player, event.getHand())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if(event.getLevel().isClientSide()) return;
        BlockPos targetPos = ClaimInteractionTargetResolver.resolveItemInteractionTarget(event.getEntity(), event.getHand());
        if(targetPos == null) return;
        if(claimProtectionPolicy().shouldDenyBlockInteraction(event.getLevel(), targetPos, event.getEntity(), event.getHand())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityInteraction(event.getEntity(), event.getTarget())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityInteraction(event.getEntity(), event.getTarget())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if(player.level().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityAttack(player, event.getTarget())){
            event.setCanceled(true);
        }
    }

    private ClaimProtectionPolicy claimProtectionPolicy() {
        return new ClaimProtectionPolicy(ClaimEvents.recruitsClaimManager);
    }
}
