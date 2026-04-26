package com.talhanation.bannermod.army.command;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class CommandHierarchy {
    private CommandHierarchy() {
    }

    public static CommandRole roleFor(ServerPlayer commander, AbstractRecruitEntity recruit) {
        if (commander == null || recruit == null || !recruit.isAlive() || !recruit.isOwned()) {
            return CommandRole.NONE;
        }
        if (Objects.equals(commander.getUUID(), recruit.getOwnerUUID())) {
            return CommandRole.UNIT_COMMANDER;
        }
        if (isNationLeaderFor(commander, recruit)) {
            return CommandRole.NATION_LEADER;
        }
        if (isTownLeaderFor(commander, recruit)) {
            return CommandRole.TOWN_LEADER;
        }
        return CommandRole.NONE;
    }

    public static boolean canCommand(ServerPlayer commander, AbstractRecruitEntity recruit) {
        return roleFor(commander, recruit) != CommandRole.NONE;
    }

    private static boolean isNationLeaderFor(ServerPlayer commander, AbstractRecruitEntity recruit) {
        return false;
    }

    private static boolean isTownLeaderFor(ServerPlayer commander, AbstractRecruitEntity recruit) {
        if (commander.getTeam() == null || recruit.getTeam() == null) {
            return false;
        }
        if (!commander.getTeam().getName().equals(recruit.getTeam().getName())) {
            return false;
        }
        return true;
    }
}
