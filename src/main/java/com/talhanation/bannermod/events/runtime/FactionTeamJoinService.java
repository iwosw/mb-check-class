package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.events.FactionEvent;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

final class FactionTeamJoinService {
    private FactionTeamJoinService() {
    }

    static void addPlayerToTeam(@Nullable ServerPlayer player, ServerLevel level, String teamName, String namePlayerToAdd) {
        MinecraftServer server = level.getServer();
        ServerPlayer playerToAdd = server.getPlayerList().getPlayerByName(namePlayerToAdd);
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (recruitsFaction == null || playerToAdd == null || !recruitsFaction.canAddPlayer()) return;
        if (isPlayerAlreadyAFactionLeader(playerToAdd)) {
            if (player != null) player.sendSystemMessage(FactionEvents.CAN_NOT_ADD_OTHER_LEADER());
            return;
        }
        if (playerTeam == null) {
            BannerModMain.LOGGER.error("Can not add " + playerToAdd + " to Team, because " + teamName + " does not exist!");
            return;
        }
        RecruitsFaction joiningFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (joiningFaction != null && MinecraftForge.EVENT_BUS.post(new FactionEvent.PlayerJoined(joiningFaction, level, playerToAdd))) return;
        completePlayerJoin(player, level, server, teamName, namePlayerToAdd, playerToAdd, playerTeam, recruitsFaction);
    }

    static boolean isPlayerAlreadyAFactionLeader(ServerPlayer playerToCheck) {
        for (RecruitsFaction recruitsFaction : FactionEvents.recruitsFactionManager.getFactions()) {
            if (recruitsFaction.getTeamLeaderUUID().equals(playerToCheck.getUUID())) return true;
        }
        return false;
    }

    static void addPlayerToData(ServerLevel level, String teamName, int x, String namePlayerToAdd) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        recruitsFaction.addPlayer(x);
        if (x > 0) recruitsFaction.removeJoinRequest(namePlayerToAdd);
        FactionRuntimeSyncService.save(level);
    }

    static void addNPCToData(ServerLevel level, String teamName, int x) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (recruitsFaction != null) recruitsFaction.addNPCs(x);
        else BannerModMain.LOGGER.error("Could not modify recruits team: " + teamName + ".Team does not exist.");
        FactionRuntimeSyncService.broadcastFactionPlayers(level, teamName);
    }

    private static void completePlayerJoin(@Nullable ServerPlayer player, ServerLevel level, MinecraftServer server, String teamName, String namePlayerToAdd, ServerPlayer playerToAdd, PlayerTeam playerTeam, RecruitsFaction recruitsFaction) {
        server.getScoreboard().addPlayerToTeam(namePlayerToAdd, playerTeam);
        playerToAdd.sendSystemMessage(FactionEvents.ADDED_PLAYER(teamName));
        if (player != null) player.sendSystemMessage(FactionEvents.ADDED_PLAYER_LEADER(namePlayerToAdd));
        recruitsFaction.addMember(playerToAdd.getUUID(), namePlayerToAdd);
        addPlayerToData(level, teamName, 1, namePlayerToAdd);
        addNPCToData(level, teamName, FactionRecruitTeamService.getRecruitsOfPlayer(playerToAdd.getUUID(), level).size());
        FactionRuntimeSyncService.markRecruitsNeedingTeamUpdate(level);
        FactionNotifier.notifyPlayerJoinedFaction(level, playerToAdd, recruitsFaction);
        FactionNotifier.notifyFactionMembers(level, recruitsFaction, 9, playerToAdd.getName().getString());
        FactionRuntimeSyncService.saveOverworld(server);
    }
}
