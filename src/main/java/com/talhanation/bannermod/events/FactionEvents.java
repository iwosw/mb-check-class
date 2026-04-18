package com.talhanation.bannermod.events;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.runtime.FactionEconomyService;
import com.talhanation.bannermod.events.runtime.FactionLifecycleService;
import com.talhanation.bannermod.events.runtime.FactionMembershipService;
import com.talhanation.bannermod.events.runtime.FactionNotifier;
import com.talhanation.bannermod.inventory.military.*;
import com.talhanation.bannermod.network.messages.military.*;
import com.talhanation.bannermod.util.DelayedExecutor;
import com.talhanation.bannermod.persistence.military.*;
import com.talhanation.bannermod.events.FactionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        MinecraftServer server = level.getServer();
        Team team = player.getTeam();
        if (team == null) return;

        String teamName = team.getName();
        RecruitsFaction recruitsFaction = recruitsFactionManager.getFactionByStringID(teamName);
        if (recruitsFaction == null) return;

        boolean isLeader = recruitsFaction.getTeamLeaderUUID().equals(player.getUUID());
        if (!isLeader) return;

        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);
        if (playerTeam == null || !playerTeam.getPlayers().contains(nameToRemove)) return;

        server.getScoreboard().removePlayerFromTeam(nameToRemove, playerTeam);
        recruitsFaction.removeMember(nameToRemove);
        addPlayerToData(level, teamName, -1, nameToRemove);

        recruitsFactionManager.save(server.overworld());
        BannerModMain.LOGGER.info("Offline player " + nameToRemove + " removed from team " + teamName + " by " + player.getName().getString());
    }

    public static void tryToRemoveFromTeam(Team team, ServerPlayer serverPlayer, ServerPlayer potentialRemovePlayer, ServerLevel level, String nameToRemove, boolean menu) {
        if (potentialRemovePlayer != null && team != null) {
            boolean isPlayerToRemove = potentialRemovePlayer.getName().getString().equals(nameToRemove);

            if (isPlayerToRemove) {
                FactionEvents.leaveTeam(false, potentialRemovePlayer, null, level, true);
                potentialRemovePlayer.sendSystemMessage(PLAYER_REMOVED);
                if(menu)serverPlayer.sendSystemMessage(REMOVE_PLAYER_LEADER(potentialRemovePlayer.getDisplayName().getString()));

                List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(serverPlayer.getUUID(), level);
                int recruitCount = recruits.size();

                addNPCToData(level, team.getName(), -recruitCount);
                removeRecruitFromTeam(recruits, team, level);
            }
        }
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
        ServerLevel level = (ServerLevel) oldOwner.getCommandSenderWorld();

        Team team = oldOwner.getTeam();

        if(team != null){
           Collection<String> list = team.getPlayers().stream().toList();
           List<ServerPlayer> playerList = level.players();

           boolean playerNotFound = false;
           ServerPlayer newOwner = playerList.stream().filter(player -> player.getUUID().equals(newOwnerUUID)).findFirst().orElse(null);

            if(newOwner != null){
                if(list.contains(newOwner.getName().getString())){

                    if (!RecruitEvents.recruitsPlayerUnitManager.canPlayerRecruit(team.getName(), newOwnerUUID)) {
                        oldOwner.sendSystemMessage(Component.translatable("chat.recruits.team.assignNewOwnerLimitReached"));
                        return;
                    }
                    recruit.disband(oldOwner, true, true);

                    BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(()-> newOwner), new MessageToClientSetToast(0, oldOwner.getName().getString()));

                    recruit.hire(newOwner, null, true);
                }
                else
                    playerNotFound = true;
            }
            else
                playerNotFound = true;

            if(playerNotFound) oldOwner.sendSystemMessage(Component.translatable("chat.recruits.team.assignNewOwnerNotFound"));
        }
    }

    @SubscribeEvent
    public void onTypeCommandEvent(CommandEvent event){
        if (event.getParseResults() != null) {
            String command = event.getParseResults().getReader().getString();
            CommandSourceStack sourceStack = event.getParseResults().getContext().build(command).getSource();
            ServerPlayer sender = sourceStack.getPlayer();
            ServerLevel level = this.server.overworld();

            if(sender != null){
                if(command.contains("team")){
                    if(command.contains("add")) {
                        ItemStack mainhand = (sender).getMainHandItem();
                        String[] parts = command.split(" ");
                        String teamName = parts[2];

                        createTeam(false, sender, level, teamName, teamName, sender.getName().getString(), mainhand.getItem() instanceof BannerItem ? mainhand : null, ChatFormatting.WHITE, (byte) 0);
                        sourceStack.sendSuccess(() -> Component.translatable("commands.team.add.success", teamName), true);

                        event.setCanceled(true);
                        delayedServerSideUpdate(level);
                    }
                    else if(command.contains("remove")){
                        String[] parts = command.split(" ");
                        String teamName = parts[2];
                        leaveTeam(true,sender, teamName, level, false);
                        sourceStack.sendSuccess(() -> Component.translatable("commands.team.remove.success", teamName), true);
                        event.setCanceled(true);
                        delayedServerSideUpdate(level);
                    }
                    else if(command.contains("join") || command.contains("leave")){
                        delayedServerSideUpdate(level);
                    }
                }
            }
            else {
                if (command.contains("team")) {
                    if (command.contains("add")) {
                        String[] parts = command.split(" ");
                        String teamName = parts[2];
                        createTeamConsole(sourceStack, level, teamName, "white", (byte) 0);
                        event.setCanceled(true);
                    }
                    else if (command.contains("remove")) {
                        String[] parts = command.split(" ");
                        String teamName = parts[2];

                        PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);

                        if (playerTeam != null) {

                            server.getScoreboard().removePlayerTeam(playerTeam);
                            recruitsFactionManager.removeTeam(teamName);

                            sourceStack.sendSuccess(() -> Component.translatable("commands.team.remove.success", teamName), true);
                        } else {
                            sourceStack.sendFailure(Component.translatable("team.notFound", teamName));
                        }
                        event.setCanceled(true);
                    }
                    else if (command.contains("join")) {
                        String[] parts = command.split(" ");
                        String teamName = parts[2];
                        String playerName = parts[3];

                        ServerPlayer player = this.server.getPlayerList().getPlayerByName(playerName);
                        if (player != null) {
                            addPlayerToTeam(player, this.server.overworld(), teamName, playerName);
                            sourceStack.sendSuccess(() -> Component.translatable("commands.team.join.success.single", playerName, teamName), true);
                            delayedServerSideUpdate(level);
                        } else {
                            sourceStack.sendFailure(Component.translatable("argument.player.unknown"));
                        }
                        event.setCanceled(true);
                    }
                    else if (command.contains("leave")) {
                        String[] parts = command.split(" ");
                        String playerName = parts[2];

                        ServerPlayer player = this.server.getPlayerList().getPlayerByName(playerName);
                        if (player != null) {
                            Team team = player.getTeam();
                            tryToRemoveFromTeam(team, player, player,this.server.overworld(), playerName, false);
                            sourceStack.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", playerName), true);
                            delayedServerSideUpdate(level);
                        } else {
                            sourceStack.sendFailure(Component.translatable("argument.player.unknown"));
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    public void delayedServerSideUpdate(ServerLevel serverLevel){
        DelayedExecutor.runLater(()-> serverSideUpdateTeam(serverLevel), 500L);
    }

    private void createTeamConsole(CommandSourceStack sourceStack, ServerLevel  level, String teamName, String color, byte colorByte) {
        PlayerTeam team = server.getScoreboard().getPlayerTeam(teamName);

        ItemStack banner = Items.BROWN_BANNER.getDefaultInstance();
        if (team == null) {
            if (teamName.chars().count() <= 13) {
                if (!(teamName.isBlank() || teamName.isEmpty())) {
                    if (!recruitsFactionManager.isNameInUse(teamName)) {
                        Scoreboard scoreboard = server.getScoreboard();
                        PlayerTeam newTeam = scoreboard.addPlayerTeam(teamName);
                        newTeam.setDisplayName(Component.literal(teamName));

                        newTeam.setColor(Objects.requireNonNull(ChatFormatting.getByName(color)));
                        newTeam.setAllowFriendlyFire(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamFriendlyFireSetting.get());
                        newTeam.setSeeFriendlyInvisibles(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamSeeFriendlyInvisibleSetting.get());

                        recruitsFactionManager.addTeam(teamName, teamName, new UUID(0,0),"none", banner.serializeNBT(), colorByte, newTeam.getColor());

                        BannerModMain.LOGGER.info("The new Team " + teamName + " has been created by console.");

                        recruitsFactionManager.save(server.overworld());
                    }
                    else
                        sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(ChatFormatting.RED));
                }
                else
                    sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.noname").withStyle(ChatFormatting.RED));
            }
            else
                sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.teamname_to_long").withStyle(ChatFormatting.RED));
        }
        else
            sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(ChatFormatting.RED));
    }


    public static void serverSideUpdateTeam(ServerLevel level){
        FactionMembershipService.serverSideUpdateTeam(level);
    }

    ////////////////////////////////////Recruit TEAM JOIN AND REMOVE////////////////////////////

    private static List<AbstractRecruitEntity> getRecruitsOfPlayer(UUID player_uuid, ServerLevel level) {
        List<AbstractRecruitEntity> list = new ArrayList<>();

        for(Entity entity : level.getEntities().getAll()){
            if(entity instanceof AbstractRecruitEntity recruit && recruit.getOwner() != null && recruit.getOwnerUUID().equals(player_uuid))
                list.add(recruit);
        }
        return list;
    }

    public static void addRecruitToTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level){
        for(AbstractRecruitEntity recruit : recruits){
            addRecruitToTeam(recruit, team, level);
        }
    }

    public static void addRecruitToTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level){
        String teamName = team.getName();
        PlayerTeam playerteam = level.getScoreboard().getPlayerTeam(teamName);
        RecruitsFaction recruitsFaction = recruitsFactionManager.getFactionByStringID(teamName);

        boolean flag = playerteam != null && level.getScoreboard().addPlayerToTeam(recruit.getStringUUID(), playerteam);
        if (!flag) {
            BannerModMain.LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", teamName);
        } else{
            recruit.setTarget(null);// fix "if owner was other team and now same team und was target"
            if(recruitsFaction != null) recruit.setColor(recruitsFaction.getUnitColor());
        }
    }

    public static void removeRecruitFromTeam(String teamName, ServerPlayer player, ServerLevel level){
        List<AbstractRecruitEntity> recruits = getRecruitsOfPlayer(player.getUUID(), level);
        Team team = level.getScoreboard().getPlayerTeam(teamName);
        if(team  != null){
            removeRecruitFromTeam(recruits, team, level);
        }
    }
    public static void removeRecruitFromTeam(List<AbstractRecruitEntity> recruits, Team team, ServerLevel level){
        for(AbstractRecruitEntity recruit : recruits){
            removeRecruitFromTeam(recruit, team, level);
        }
    }
    public static void removeRecruitFromTeam(AbstractRecruitEntity recruit, Team team, ServerLevel level){
        if(recruit == null || team == null) return;

        Team recruitsFaction = recruit.getTeam();

        if(recruitsFaction != null && recruitsFaction.equals(team)){
            PlayerTeam recruitTeam = level.getScoreboard().getPlayerTeam(team.getName());
            if(recruitTeam != null) level.getScoreboard().removePlayerFromTeam(recruit.getStringUUID(), recruitTeam);
        }
    }
    public static void openDisbandingScreen(Player player, UUID recruit) {
        if (player instanceof ServerPlayer) {
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("disband_screen");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new DisbandContainer(i, playerEntity, recruit);
                }
            }, packetBuffer -> {
                packetBuffer.writeUUID(recruit);
            });
        } else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenDisbandScreen(player, recruit));
        }
    }

    public static void openTeamEditScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {

                @Override
                public Component getDisplayName() {
                    return Component.literal("team_edit_screen");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new TeamEditMenu(i, playerInventory);
                }
            }, packetBuffer -> {
                packetBuffer.writeUUID(player.getUUID());
            });
        }
        else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenTeamEditScreen(player));
        }
    }
}
