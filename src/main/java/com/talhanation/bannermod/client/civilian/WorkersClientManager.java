package com.talhanation.bannermod.client.civilian;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class WorkersClientManager {

    public static boolean configValueWorkAreaOnlyInFactionClaim;

    public static boolean isInFactionClaim(BlockPos pos){
        if(!configValueWorkAreaOnlyInFactionClaim) return true;
        if (pos == null) return false;
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        RecruitsClaim claim = RecruitsClaimManager.getClaimAt(chunkPos, ClientManager.recruitsClaims);
        if (claim == null) return false;
        return claim.containsChunk(chunkPos) && claim.getOwnerPoliticalEntityId() != null;
    }
}
