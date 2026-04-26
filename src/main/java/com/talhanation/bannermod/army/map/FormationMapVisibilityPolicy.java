package com.talhanation.bannermod.army.map;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FormationMapVisibilityPolicy {
    private FormationMapVisibilityPolicy() {
    }

    public static boolean canRevealContact(ServerPlayer viewer, AbstractRecruitEntity recruit, double maxDistanceSqr) {
        if (viewer == null || recruit == null || !recruit.isAlive()) return false;
        if (recruit.distanceToSqr(viewer) > maxDistanceSqr) return false;
        if (recruit.isCrouching()) return false;
        if (!viewer.serverLevel().canSeeSky(recruit.blockPosition().above())) return false;

        Vec3 from = viewer.getEyePosition();
        Vec3 to = recruit.getEyePosition();
        HitResult result = viewer.serverLevel().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                viewer
        ));
        return result.getType() == HitResult.Type.MISS;
    }
}
