package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

final class RecruitMessageEntityResolver {
    private RecruitMessageEntityResolver() {
    }

    @Nullable
    static AbstractRecruitEntity resolveRecruitWithinDistance(ServerPlayer player, UUID entityUuid, double maxDistanceSqr) {
        if (player == null || entityUuid == null) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(entityUuid);
        if (!(entity instanceof AbstractRecruitEntity recruit) || !recruit.isAlive()) {
            return null;
        }
        if (recruit.distanceToSqr(player) > maxDistanceSqr) {
            return null;
        }
        return recruit;
    }

    @Nullable
    static AbstractRecruitEntity resolveRecruitInInflatedBox(ServerPlayer player, UUID entityUuid, double inflate) {
        if (player == null || entityUuid == null) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(entityUuid);
        if (!(entity instanceof AbstractRecruitEntity recruit) || !recruit.isAlive()) {
            return null;
        }
        if (!player.getBoundingBox().inflate(inflate).intersects(recruit.getBoundingBox())) {
            return null;
        }
        return recruit;
    }
}
