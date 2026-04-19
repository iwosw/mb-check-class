package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

final class FactionManagerAdminCommands {
    private FactionManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("factionManager")
                .then(Commands.literal("getNPCCount")
                        .then(Commands.argument("Faction", TeamArgument.team())
                                .executes(context -> {
                                    RecruitsFaction faction = getFaction(context);
                                    if (faction == null) {
                                        return 0;
                                    }

                                    context.getSource().sendSuccess(() ->
                                            Component.literal(faction.getTeamDisplayName() + " has " + faction.npcs + " from max. " + faction.maxNPCs), false);
                                    return 1;
                                })))
                .then(Commands.literal("setNPCCount")
                        .then(Commands.argument("Faction", TeamArgument.team())
                                .then(Commands.argument("Amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            RecruitsFaction faction = getFaction(context);
                                            if (faction == null) {
                                                return 0;
                                            }

                                            int amount = IntegerArgumentType.getInteger(context, "Amount");
                                            return setFactionNPCsCount(context, faction, amount);
                                        }))))
                .then(Commands.literal("getLeader")
                        .then(Commands.argument("Faction", TeamArgument.team())
                                .executes(context -> {
                                    RecruitsFaction faction = getFaction(context);
                                    if (faction == null) {
                                        return 0;
                                    }

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("The Leader of " + faction.getTeamDisplayName() + " is " + faction.getTeamLeaderName()), false);
                                    return 1;
                                })))
                .then(Commands.literal("setLeader")
                        .then(Commands.argument("Faction", TeamArgument.team())
                                .then(Commands.argument("Player", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                        .executes(context -> {
                                            PlayerTeam playerTeam = TeamArgument.getTeam(context, "Faction");
                                            RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(playerTeam.getName());

                                            String playerName = ScoreHolderArgument.getName(context, "Player");
                                            ServerPlayer player = context.getSource().getLevel().getServer().getPlayerList().getPlayerByName(playerName);

                                            if (faction == null) {
                                                context.getSource().sendFailure(Component.literal("No Faction found!").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (player == null) {
                                                context.getSource().sendFailure(Component.literal("No Player found!").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (FactionEvents.isPlayerAlreadyAFactionLeader(player)) {
                                                context.getSource().sendFailure(Component.literal("Player is already a Leader of another Faction!").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            if (!playerTeam.getPlayers().contains(playerName)) {
                                                FactionEvents.addPlayerToTeam(null, context.getSource().getLevel(), faction.getStringID(), playerName);
                                            }

                                            faction.setTeamLeaderID(player.getUUID());
                                            faction.setTeamLeaderName(player.getName().getString());

                                            FactionEvents.modifyTeam(context.getSource().getLevel(), faction.getStringID(), faction, context.getSource().getPlayer(), 0);
                                            FactionEvents.recruitsFactionManager.save(context.getSource().getLevel());

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("The Leader of " + faction.getTeamDisplayName() + " is now " + faction.getTeamLeaderName()), false);
                                            return 1;
                                        }))))
                .then(Commands.literal("delete")
                        .then(Commands.argument("Faction", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String userInput = StringArgumentType.getString(context, "Faction");
                                    RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(userInput);

                                    if (faction == null) {
                                        context.getSource().sendFailure(Component.literal("No Faction found!").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    FactionEvents.recruitsFactionManager.removeTeam(faction.getStringID());
                                    FactionEvents.recruitsFactionManager.save(context.getSource().getLevel());

                                    try {
                                        PlayerTeam team = context.getSource().getLevel().getScoreboard().getPlayerTeam(userInput);
                                        if (team != null) {
                                            context.getSource().getLevel().getScoreboard().removePlayerTeam(team);
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Faction (" + userInput + ") was deleted"), false);
                                    return 1;
                                })));
    }

    private static RecruitsFaction getFaction(CommandContext<CommandSourceStack> context) {
        PlayerTeam playerTeam;
        try {
            playerTeam = TeamArgument.getTeam(context, "Faction");
        } catch (Exception ignored) {
            return null;
        }
        RecruitsFaction faction = FactionEvents.recruitsFactionManager.getFactionByStringID(playerTeam.getName());
        if (faction == null) {
            context.getSource().sendFailure(Component.literal("No Faction found!").withStyle(ChatFormatting.RED));
        }
        return faction;
    }

    private static int setFactionNPCsCount(CommandContext<CommandSourceStack> context, RecruitsFaction faction, int amount) {
        if (FactionEvents.recruitsFactionManager != null) {
            faction.setNPCs(amount);
            FactionEvents.recruitsFactionManager.save(context.getSource().getLevel());
            context.getSource().sendSuccess(() ->
                    Component.literal("The npc count of " + faction.getStringID() + " has been set to " + amount + "."), false);
            return 1;
        }
        return 0;
    }
}
