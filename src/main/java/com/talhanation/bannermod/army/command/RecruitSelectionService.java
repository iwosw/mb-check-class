package com.talhanation.bannermod.army.command;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Helpers that populate the {@link RecruitSelectionRegistry} from common predicates.
 */
public final class RecruitSelectionService {
    private RecruitSelectionService() {
    }

    /** Select every owned, alive, listening recruit within {@code radius} of the player. */
    public static int selectNearby(ServerPlayer player, double radius) {
        if (player == null) return 0;
        List<AbstractRecruitEntity> nearby = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(radius)
        );
        Set<UUID> selection = new LinkedHashSet<>();
        for (AbstractRecruitEntity recruit : nearby) {
            if (!recruit.isAlive()) continue;
            if (!recruit.isOwned()) continue;
            UUID ownerUuid = recruit.getOwnerUUID();
            if (ownerUuid == null || !ownerUuid.equals(player.getUUID())) continue;
            if (!recruit.getListen()) continue;
            selection.add(recruit.getUUID());
        }
        RecruitSelectionRegistry.instance().set(player.getUUID(), selection);
        return selection.size();
    }

    /** Select every owned recruit belonging to a specific group within {@code radius}. */
    public static int selectGroup(ServerPlayer player, UUID groupUuid, double radius) {
        if (player == null || groupUuid == null) return 0;
        List<AbstractRecruitEntity> nearby = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(radius)
        );
        Set<UUID> selection = new LinkedHashSet<>();
        for (AbstractRecruitEntity recruit : nearby) {
            if (!recruit.isAlive()) continue;
            if (!recruit.isOwned()) continue;
            UUID ownerUuid = recruit.getOwnerUUID();
            if (ownerUuid == null || !ownerUuid.equals(player.getUUID())) continue;
            if (!groupUuid.equals(recruit.getGroup())) continue;
            selection.add(recruit.getUUID());
        }
        RecruitSelectionRegistry.instance().set(player.getUUID(), selection);
        return selection.size();
    }

    /** Replace the selection set with the provided UUIDs (validated against owner+radius). */
    public static int selectExplicit(ServerPlayer player, Set<UUID> candidateUuids, double radius) {
        if (player == null || candidateUuids == null || candidateUuids.isEmpty()) {
            RecruitSelectionRegistry.instance().clear(player == null ? null : player.getUUID());
            return 0;
        }
        List<AbstractRecruitEntity> nearby = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(radius)
        );
        Set<UUID> selection = new LinkedHashSet<>();
        for (AbstractRecruitEntity recruit : nearby) {
            if (!candidateUuids.contains(recruit.getUUID())) continue;
            if (!recruit.isAlive() || !recruit.isOwned()) continue;
            UUID ownerUuid = recruit.getOwnerUUID();
            if (ownerUuid == null || !ownerUuid.equals(player.getUUID())) continue;
            selection.add(recruit.getUUID());
        }
        RecruitSelectionRegistry.instance().set(player.getUUID(), selection);
        return selection.size();
    }
}
