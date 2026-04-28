package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentLog;
import com.talhanation.bannermod.army.command.RecruitSelectionRegistry;
import com.talhanation.bannermod.army.command.RecruitSelectionService;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.items.military.RecruitsSpawnEgg;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class DebugManagerAdminCommands {
    private static final String[] PROFILING_SUMMARY_PREFIXES = {
            "pathfinding",
            "recruit.index",
            "worker.index",
            "work_area.index",
            "settlement.heartbeat",
            "recruit.render",
            "network"
    };

    private DebugManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("debugManager")
                .then(Commands.literal("spawnFromEgg")
                        .then(Commands.argument("Amount", IntegerArgumentType.integer(0))
                                .executes(context -> spawnFromEgg(context.getSource(), IntegerArgumentType.getInteger(context, "Amount")))))
                .then(Commands.literal("intents")
                        .executes(context -> dumpIntents(context.getSource(), null, 20))
                        .then(Commands.argument("Count", IntegerArgumentType.integer(1, CommandIntentLog.MAX_ENTRIES_PER_PLAYER))
                                .executes(context -> dumpIntents(context.getSource(), null, IntegerArgumentType.getInteger(context, "Count")))
                                .then(Commands.argument("Player", StringArgumentType.word())
                                        .executes(context -> dumpIntents(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "Player"),
                                                IntegerArgumentType.getInteger(context, "Count"))))))
                .then(Commands.literal("profiling")
                        .executes(context -> dumpProfilingCounters(context.getSource()))
                        .then(Commands.literal("reset")
                                .executes(context -> resetProfilingCounters(context.getSource()))))
                .then(Commands.literal("select")
                        .then(Commands.literal("nearby")
                                .executes(context -> selectNearby(context.getSource(), 32.0D))
                                .then(Commands.argument("Radius", DoubleArgumentType.doubleArg(1.0D, 200.0D))
                                        .executes(context -> selectNearby(context.getSource(), DoubleArgumentType.getDouble(context, "Radius")))))
                        .then(Commands.literal("clear")
                                .executes(context -> clearSelection(context.getSource())))
                        .then(Commands.literal("list")
                                .executes(context -> listSelection(context.getSource()))));
    }

    private static int dumpProfilingCounters(CommandSourceStack source) {
        Map<String, Long> snapshot = RuntimeProfilingCounters.snapshot();
        if (snapshot.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No runtime profiling counters recorded.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Runtime profiling counters:").withStyle(ChatFormatting.GOLD), false);
        sendProfilingSummary(source, snapshot);
        for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
            source.sendSuccess(() -> Component.literal(entry.getKey() + "=" + entry.getValue()).withStyle(ChatFormatting.AQUA), false);
        }
        return snapshot.size();
    }

    private static void sendProfilingSummary(CommandSourceStack source, Map<String, Long> snapshot) {
        source.sendSuccess(() -> Component.literal("Summary by prefix:").withStyle(ChatFormatting.YELLOW), false);
        for (String prefix : PROFILING_SUMMARY_PREFIXES) {
            long total = 0L;
            for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
                String key = entry.getKey();
                if (key.equals(prefix) || key.startsWith(prefix + ".")) {
                    total += entry.getValue();
                }
            }
            long prefixTotal = total;
            source.sendSuccess(() -> Component.literal(prefix + "=" + prefixTotal).withStyle(ChatFormatting.DARK_AQUA), false);
        }
    }

    private static int resetProfilingCounters(CommandSourceStack source) {
        int clearedCounters = RuntimeProfilingCounters.reset();
        source.sendSuccess(() -> Component.literal("Runtime profiling counters reset; cleared " + clearedCounters + " counters.").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int selectNearby(CommandSourceStack source, double radius) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Run as a player.").withStyle(ChatFormatting.RED));
            return 0;
        }
        int count = RecruitSelectionService.selectNearby(player, radius);
        source.sendSuccess(() -> Component.literal("Selected " + count + " recruits within " + radius + " blocks.")
                .withStyle(ChatFormatting.GREEN), false);
        return count;
    }

    private static int clearSelection(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Run as a player.").withStyle(ChatFormatting.RED));
            return 0;
        }
        RecruitSelectionRegistry.instance().clear(player.getUUID());
        source.sendSuccess(() -> Component.literal("Selection cleared.").withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int listSelection(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Run as a player.").withStyle(ChatFormatting.RED));
            return 0;
        }
        int size = RecruitSelectionRegistry.instance().sizeFor(player.getUUID());
        source.sendSuccess(() -> Component.literal("Selection size: " + size).withStyle(ChatFormatting.AQUA), false);
        for (UUID recruitUuid : RecruitSelectionRegistry.instance().get(player.getUUID())) {
            source.sendSuccess(() -> Component.literal("  " + recruitUuid).withStyle(ChatFormatting.DARK_AQUA), false);
        }
        return size;
    }

    private static int dumpIntents(CommandSourceStack source, String playerName, int count) {
        UUID target;
        String label;
        if (playerName == null || playerName.isBlank()) {
            ServerPlayer selfPlayer = source.getPlayer();
            if (selfPlayer == null) {
                source.sendFailure(Component.literal("Run as a player or pass a player name.").withStyle(ChatFormatting.RED));
                return 0;
            }
            target = selfPlayer.getUUID();
            label = selfPlayer.getName().getString();
        } else {
            ServerPlayer namedPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (namedPlayer == null) {
                source.sendFailure(Component.literal("No online player: " + playerName).withStyle(ChatFormatting.RED));
                return 0;
            }
            target = namedPlayer.getUUID();
            label = namedPlayer.getName().getString();
        }

        List<CommandIntentLog.Entry> entries = CommandIntentLog.instance().recentFor(target);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No recent intents for " + label).withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        int limit = Math.min(count, entries.size());
        source.sendSuccess(() -> Component.literal(String.format("Last %d of %d intents for %s", limit, entries.size(), label))
                .withStyle(ChatFormatting.GOLD), false);
        for (int i = 0; i < limit; i++) {
            CommandIntentLog.Entry entry = entries.get(i);
            CommandIntent intent = entry.intent();
            String line = String.format("#%d tick=%d %s p=%d q=%s actors=%d%s",
                    i,
                    intent.issuedAtGameTime(),
                    intent.type().name(),
                    intent.priority(),
                    intent.queueMode(),
                    entry.actorCount(),
                    intentDetails(intent));
            source.sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.AQUA), false);
        }
        return limit;
    }

    private static String intentDetails(CommandIntent intent) {
        if (intent instanceof CommandIntent.Movement move) {
            return " state=" + move.movementState() + " f=" + move.formation() + (move.tight() ? " tight" : "")
                    + (move.targetPos() != null ? " -> " + move.targetPos() : "");
        }
        if (intent instanceof CommandIntent.Face face) {
            return " f=" + face.formation() + (face.tight() ? " tight" : "");
        }
        if (intent instanceof CommandIntent.Attack attack) {
            return " group=" + attack.groupUuid();
        }
        if (intent instanceof CommandIntent.StrategicFire fire) {
            return " group=" + fire.groupUuid() + " fire=" + fire.shouldFire();
        }
        if (intent instanceof CommandIntent.Aggro aggro) {
            return " state=" + aggro.state() + " group=" + aggro.groupUuid() + (aggro.fromGui() ? " gui" : "");
        }
        return "";
    }

    private static int spawnFromEgg(CommandSourceStack source, int amount) {
        ServerPlayer player = source.getPlayer();
        ServerLevel serverLevel = source.getLevel();
        if (player == null) {
            return 0;
        }

        ItemStack handItem = player.getMainHandItem();
        if (!(handItem.getItem() instanceof RecruitsSpawnEgg recruitsSpawnEgg)) {
            source.sendFailure(Component.literal("No Spawn Egg found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos pos = player.getOnPos();
        EntityType<?> entityType = recruitsSpawnEgg.getType(handItem);
        List<AbstractRecruitEntity> recruitEntities = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Entity entity = entityType.create(serverLevel);
            CompoundTag entityTag = RecruitsSpawnEgg.readEntityData(handItem);
            if (entity instanceof AbstractRecruitEntity recruit && entityTag != null) {
                RecruitsSpawnEgg.fillRecruit(recruit, entityTag, pos);
                recruitEntities.add(recruit);
            }
        }

        for (Entity entity : recruitEntities) {
            serverLevel.addFreshEntity(entity);
        }

        return 1;
    }
}
