package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.server.command.EnumArgument;

final class DiplomacyManagerAdminCommands {
    private DiplomacyManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("diplomacyManager")
                /*
                .then(Commands.literal("getRelations")
                        .then(Commands.argument("Faction", TeamArgument.team())
                                .executes((context) -> {
                                    PlayerTeam playerTeam = TeamArgument.getTeam(context, "Faction");
                                    return 1;
                                })
                        )
                )
                 */
                .then(Commands.literal("setRelations")
                        .then(Commands.argument("Faction1", TeamArgument.team())
                                .then(Commands.argument("Faction2", TeamArgument.team())
                                        .then(Commands.argument("Relation", EnumArgument.enumArgument(RecruitsDiplomacyManager.DiplomacyStatus.class))
                                                .executes(context -> {
                                                    PlayerTeam playerTeam1 = TeamArgument.getTeam(context, "Faction1");
                                                    PlayerTeam playerTeam2 = TeamArgument.getTeam(context, "Faction2");
                                                    RecruitsDiplomacyManager.DiplomacyStatus status = context.getArgument("Relation", RecruitsDiplomacyManager.DiplomacyStatus.class);

                                                    if (playerTeam1.equals(playerTeam2)) {
                                                        context.getSource().sendFailure(Component.literal("Cannot set Diplomacy of same Faction!").withStyle(ChatFormatting.RED));
                                                        return 0;
                                                    }

                                                    if (FactionEvents.recruitsDiplomacyManager == null) {
                                                        context.getSource().sendFailure(Component.literal("recruitsDiplomacyManager == null!").withStyle(ChatFormatting.RED));
                                                        return 0;
                                                    }

                                                    FactionEvents.recruitsDiplomacyManager.setRelation(playerTeam1.getName(), playerTeam2.getName(), status, context.getSource().getLevel());
                                                    FactionEvents.recruitsDiplomacyManager.setRelation(playerTeam2.getName(), playerTeam1.getName(), status, context.getSource().getLevel());

                                                    context.getSource().sendSuccess(() ->
                                                            Component.literal(playerTeam1.getName() + " and " + playerTeam2.getName() + " are now " + status), false);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("getTreatyTime")
                        .then(Commands.argument("Faction1", TeamArgument.team())
                                .then(Commands.argument("Faction2", TeamArgument.team())
                                        .executes(context -> {
                                            PlayerTeam playerTeam1 = TeamArgument.getTeam(context, "Faction1");
                                            PlayerTeam playerTeam2 = TeamArgument.getTeam(context, "Faction2");

                                            if (FactionEvents.recruitsTreatyManager == null) {
                                                context.getSource().sendFailure(Component.literal("recruitsTreatyManager == null!").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }

                                            return TreatyAdminHelper.getTreatyTime(context, playerTeam1.getName(), playerTeam2.getName());
                                        })
                                )
                        )
                )
                .then(Commands.literal("setTreatyTime")
                        .then(Commands.argument("Faction1", TeamArgument.team())
                                .then(Commands.argument("Faction2", TeamArgument.team())
                                        .then(Commands.argument("Minutes", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    PlayerTeam playerTeam1 = TeamArgument.getTeam(context, "Faction1");
                                                    PlayerTeam playerTeam2 = TeamArgument.getTeam(context, "Faction2");
                                                    int minutes = IntegerArgumentType.getInteger(context, "Minutes");

                                                    if (playerTeam1.equals(playerTeam2)) {
                                                        context.getSource().sendFailure(Component.literal("Cannot set treaty between the same Faction!").withStyle(ChatFormatting.RED));
                                                        return 0;
                                                    }

                                                    if (FactionEvents.recruitsTreatyManager == null) {
                                                        context.getSource().sendFailure(Component.literal("recruitsTreatyManager == null!").withStyle(ChatFormatting.RED));
                                                        return 0;
                                                    }

                                                    return TreatyAdminHelper.setTreatyTime(context, playerTeam1.getName(), playerTeam2.getName(), minutes);
                                                })
                                        )
                                )
                        )
                );
    }
}
