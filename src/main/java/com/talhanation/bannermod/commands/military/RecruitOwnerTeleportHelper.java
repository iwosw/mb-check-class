package com.talhanation.bannermod.commands.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

final class RecruitOwnerTeleportHelper {
    private RecruitOwnerTeleportHelper() {
    }

    static int teleportToOwners(ServerLevel level, Collection<String> ownerNames) {
        Map<UUID, ServerPlayer> playersById = level.getPlayers(player -> ownerNames.contains(player.getScoreboardName())).stream()
                .collect(Collectors.toMap(ServerPlayer::getUUID, Function.identity()));

        for (AbstractRecruitEntity recruit : getOwnedRecruits(level)) {
            ServerPlayer player = playersById.get(recruit.getOwnerUUID());
            if (player != null) {
                recruit.teleportTo(player.getX(), player.getY(), player.getZ());
            }
        }

        return 1;
    }

    private static List<AbstractRecruitEntity> getOwnedRecruits(ServerLevel level) {
        List<AbstractRecruitEntity> indexed = RecruitIndex.instance().all(level, false);
        if (indexed != null) {
            return indexed.stream()
                    .filter(AbstractRecruitEntity::isOwned)
                    .toList();
        }
        RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
        List<Entity> allEntities = new ArrayList<>();
        level.getEntities().getAll().iterator().forEachRemaining(allEntities::add);
        return allEntities.stream()
                .filter(entity -> entity instanceof AbstractRecruitEntity recruit && recruit.isOwned())
                .map(entity -> (AbstractRecruitEntity) entity)
                .toList();
    }
}
