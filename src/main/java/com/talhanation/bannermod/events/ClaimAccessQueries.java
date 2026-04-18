package com.talhanation.bannermod.events;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

final class ClaimAccessQueries {

    private ClaimAccessQueries() {
    }

    static RecruitsClaim getClaim(RecruitsClaimManager claimManager, LevelAccessor level, BlockPos pos) {
        if (claimManager == null) return null;
        ChunkAccess access = level.getChunk(pos);
        return claimManager.getClaim(access.getPos());
    }

    static boolean hasAdminBypass(Entity entity) {
        return entity instanceof Player player && hasAdminBypass(player);
    }

    static boolean hasAdminBypass(Player player) {
        return player.isCreative() && player.hasPermissions(2);
    }

    static boolean isFriendlyToClaim(Entity entity, RecruitsClaim claim) {
        return entity instanceof LivingEntity livingEntity && isFriendlyToClaim(livingEntity, claim);
    }

    static boolean isFriendlyToClaim(LivingEntity livingEntity, RecruitsClaim claim) {
        return livingEntity.getTeam() != null && livingEntity.getTeam().getName().equals(claim.getOwnerFactionStringID());
    }
}
