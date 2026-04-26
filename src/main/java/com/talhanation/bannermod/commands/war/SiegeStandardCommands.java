package com.talhanation.bannermod.commands.war;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.talhanation.bannermod.registry.war.ModWarBlocks;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.SiegeStandardPlacementService;
import com.talhanation.bannermod.war.runtime.SiegeStandardRecord;
import com.talhanation.bannermod.war.runtime.SiegeStandardRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;

public final class SiegeStandardCommands {
    private SiegeStandardCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("siege")
                .then(Commands.literal("place")
                        .then(Commands.argument("warId", StringArgumentType.word())
                                .then(Commands.argument("side", StringArgumentType.word())
                                        .executes(ctx -> place(ctx, null, -1))
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(ctx -> place(ctx,
                                                        BlockPosArgument.getBlockPos(ctx, "pos"),
                                                        -1))
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(8, 512))
                                                        .executes(ctx -> place(ctx,
                                                                BlockPosArgument.getBlockPos(ctx, "pos"),
                                                                IntegerArgumentType.getInteger(ctx, "radius"))))))))
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx, null))
                        .then(Commands.argument("warId", StringArgumentType.word())
                                .executes(ctx -> list(ctx, StringArgumentType.getString(ctx, "warId")))))
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("standardId", StringArgumentType.word())
                                .executes(SiegeStandardCommands::remove)));
    }

    private static int place(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                             BlockPos overridePos,
                             int overrideRadius) throws CommandSyntaxException {
        String warToken = StringArgumentType.getString(context, "warId");
        String sideToken = StringArgumentType.getString(context, "side");
        WarDeclarationRecord war = WarCommandSupport.requireWar(context, warToken);
        PoliticalEntityRecord side = WarCommandSupport.requireEntity(context, sideToken);
        BlockPos pos = overridePos;
        if (pos == null && context.getSource().getEntity() == null) {
            context.getSource().sendFailure(Component.literal("Provide a position or run as a player."));
            return 0;
        }
        ServerPlayer actor = context.getSource().getEntity() instanceof ServerPlayer player ? player : null;
        ServerLevel level = WarCommandSupport.level(context);
        SiegeStandardPlacementService.Result result = SiegeStandardPlacementService.placeAt(
                level, actor, war.id(), side.id(), pos, overrideRadius);
        if (!result.ok()) {
            if (result.outcome() == SiegeStandardPlacementService.Outcome.NOT_LEADER) {
                throw WarCommandSupport.ERR_NOT_LEADER.create();
            }
            context.getSource().sendFailure(Component.literal(SiegeStandardPlacementService.describe(result.outcome())));
            return 0;
        }
        SiegeStandardRecord record = result.record();
        WarCommandSupport.reply(context,
                "Siege standard placed for " + side.name() + " at " + record.pos().toShortString()
                        + " (radius " + record.radius() + ", id " + shortId(record.id()) + ").");
        return 1;
    }

    private static int list(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
                            String warToken) throws CommandSyntaxException {
        SiegeStandardRuntime runtime = WarRuntimeContext.sieges(WarCommandSupport.level(context));
        java.util.Collection<SiegeStandardRecord> records;
        if (warToken == null) {
            records = runtime.all();
        } else {
            WarDeclarationRecord war = WarCommandSupport.requireWar(context, warToken);
            records = runtime.forWar(war.id());
        }
        if (records.isEmpty()) {
            WarCommandSupport.reply(context, "No siege standards.");
            return 0;
        }
        PoliticalRegistryRuntime registry = WarCommandSupport.registry(context);
        MutableComponent text = Component.literal("Siege standards:\n");
        for (SiegeStandardRecord record : records) {
            String sideName = registry.byId(record.sidePoliticalEntityId())
                    .map(PoliticalEntityRecord::name)
                    .orElse(shortId(record.sidePoliticalEntityId()));
            text.append(Component.literal(" id=" + shortId(record.id())
                    + " war=" + shortId(record.warId())
                    + " side=" + sideName
                    + " pos=" + record.pos().toShortString()
                    + " radius=" + record.radius() + "\n"));
        }
        WarCommandSupport.replyComponent(context, text);
        return records.size();
    }

    private static int remove(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        String token = StringArgumentType.getString(context, "standardId");
        ServerLevel level = WarCommandSupport.level(context);
        SiegeStandardRuntime runtime = WarRuntimeContext.sieges(level);
        UUID id = parseFragment(runtime, token);
        if (id == null) {
            context.getSource().sendFailure(Component.literal("Siege standard not found."));
            return 0;
        }
        Optional<SiegeStandardRecord> existing = runtime.byId(id);
        if (existing.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Siege standard not found."));
            return 0;
        }
        SiegeStandardRecord record = existing.get();
        // Remove the data record first; if a block exists at the recorded position, also clear it.
        // SiegeStandardBlock.onRemove guards against a double-remove via the same lookup.
        boolean removed = runtime.remove(id);
        if (level.getBlockState(record.pos()).is(ModWarBlocks.SIEGE_STANDARD.get())) {
            level.setBlockAndUpdate(record.pos(), Blocks.AIR.defaultBlockState());
        }
        if (!removed) {
            context.getSource().sendFailure(Component.literal("Failed to remove siege standard."));
            return 0;
        }
        WarCommandSupport.reply(context, "Siege standard removed.");
        return 1;
    }

    private static UUID parseFragment(SiegeStandardRuntime runtime, String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            UUID id = UUID.fromString(token);
            return runtime.byId(id).map(SiegeStandardRecord::id).orElse(null);
        } catch (IllegalArgumentException ignored) {
            String lower = token.toLowerCase(java.util.Locale.ROOT);
            for (SiegeStandardRecord record : runtime.all()) {
                if (record.id().toString().startsWith(lower)) {
                    return record.id();
                }
            }
            return null;
        }
    }

    private static String shortId(UUID id) {
        if (id == null) {
            return "?";
        }
        String full = id.toString();
        return full.length() > 8 ? full.substring(0, 8) : full;
    }
}
