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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FactionMembershipService {
    private FactionMembershipService() {
    }

    public static void createTeam(boolean menu, ServerPlayer serverPlayer, @NotNull ServerLevel level, String teamName, String displayName, String playerName, ItemStack banner, net.minecraft.ChatFormatting color, byte colorByte) {
        MinecraftServer server = level.getServer();
        PlayerTeam team = server.getScoreboard().getPlayerTeam(teamName);
        int cost = com.talhanation.bannermod.config.RecruitsServerConfig.FactionCreationCost.get();
        if (banner == null) banner = Items.BROWN_BANNER.getDefaultInstance();

        if (team != null || FactionEvents.recruitsFactionManager.isNameInUse(teamName)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }
        if (teamName.chars().count() > 32) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.teamname_to_long").withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }
        if (teamName.isBlank()) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noname").withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }
        if (menu && !FactionEconomyService.playerHasEnoughEmeralds(serverPlayer, cost)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noenough_money").withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }
        if (menu && FactionEvents.recruitsFactionManager.isBannerBlank(banner)) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.wrongbanner"));
            return;
        }
        if (menu && FactionEvents.recruitsFactionManager.isBannerInUse(banner.serializeNBT())) {
            serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.banner_exists").withStyle(net.minecraft.ChatFormatting.RED));
            return;
        }

        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam newTeam = scoreboard.addPlayerTeam(teamName);
        newTeam.setDisplayName(Component.literal(displayName));
        newTeam.setColor(color);
        newTeam.setAllowFriendlyFire(com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSetting.get() && com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamFriendlyFireSetting.get());
        newTeam.setSeeFriendlyInvisibles(com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSetting.get() && com.talhanation.bannermod.config.RecruitsServerConfig.GlobalTeamSeeFriendlyInvisibleSetting.get());
        server.getScoreboard().addPlayerToTeam(playerName, newTeam);
        if (menu) FactionEconomyService.doPayment(serverPlayer, cost);
        FactionEvents.recruitsFactionManager.addTeam(teamName, displayName, serverPlayer.getUUID(), serverPlayer.getScoreboardName(), banner.serializeNBT(), colorByte, newTeam.getColor());
        addPlayerToData(level, teamName, 1, playerName);
        RecruitsFaction createdFactionForMember = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (createdFactionForMember != null) createdFactionForMember.addMember(serverPlayer.getUUID(), playerName);
        List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(serverPlayer.getUUID(), level);
        addNPCToData(level, teamName, recruits.size());
        addRecruitToTeam(recruits, newTeam, level);
        BannerModMain.LOGGER.info("The new Team " + teamName + " has been created by " + playerName + ".");
        FactionEvents.recruitsFactionManager.save(server.overworld());
        RecruitsFaction createdFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (createdFaction != null && MinecraftForge.EVENT_BUS.post(new FactionEvent.Created(createdFaction, level, serverPlayer))) {
            removeTeam(level, teamName);
        }
    }

    public static void leaveTeam(boolean command, ServerPlayer player, String teamName, ServerLevel level, boolean fromLeader) {
        MinecraftServer server = level.getServer();
        String playerName = player.getName().getString();
        Team team = player.getTeam();
        if (team == null) {
            PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
            if (playerTeam != null) FactionEvents.recruitsFactionManager.removeTeam(teamName);
            return;
        }
        if (teamName == null) teamName = team.getName();
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        boolean isLeader = recruitsFaction != null ? recruitsFaction.getTeamLeaderUUID().equals(player.getUUID()) : command;
        if (recruitsFaction != null) MinecraftForge.EVENT_BUS.post(new FactionEvent.PlayerLeft(recruitsFaction, level, player, isLeader));
        addNPCToData(level, teamName, -getRecruitsOfPlayer(player.getUUID(), level).size());
        if (playerTeam != null) {
            if (isLeader) {
                removeTeam(level, teamName);
                return;
            }
            ServerPlayer leaderOfTeam = recruitsFaction == null ? null : server.getPlayerList().getPlayerByName(recruitsFaction.getTeamLeaderName());
            if (!fromLeader && leaderOfTeam != null) leaderOfTeam.sendSystemMessage(FactionEvents.PLAYER_LEFT_TEAM_LEADER(playerName));
            server.getScoreboard().removePlayerFromTeam(playerName, playerTeam);
            if (recruitsFaction != null) recruitsFaction.removeMember(playerName);
            addPlayerToData(level, teamName, -1, playerName);
            removeRecruitFromTeam(teamName, player, level);
            return;
        }
        BannerModMain.LOGGER.error("Can not remove " + playerName + " from Team, because " + teamName + " does not exist!");
        serverSideUpdateTeam(level);
        FactionEvents.recruitsFactionManager.save(server.overworld());
    }

    public static void modifyTeam(ServerLevel level, String stringID, RecruitsFaction editedTeam, @Nullable ServerPlayer serverPlayer, int cost) {
        MinecraftServer server = level.getServer();
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(stringID);
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(stringID);
        if (serverPlayer != null) {
            if (cost > 0 && !FactionEconomyService.playerHasEnoughEmeralds(serverPlayer, cost)) {
                serverPlayer.sendSystemMessage(Component.translatable("chat.recruits.team_creation.noenough_money").withStyle(net.minecraft.ChatFormatting.RED));
                return;
            }
            if (cost > 0) FactionEconomyService.doPayment(serverPlayer, cost);
        }
        if (recruitsFaction == null || playerTeam == null) return;
        if (!recruitsFaction.getTeamLeaderUUID().equals(editedTeam.getTeamLeaderUUID())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 10, editedTeam.getTeamLeaderName());
            recruitsFaction.setTeamLeaderID(editedTeam.getTeamLeaderUUID());
            recruitsFaction.setTeamLeaderName(editedTeam.getTeamLeaderName());
        }
        if (!recruitsFaction.getTeamDisplayName().equals(editedTeam.getTeamDisplayName())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 11, editedTeam.getTeamDisplayName());
            recruitsFaction.setTeamDisplayName(editedTeam.getTeamDisplayName());
        }
        if (!recruitsFaction.getBanner().equals(editedTeam.getBanner())) {
            FactionNotifier.notifyFactionMembers(level, recruitsFaction, 12, "");
            recruitsFaction.setBanner(editedTeam.getBanner());
        }
        recruitsFaction.setUnitColor(editedTeam.getUnitColor());
        recruitsFaction.setTeamColor(editedTeam.getTeamColor());
        recruitsFaction.setMaxNPCsPerPlayer(editedTeam.getMaxNPCsPerPlayer());
        playerTeam.setDisplayName(Component.literal(editedTeam.getTeamDisplayName()));
        playerTeam.setColor(net.minecraft.ChatFormatting.getById(editedTeam.getTeamColor()));
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (claim.getOwnerFaction().getStringID().equals(editedTeam.getStringID())) claim.setOwnerFaction(editedTeam);
        }
        ClaimEvents.recruitsClaimManager.broadcastClaimsToAll(level);
        FactionEvents.recruitsFactionManager.save(level);
    }

    public static void removeTeam(ServerLevel level, String teamName) {
        MinecraftServer server = level.getServer();
        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction disbandingFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (disbandingFaction != null) MinecraftForge.EVENT_BUS.post(new FactionEvent.Disbanded(disbandingFaction, level));
        if (playerTeam != null) {
            server.getScoreboard().removePlayerTeam(playerTeam);
            FactionEvents.recruitsFactionManager.removeTeam(teamName);
            for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
                if (claim.getOwnerFaction().getStringID().equals(teamName)) ClaimEvents.recruitsClaimManager.removeClaim(claim);
            }
            ClaimEvents.recruitsClaimManager.broadcastClaimsToAll(level);
            FactionEvents.recruitsFactionManager.removeTeam(teamName);
        }
        FactionEvents.recruitsFactionManager.save(server.overworld());
    }

    public static void addPlayerToTeam(@Nullable ServerPlayer player, ServerLevel level, String teamName, String namePlayerToAdd) {
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
        server.getScoreboard().addPlayerToTeam(namePlayerToAdd, playerTeam);
        playerToAdd.sendSystemMessage(FactionEvents.ADDED_PLAYER(teamName));
        if (player != null) player.sendSystemMessage(FactionEvents.ADDED_PLAYER_LEADER(namePlayerToAdd));
        recruitsFaction.addMember(playerToAdd.getUUID(), namePlayerToAdd);
        addPlayerToData(level, teamName, 1, namePlayerToAdd);
        addNPCToData(level, teamName, getRecruitsOfPlayer(playerToAdd.getUUID(), level).size());
        serverSideUpdateTeam(level);
        FactionNotifier.notifyPlayerJoinedFaction(level, playerToAdd, recruitsFaction);
        FactionNotifier.notifyFactionMembers(level, recruitsFaction, 9, playerToAdd.getName().getString());
        FactionEvents.recruitsFactionManager.save(server.overworld());
    }

    public static boolean isPlayerAlreadyAFactionLeader(ServerPlayer playerToCheck) {
        for (RecruitsFaction recruitsFaction : FactionEvents.recruitsFactionManager.getFactions()) {
            if (recruitsFaction.getTeamLeaderUUID().equals(playerToCheck.getUUID())) return true;
        }
        return false;
    }

    public static void addPlayerToData(ServerLevel level, String teamName, int x, String namePlayerToAdd) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        recruitsFaction.addPlayer(x);
        if (x > 0) recruitsFaction.removeJoinRequest(namePlayerToAdd);
        FactionEvents.recruitsFactionManager.save(level);
    }

    public static void addNPCToData(ServerLevel level, String teamName, int x) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(teamName);
        if (recruitsFaction != null) recruitsFaction.addNPCs(x);
        else BannerModMain.LOGGER.error("Could not modify recruits team: " + teamName + ".Team does not exist.");
        FactionEvents.recruitsFactionManager.broadcastToFactionPlayers(teamName, level);
    }

    public static void serverSideUpdateTeam(ServerLevel level) {
        List<AbstractRecruitEntity> recruitList = new ArrayList<>();
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof AbstractRecruitEntity recruit) recruitList.add(recruit);
        }
        for (AbstractRecruitEntity recruit : recruitList) {
            recruit.needsTeamUpdate = true;
        }
        FactionEvents.recruitsFactionManager.save(level);
    }

    private static List<AbstractRecruitEntity> getRecruitsOfPlayer(UUID playerUuid, ServerLevel level) {
        List<AbstractRecruitEntity> list = new ArrayList<>();
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof AbstractRecruitEntity recruit && recruit.getOwner() != null && recruit.getOwnerUUID().equals(playerUuid)) list.add(recruit);
        }
        return list;
    }

    private static void addRecruitToTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level) {
        for (AbstractRecruitEntity recruit : recruits) addRecruitToTeam(recruit, team, level);
    }

    static void addRecruitToTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level) {
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

    private static void removeRecruitFromTeam(String teamName, ServerPlayer player, ServerLevel level) {
        List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(player.getUUID(), level);
        Team team = level.getScoreboard().getPlayerTeam(teamName);
        if (team != null) removeRecruitFromTeam(recruits, team, level);
    }

    private static void removeRecruitFromTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level) {
        for (AbstractRecruitEntity recruit : recruits) removeRecruitFromTeam(recruit, team, level);
    }

    static void removeRecruitFromTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level) {
        if (recruit == null || team == null) return;
        Team recruitsFaction = recruit.getTeam();
        if (recruitsFaction != null && recruitsFaction.equals(team)) {
            PlayerTeam recruitTeam = level.getScoreboard().getPlayerTeam(team.getName());
            if (recruitTeam != null) level.getScoreboard().removePlayerFromTeam(recruit.getStringUUID(), recruitTeam);
        }
    }
}
