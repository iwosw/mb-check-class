package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.events.FactionEvent;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class FactionTeamTransactionService {
    private FactionTeamTransactionService() {
    }

    static void createTeam(boolean menu, ServerPlayer serverPlayer, @NotNull ServerLevel level, String teamName, String displayName, String playerName, ItemStack banner, net.minecraft.ChatFormatting color, byte colorByte) {
        MinecraftServer server = level.getServer();
        int cost = com.talhanation.bannermod.config.RecruitsServerConfig.FactionCreationCost.get();
        ItemStack teamBanner = banner == null ? Items.BROWN_BANNER.getDefaultInstance() : banner;

        if (!validateCreateTeam(menu, serverPlayer, server, teamName, teamBanner, cost)) {
            return;
        }

        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam newTeam = scoreboard.addPlayerTeam(teamName);
        configureNewTeam(newTeam, displayName, color);
        scoreboard.addPlayerToTeam(playerName, newTeam);
        if (menu) {
            FactionEconomyService.doPayment(serverPlayer, cost);
        }

        initializeCreatedFaction(level, teamName, displayName, playerName, teamBanner, colorByte, newTeam, serverPlayer);

        BannerModMain.LOGGER.info("The new Team " + teamName + " has been created by " + playerName + ".");
        FactionRuntimeSyncService.saveOverworld(server);

        RecruitsFaction createdFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (createdFaction != null && MinecraftForge.EVENT_BUS.post(new FactionEvent.Created(createdFaction, level, serverPlayer))) {
            removeTeam(level, teamName);
        }
    }

    static void leaveTeam(boolean command, ServerPlayer player, String teamName, ServerLevel level, boolean fromLeader) {
        MinecraftServer server = level.getServer();
        String playerName = player.getName().getString();
        Team team = player.getTeam();
        if (team == null) {
            removeTeamDataWhenScoreboardTeamMissing(server, teamName);
            return;
        }

        String resolvedTeamName = teamName == null ? team.getName() : teamName;
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(resolvedTeamName);
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(resolvedTeamName);
        boolean isLeader = recruitsFaction != null ? recruitsFaction.getTeamLeaderUUID().equals(player.getUUID()) : command;

        postPlayerLeftEvent(recruitsFaction, level, player, isLeader);
        removePlayerRecruitCount(level, resolvedTeamName, player);
        if (playerTeam != null) {
            if (handleLeaderLeave(level, resolvedTeamName, isLeader)) {
                return;
            }

            completeMemberLeave(level, server, player, playerName, resolvedTeamName, fromLeader, playerTeam, recruitsFaction, resolveTeamLeader(server, recruitsFaction));
            return;
        }

        BannerModMain.LOGGER.error("Can not remove " + playerName + " from Team, because " + resolvedTeamName + " does not exist!");
        FactionRuntimeSyncService.markRecruitsNeedingTeamUpdate(level);
        FactionRuntimeSyncService.saveOverworld(server);
    }

    static void removeTeam(ServerLevel level, String teamName) {
        MinecraftServer server = level.getServer();
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction disbandingFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (disbandingFaction != null) {
            MinecraftForge.EVENT_BUS.post(new FactionEvent.Disbanded(disbandingFaction, level));
        }
        if (playerTeam != null) {
            server.getScoreboard().removePlayerTeam(playerTeam);
            FactionEvents.recruitsFactionManager.removeTeam(teamName);
            removeClaimsForTeam(level, teamName);
            FactionEvents.recruitsFactionManager.removeTeam(teamName);
        }
        FactionRuntimeSyncService.saveOverworld(server);
    }

    private static void configureNewTeam(PlayerTeam newTeam, String displayName, net.minecraft.ChatFormatting color) {
        newTeam.setDisplayName(Component.literal(displayName));
        newTeam.setColor(color);
        newTeam.setAllowFriendlyFire(com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSetting.get() && com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamFriendlyFireSetting.get());
        newTeam.setSeeFriendlyInvisibles(com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSetting.get() && com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSeeFriendlyInvisibleSetting.get());
    }

    private static void removeTeamDataWhenScoreboardTeamMissing(MinecraftServer server, String teamName) {
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        if (playerTeam != null) {
            FactionEvents.recruitsFactionManager.removeTeam(teamName);
        }
    }

    private static void postPlayerLeftEvent(RecruitsFaction recruitsFaction, ServerLevel level, ServerPlayer player, boolean isLeader) {
        if (recruitsFaction != null) {
            MinecraftForge.EVENT_BUS.post(new FactionEvent.PlayerLeft(recruitsFaction, level, player, isLeader));
        }
    }

    private static void removePlayerRecruitCount(ServerLevel level, String teamName, ServerPlayer player) {
        FactionTeamJoinService.addNPCToData(level, teamName, -FactionRecruitTeamService.getRecruitsOfPlayer(player.getUUID(), level).size());
    }

    private static boolean handleLeaderLeave(ServerLevel level, String teamName, boolean isLeader) {
        if (!isLeader) {
            return false;
        }
        removeTeam(level, teamName);
        return true;
    }

    private static ServerPlayer resolveTeamLeader(MinecraftServer server, RecruitsFaction recruitsFaction) {
        return recruitsFaction == null ? null : server.getPlayerList().getPlayerByName(recruitsFaction.getTeamLeaderName());
    }

    private static void initializeCreatedFaction(ServerLevel level, String teamName, String displayName, String playerName, ItemStack teamBanner, byte colorByte, PlayerTeam newTeam, ServerPlayer serverPlayer) {
        FactionEvents.recruitsFactionManager.addTeam(teamName, displayName, serverPlayer.getUUID(), serverPlayer.getScoreboardName(), teamBanner.serializeNBT(), colorByte, newTeam.getColor());
        FactionTeamJoinService.addPlayerToData(level, teamName, 1, playerName);

        RecruitsFaction createdFactionForMember = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (createdFactionForMember != null) {
            createdFactionForMember.addMember(serverPlayer.getUUID(), playerName);
        }

        List<AbstractRecruitEntity> recruits = FactionRecruitTeamService.getRecruitsOfPlayer(serverPlayer.getUUID(), level);
        FactionTeamJoinService.addNPCToData(level, teamName, recruits.size());
        FactionRecruitTeamService.addRecruitToTeam(recruits, newTeam, level);
    }

    private static void completeMemberLeave(ServerLevel level, MinecraftServer server, ServerPlayer player, String playerName, String teamName, boolean fromLeader, PlayerTeam playerTeam, RecruitsFaction recruitsFaction, ServerPlayer leaderOfTeam) {
        if (!fromLeader && leaderOfTeam != null) {
            leaderOfTeam.sendSystemMessage(FactionEvents.PLAYER_LEFT_TEAM_LEADER(playerName));
        }

        server.getScoreboard().removePlayerFromTeam(playerName, playerTeam);
        if (recruitsFaction != null) {
            recruitsFaction.removeMember(playerName);
        }
        FactionTeamJoinService.addPlayerToData(level, teamName, -1, playerName);
        FactionRecruitTeamService.removeRecruitFromTeam(teamName, player, level);
    }

    private static void removeClaimsForTeam(ServerLevel level, String teamName) {
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim.getOwnerFaction().getStringID().equals(teamName)) {
                ClaimEvents.recruitsClaimManager.removeClaim(claim);
            }
        }
        FactionRuntimeSyncService.broadcastClaims(level);
    }

    private static boolean validateCreateTeam(boolean menu, ServerPlayer serverPlayer, MinecraftServer server, String teamName, ItemStack banner, int cost) {
        PlayerTeam existingTeam = server.getScoreboard().getPlayerTeam(teamName);
        if (existingTeam != null || FactionEvents.recruitsFactionManager.isNameInUse(teamName)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        if (teamName.chars().count() > 32) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.teamname_to_long").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        if (teamName.isBlank()) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noname").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        if (menu && !FactionEconomyService.playerHasEnoughEmeralds(serverPlayer, cost)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noenough_money").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        if (menu && FactionEvents.recruitsFactionManager.isBannerBlank(banner)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.wrongbanner"));
            return false;
        }
        if (menu && FactionEvents.recruitsFactionManager.isBannerInUse(banner.serializeNBT())) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.banner_exists").withStyle(net.minecraft.ChatFormatting.RED));
            return false;
        }
        return true;
    }
}
