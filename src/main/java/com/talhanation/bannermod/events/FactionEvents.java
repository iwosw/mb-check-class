package com.talhanation.bannermod.events;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.runtime.FactionEconomyService;
import com.talhanation.bannermod.events.runtime.FactionLifecycleService;
import com.talhanation.bannermod.events.runtime.FactionMenuService;
import com.talhanation.bannermod.events.runtime.FactionMembershipService;
import com.talhanation.bannermod.events.runtime.FactionNotifier;
import com.talhanation.bannermod.events.runtime.FactionRecruitTeamService;
import com.talhanation.bannermod.inventory.military.*;
import com.talhanation.bannermod.network.messages.military.*;
import com.talhanation.bannermod.persistence.military.*;
import com.talhanation.bannermod.events.FactionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FactionEvents {

    public MinecraftServer server;
    public static RecruitsFactionManager recruitsFactionManager;
    public static RecruitsDiplomacyManager recruitsDiplomacyManager;
    public static RecruitsTreatyManager recruitsTreatyManager;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.server = event.getServer();
        FactionLifecycleService.onServerStarting(this.server);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        FactionLifecycleService.saveAll(this.server);
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event){
        FactionLifecycleService.saveAll(this.server);
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof Player player){
            FactionLifecycleService.onPlayerJoin(this.server.overworld(), player, this.server);
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(EntityLeaveLevelEvent event){
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof Player){
            FactionLifecycleService.onPlayerLeave(this.server);
        }
    }


    public static void createTeam(boolean menu, ServerPlayer serverPlayer, @NotNull ServerLevel level, String teamName, String displayName, String playerName, ItemStack banner, ChatFormatting color, byte colorByte) {
        FactionMembershipService.createTeam(menu, serverPlayer, level, teamName, displayName, playerName, banner, color, colorByte);
    }

    public static void leaveTeam(boolean command, ServerPlayer player, String teamName, ServerLevel level, boolean fromLeader) {
        FactionMembershipService.leaveTeam(command, player, teamName, level, fromLeader);
    }

    public static void modifyTeam(ServerLevel level, String stringID, RecruitsFaction editedTeam, @Nullable ServerPlayer serverPlayer, int cost) {
        FactionMembershipService.modifyTeam(level, stringID, editedTeam, serverPlayer, cost);
    }

    public static void notifyFactionMembers(ServerLevel level, RecruitsFaction recruitsFaction, int id, String notification){
        FactionNotifier.notifyFactionMembers(level, recruitsFaction, id, notification);
    }

    public static void notifyPlayer(ServerLevel level, RecruitsPlayerInfo playerInfo, int id, String notification){
        FactionNotifier.notifyPlayer(level, playerInfo, id, notification);
    }

    public static void removeTeam(ServerLevel level, String teamName){
        FactionMembershipService.removeTeam(level, teamName);
    }

    private static void removeRecruitsFactionData(String teamName) {
        recruitsFactionManager.removeTeam(teamName);
    }

    public static void addPlayerToTeam(@Nullable ServerPlayer player, ServerLevel level, String teamName, String namePlayerToAdd) {
        FactionMembershipService.addPlayerToTeam(player, level, teamName, namePlayerToAdd);
    }

    public static boolean isPlayerAlreadyAFactionLeader(ServerPlayer playerToCheck){
        return FactionMembershipService.isPlayerAlreadyAFactionLeader(playerToCheck);
    }

    public static Component REMOVE_PLAYER_LEADER(String player){
        return Component.translatable("chat.recruits.team_creation.removedPlayerLeader", player);
    }

    public static final Component PLAYER_REMOVED = Component.translatable("chat.recruits.team_creation.removedPlayer");

    public static Component ADDED_PLAYER(String s){
        return Component.translatable("chat.recruits.team_creation.addedPlayer", s);
    }

    public static Component ADDED_PLAYER_LEADER(String s){
        return Component.translatable("chat.recruits.team_creation.addedPlayerLeader", s);
    }

    public static Component CAN_NOT_ADD_OTHER_LEADER(){
        return Component.translatable("chat.recruits.team_creation.canNotAddOtherLeader");
    }

    public static Component PLAYER_LEFT_TEAM_LEADER(String s){
        return Component.translatable("chat.recruits.team_creation.playerLeftTeamLeader", s);
    }

    public static void addPlayerToData(ServerLevel level, String teamName, int x, String namePlayerToAdd){
        FactionMembershipService.addPlayerToData(level, teamName, x, namePlayerToAdd);
    }
    public static void addNPCToData(ServerLevel level, String teamName, int x){
        FactionMembershipService.addNPCToData(level, teamName, x);
    }

    public static void sendJoinRequest(ServerLevel level, ServerPlayer player, String stringID) {
        RecruitsFaction recruitsFaction = recruitsFactionManager.getFactionByStringID(stringID);

        if(recruitsFaction != null){
            if(recruitsFaction.addPlayerAsJoinRequest(player.getName().getString())){
                BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(()-> recruitsFactionManager.getTeamLeader(recruitsFaction, level)), new MessageToClientSetDiplomaticToast(7, recruitsFaction, player.getName().getString()));
                recruitsFactionManager.broadcastFactionsToAll(level);
            }
        }
        else BannerModMain.LOGGER.error("Could not add join request for "+ stringID + ".Team does not exist.");
    }

    public static void removeOfflinePlayerFromTeam(ServerPlayer player, String nameToRemove, ServerLevel level) {
        FactionRecruitTeamService.removeOfflinePlayerFromTeam(player, nameToRemove, level);
    }

    public static void tryToRemoveFromTeam(Team team, ServerPlayer serverPlayer, ServerPlayer potentialRemovePlayer, ServerLevel level, String nameToRemove, boolean menu) {
        FactionRecruitTeamService.tryToRemoveFromTeam(team, serverPlayer, potentialRemovePlayer, level, nameToRemove, menu);
    }

    public static ItemStack getCurrency(){
        return FactionEconomyService.getCurrency();
    }
    public static boolean playerHasEnoughEmeralds(ServerPlayer player, int price){
        return FactionEconomyService.playerHasEnoughEmeralds(player, price);
    }

    public static void doPayment(Player player, int costs){
        FactionEconomyService.doPayment(player, costs);
    }

    public static int playerGetEmeraldsInInventory(Player player, Item currency) {
        return FactionEconomyService.playerGetEmeraldsInInventory(player, currency);
    }

    public static void assignToTeamMate(ServerPlayer oldOwner, UUID newOwnerUUID, AbstractRecruitEntity recruit) {
        FactionRecruitTeamService.assignToTeamMate(oldOwner, newOwnerUUID, recruit);
    }

    @SubscribeEvent
    public void onTypeCommandEvent(CommandEvent event){
        new FactionTeamCommandBridge(this.server).onTypeCommandEvent(event);
    }


    public static void serverSideUpdateTeam(ServerLevel level){
        FactionMembershipService.serverSideUpdateTeam(level);
    }

    ////////////////////////////////////Recruit TEAM JOIN AND REMOVE////////////////////////////

    private static List<AbstractRecruitEntity> getRecruitsOfPlayer(UUID player_uuid, ServerLevel level) {
        return FactionRecruitTeamService.getRecruitsOfPlayer(player_uuid, level);
    }

    public static void addRecruitToTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level){
        FactionRecruitTeamService.addRecruitToTeam(recruits, team, level);
    }

    public static void addRecruitToTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level){
        FactionRecruitTeamService.addRecruitToTeam(recruit, team, level);
    }

    public static void removeRecruitFromTeam(String teamName, ServerPlayer player, ServerLevel level){
        FactionRecruitTeamService.removeRecruitFromTeam(teamName, player, level);
    }
    public static void removeRecruitFromTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level){
        FactionRecruitTeamService.removeRecruitFromTeam(recruits, team, level);
    }
    public static void removeRecruitFromTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level){
        FactionRecruitTeamService.removeRecruitFromTeam(recruit, team, level);
    }
    public static void openDisbandingScreen(Player player, UUID recruit) {
        FactionMenuService.openDisbandingScreen(player, recruit);
    }

    public static void openTeamEditScreen(Player player) {
        FactionMenuService.openTeamEditScreen(player);
    }
}
