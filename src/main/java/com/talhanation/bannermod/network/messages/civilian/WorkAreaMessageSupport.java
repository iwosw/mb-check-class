package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

final class WorkAreaMessageSupport {

    private WorkAreaMessageSupport() {
    }

    @Nullable
    static <T extends AbstractWorkAreaEntity> T resolveAuthorizedWorkArea(ServerPlayer player, UUID uuid, Class<T> areaType) {
        return resolveAuthorizedWorkArea(player, uuid, areaType, area -> WorkAreaAuthoringRules.modifyDecision(true, area.getAuthoringAccess(player)));
    }

    @Nullable
    static <T extends AbstractWorkAreaEntity> T resolveAuthorizedWorkArea(ServerPlayer player, UUID uuid, Class<T> areaType, Function<T, WorkAreaAuthoringRules.Decision> authorizer) {
        Entity entity = player.serverLevel().getEntity(uuid);
        if (!areaType.isInstance(entity)) {
            sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return null;
        }

        T workArea = areaType.cast(entity);
        WorkAreaAuthoringRules.Decision decision = authorizer.apply(workArea);
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            sendDecision(player, decision);
            return null;
        }

        return workArea;
    }

    static void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }
}
