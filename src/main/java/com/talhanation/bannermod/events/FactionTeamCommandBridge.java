package com.talhanation.bannermod.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.util.DelayedExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.event.CommandEvent;

import java.util.Objects;
import java.util.UUID;

final class FactionTeamCommandBridge {
    private final MinecraftServer server;

    FactionTeamCommandBridge(MinecraftServer server) {
        this.server = server;
    }

    void onTypeCommandEvent(CommandEvent event) {
        if (event.getParseResults() == null) {
            return;
        }

        String command = event.getParseResults().getReader().getString();
        CommandSourceStack sourceStack = event.getParseResults().getContext().build(command).getSource();
        ServerPlayer sender = sourceStack.getPlayer();
        ServerLevel level = this.server.overworld();

        if (sender != null) {
            if (command.contains("team")) {
                if (command.contains("add")) {
                    ItemStack mainhand = sender.getMainHandItem();
                    String[] parts = command.split(" ");
                    String teamName = parts[2];

                    FactionEvents.createTeam(false, sender, level, teamName, teamName, sender.getName().getString(), mainhand.getItem() instanceof BannerItem ? mainhand : null, ChatFormatting.WHITE, (byte) 0);
                    sourceStack.sendSuccess(() -> Component.translatable("commands.team.add.success", teamName), true);

                    event.setCanceled(true);
                    delayedServerSideUpdate(level);
                } else if (command.contains("remove")) {
                    String[] parts = command.split(" ");
                    String teamName = parts[2];
                    FactionEvents.leaveTeam(true, sender, teamName, level, false);
                    sourceStack.sendSuccess(() -> Component.translatable("commands.team.remove.success", teamName), true);
                    event.setCanceled(true);
                    delayedServerSideUpdate(level);
                } else if (command.contains("join") || command.contains("leave")) {
                    delayedServerSideUpdate(level);
                }
            }
            return;
        }

        if (command.contains("team")) {
            if (command.contains("add")) {
                String[] parts = command.split(" ");
                String teamName = parts[2];
                createTeamConsole(sourceStack, level, teamName, "white", (byte) 0);
                event.setCanceled(true);
            } else if (command.contains("remove")) {
                String[] parts = command.split(" ");
                String teamName = parts[2];

                PlayerTeam playerTeam = server.getScoreboard().getPlayerTeam(teamName);

                if (playerTeam != null) {
                    server.getScoreboard().removePlayerTeam(playerTeam);
                    FactionEvents.recruitsFactionManager.removeTeam(teamName);

                    sourceStack.sendSuccess(() -> Component.translatable("commands.team.remove.success", teamName), true);
                } else {
                    sourceStack.sendFailure(Component.translatable("team.notFound", teamName));
                }
                event.setCanceled(true);
            } else if (command.contains("join")) {
                String[] parts = command.split(" ");
                String teamName = parts[2];
                String playerName = parts[3];

                ServerPlayer player = this.server.getPlayerList().getPlayerByName(playerName);
                if (player != null) {
                    FactionEvents.addPlayerToTeam(player, this.server.overworld(), teamName, playerName);
                    sourceStack.sendSuccess(() -> Component.translatable("commands.team.join.success.single", playerName, teamName), true);
                    delayedServerSideUpdate(level);
                } else {
                    sourceStack.sendFailure(Component.translatable("argument.player.unknown"));
                }
                event.setCanceled(true);
            } else if (command.contains("leave")) {
                String[] parts = command.split(" ");
                String playerName = parts[2];

                ServerPlayer player = this.server.getPlayerList().getPlayerByName(playerName);
                if (player != null) {
                    Team team = player.getTeam();
                    FactionEvents.tryToRemoveFromTeam(team, player, player, this.server.overworld(), playerName, false);
                    sourceStack.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", playerName), true);
                    delayedServerSideUpdate(level);
                } else {
                    sourceStack.sendFailure(Component.translatable("argument.player.unknown"));
                }
                event.setCanceled(true);
            }
        }
    }

    void delayedServerSideUpdate(ServerLevel serverLevel) {
        DelayedExecutor.runLater(() -> FactionEvents.serverSideUpdateTeam(serverLevel), 500L);
    }

    private void createTeamConsole(CommandSourceStack sourceStack, ServerLevel level, String teamName, String color, byte colorByte) {
        PlayerTeam team = server.getScoreboard().getPlayerTeam(teamName);

        ItemStack banner = Items.BROWN_BANNER.getDefaultInstance();
        if (team == null) {
            if (teamName.chars().count() <= 13) {
                if (!(teamName.isBlank() || teamName.isEmpty())) {
                    if (!FactionEvents.recruitsFactionManager.isNameInUse(teamName)) {
                        Scoreboard scoreboard = server.getScoreboard();
                        PlayerTeam newTeam = scoreboard.addPlayerTeam(teamName);
                        newTeam.setDisplayName(Component.literal(teamName));

                        newTeam.setColor(Objects.requireNonNull(ChatFormatting.getByName(color)));
                        newTeam.setAllowFriendlyFire(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamFriendlyFireSetting.get());
                        newTeam.setSeeFriendlyInvisibles(RecruitsServerConfig.GlobalTeamSetting.get() && RecruitsServerConfig.GlobalTeamSeeFriendlyInvisibleSetting.get());

                        FactionEvents.recruitsFactionManager.addTeam(teamName, teamName, new UUID(0, 0), "none", banner.serializeNBT(), colorByte, newTeam.getColor());

                        BannerModMain.LOGGER.info("The new Team " + teamName + " has been created by console.");

                        FactionEvents.recruitsFactionManager.save(level);
                    } else {
                        sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(ChatFormatting.RED));
                    }
                } else {
                    sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.noname").withStyle(ChatFormatting.RED));
                }
            } else {
                sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.teamname_to_long").withStyle(ChatFormatting.RED));
            }
        } else {
            sourceStack.sendFailure(Component.translatable("chat.recruits.team_creation.team_exists").withStyle(ChatFormatting.RED));
        }
    }
}
