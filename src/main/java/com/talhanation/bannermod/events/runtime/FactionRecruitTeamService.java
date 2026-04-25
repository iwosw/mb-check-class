package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.network.messages.military.MessageToClientSetToast;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class FactionRecruitTeamService {
    private FactionRecruitTeamService() {
    }

    public static void removeOfflinePlayerFromTeam(ServerPlayer player, String nameToRemove, ServerLevel level) {
        MinecraftServer server = level.getServer();
        Team team = player.getTeam();
        if (team == null) return;

        String teamName = team.getName();
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (recruitsFaction == null) return;

        boolean isLeader = recruitsFaction.getTeamLeaderUUID().equals(player.getUUID());
        if (!isLeader) return;

        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        if (playerTeam == null || !playerTeam.getPlayers().contains(nameToRemove)) return;

        server.getScoreboard().removePlayerFromTeam(nameToRemove, playerTeam);
        recruitsFaction.removeMember(nameToRemove);
        FactionMembershipService.addPlayerToData(level, teamName, -1, nameToRemove);

        FactionRuntimeSyncService.saveOverworld(server);
        BannerModMain.LOGGER.info("Offline player " + nameToRemove + " removed from team " + teamName + " by " + player.getName().getString());
    }

    public static void tryToRemoveFromTeam(Team team, ServerPlayer serverPlayer, ServerPlayer potentialRemovePlayer, ServerLevel level, String nameToRemove, boolean menu) {
        if (potentialRemovePlayer != null && team != null) {
            boolean isPlayerToRemove = potentialRemovePlayer.getName().getString().equals(nameToRemove);

            if (isPlayerToRemove) {
                FactionMembershipService.leaveTeam(false, potentialRemovePlayer, null, level, true);
                potentialRemovePlayer.sendSystemMessage(FactionEvents.PLAYER_REMOVED);
                if (menu) serverPlayer.sendSystemMessage(FactionEvents.REMOVE_PLAYER_LEADER(potentialRemovePlayer.getDisplayName().getString()));

                List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(serverPlayer.getUUID(), level);
                int recruitCount = recruits.size();

                FactionMembershipService.addNPCToData(level, team.getName(), -recruitCount);
                removeRecruitFromTeam(recruits, team, level);
            }
        }
    }

    public static void assignToTeamMate(ServerPlayer oldOwner, UUID newOwnerUUID, AbstractRecruitEntity recruit) {
        ServerLevel level = (ServerLevel) oldOwner.getCommandSenderWorld();
        Team team = oldOwner.getTeam();

        if (team != null) {
            Collection<String> list = team.getPlayers().stream().toList();
            List<ServerPlayer> playerList = level.players();

            boolean playerNotFound = false;
            ServerPlayer newOwner = playerList.stream().filter(player -> player.getUUID().equals(newOwnerUUID)).findFirst().orElse(null);

            if (newOwner != null) {
                if (list.contains(newOwner.getName().getString())) {
                    if (!RecruitEvents.recruitsPlayerUnitManager.canPlayerRecruit(team.getName(), newOwnerUUID)) {
                        oldOwner.sendSystemMessage(Component.translatable("chat.recruits.team.assignNewOwnerLimitReached"));
                        return;
                    }
                    recruit.disband(oldOwner, true, true);

                    BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> newOwner), new MessageToClientSetToast(0, oldOwner.getName().getString()));

                    recruit.hire(newOwner, null, true);
                } else {
                    playerNotFound = true;
                }
            } else {
                playerNotFound = true;
            }

            if (playerNotFound) oldOwner.sendSystemMessage(Component.translatable("chat.recruits.team.assignNewOwnerNotFound"));
        }
    }

    public static List<AbstractRecruitEntity> getRecruitsOfPlayer(UUID playerUuid, ServerLevel level) {
        List<AbstractRecruitEntity> list = new ArrayList<>();
        List<AbstractRecruitEntity> indexed = RecruitIndex.instance().all(level, false);
        if (indexed != null) {
            for (AbstractRecruitEntity recruit : indexed) {
                if (recruit.getOwner() != null && recruit.getOwnerUUID().equals(playerUuid)) {
                    list.add(recruit);
                }
            }
            return list;
        }

        RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof AbstractRecruitEntity recruit && recruit.getOwner() != null && recruit.getOwnerUUID().equals(playerUuid)) {
                list.add(recruit);
            }
        }
        return list;
    }

    public static void addRecruitToTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level) {
        for (AbstractRecruitEntity recruit : recruits) {
            addRecruitToTeam(recruit, team, level);
        }
    }

    public static void addRecruitToTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level) {
        String teamName = team.getName();
        PlayerTeam playerteam = level.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);

        boolean flag = playerteam != null && level.getScoreboard().addPlayerToTeam(recruit.getStringUUID(), playerteam);
        if (!flag) {
            BannerModMain.LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", teamName);
        } else {
            recruit.setTarget(null);
            if (recruitsFaction != null) recruit.setColor(recruitsFaction.getUnitColor());
        }
    }

    public static void removeRecruitFromTeam(String teamName, ServerPlayer player, ServerLevel level) {
        List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(player.getUUID(), level);
        Team team = level.getScoreboard().getPlayerTeam(teamName);
        if (team != null) {
            removeRecruitFromTeam(recruits, team, level);
        }
    }

    public static void removeRecruitFromTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level) {
        for (AbstractRecruitEntity recruit : recruits) {
            removeRecruitFromTeam(recruit, team, level);
        }
    }

    public static void removeRecruitFromTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level) {
        if (recruit == null || team == null) return;

        Team recruitsFaction = recruit.getTeam();

        if (recruitsFaction != null && recruitsFaction.equals(team)) {
            PlayerTeam recruitTeam = level.getScoreboard().getPlayerTeam(team.getName());
            if (recruitTeam != null) level.getScoreboard().removePlayerFromTeam(recruit.getStringUUID(), recruitTeam);
        }
    }
}
