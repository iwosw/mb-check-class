package com.talhanation.bannermod.army.command;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

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
        if (FactionEvents.recruitsFactionManager == null || commander.getTeam() == null || recruit.getTeam() == null) {
            return false;
        }
        if (!commander.getTeam().getName().equals(recruit.getTeam().getName())) {
            return false;
        }
        RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(commander.getTeam().getName());
        return faction != null && Objects.equals(commander.getUUID(), faction.getTeamLeaderUUID());
    }

    private static boolean isTownLeaderFor(ServerPlayer commander, AbstractRecruitEntity recruit) {
        if (ClaimEvents.recruitsClaimManager == null || commander.getTeam() == null || recruit.getTeam() == null) {
            return false;
        }
        if (!commander.getTeam().getName().equals(recruit.getTeam().getName())) {
            return false;
        }
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(recruit.blockPosition()));
        return claim != null
                && claim.getPlayerInfo() != null
                && Objects.equals(commander.getUUID(), claim.getPlayerInfo().getUUID());
    }
}
