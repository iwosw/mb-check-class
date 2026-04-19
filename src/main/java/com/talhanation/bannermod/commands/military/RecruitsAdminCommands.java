package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RecruitsAdminCommands {
    private static final List<String> RELATIONS = List.of("Ally", "Neutral", "Enemy");

    private static final SuggestionProvider<CommandSourceStack> RELATION_SUGGESTIONS =
            SuggestionProviders.register(new ResourceLocation("bannermod:relations"),
                    (context, builder) -> {
                        for (String relation : RELATIONS) {
                            builder.suggest(relation);
                        }
                        return builder.buildFuture();
                    });
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(createRootCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRootCommand() {
        return Commands.literal("recruits")
                .requires(source -> source.hasPermission(2))
                .then(createAdminCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAdminCommand() {
        return Commands.literal("admin")
                .then(createTeleportToOwnerCommand())
                .then(UnitsManagerAdminCommands.create())
                .then(FactionManagerAdminCommands.create())
                .then(DiplomacyManagerAdminCommands.create())
                .then(ClaimManagerAdminCommands.create())
                .then(NobleVillagerManagerAdminCommands.create())
                .then(DebugManagerAdminCommands.create());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createTeleportToOwnerCommand() {
        return Commands.literal("tpRecruitsToOwner")
                .then(Commands.argument("Owner", ScoreHolderArgument.scoreHolders())
                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                        .executes(context -> RecruitOwnerTeleportHelper.teleportToOwners(
                                context.getSource().getLevel(),
                                ScoreHolderArgument.getNamesWithDefaultWildcard(context, "Owner"))));
    }
}
