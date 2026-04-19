package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.talhanation.bannermod.entity.military.VillagerNobleEntity;
import com.talhanation.bannermod.persistence.military.RecruitsHireTrade;
import com.talhanation.bannermod.persistence.military.RecruitsHireTradesRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

final class NobleVillagerManagerAdminCommands {
    private NobleVillagerManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("nobleVillagerManager")
                .then(Commands.literal("addNobleTrade")
                        .then(Commands.argument("Resource", ResourceLocationArgument.id())
                                .then(Commands.argument("MaxUses", IntegerArgumentType.integer())
                                        .then(Commands.argument("VillagerNoble", EntityArgument.entity())
                                                .executes(context -> addNobleTrade(
                                                        context.getSource(),
                                                        ResourceLocationArgument.getId(context, "Resource"),
                                                        IntegerArgumentType.getInteger(context, "MaxUses"),
                                                        EntityArgument.getEntity(context, "VillagerNoble")))))))
                .then(Commands.literal("refreshAllTrades")
                        .then(Commands.argument("VillagerNoble", EntityArgument.entity())
                                .executes(context -> refreshAllTrades(context.getSource(), EntityArgument.getEntity(context, "VillagerNoble")))))
                .then(Commands.literal("levelup")
                        .then(Commands.argument("VillagerNoble", EntityArgument.entity())
                                .executes(context -> levelUp(context.getSource(), EntityArgument.getEntity(context, "VillagerNoble")))))
                .then(Commands.literal("removeNobleTrade")
                        .then(Commands.argument("Resource", ResourceLocationArgument.id())
                                .then(Commands.argument("VillagerNoble", EntityArgument.entity())
                                        .executes(context -> removeNobleTrade(
                                                context.getSource(),
                                                ResourceLocationArgument.getId(context, "Resource"),
                                                EntityArgument.getEntity(context, "VillagerNoble"))))));
    }

    private static int addNobleTrade(CommandSourceStack source, ResourceLocation resourceLocation, int maxUses, Entity entity) {
        VillagerNobleEntity nobleVillager = getNobleVillager(source, entity);
        if (nobleVillager == null) {
            return 0;
        }

        RecruitsHireTrade hireTrade = RecruitsHireTradesRegistry.getByResourceLocation(resourceLocation);
        if (hireTrade == null) {
            source.sendFailure(Component.literal("No Trade for " + resourceLocation + " found!"));
            return 0;
        }

        if (nobleVillager.hasTrade(resourceLocation)) {
            nobleVillager.removeTrade(resourceLocation);
        }

        hireTrade.maxUses = maxUses;
        hireTrade.uses = maxUses;
        nobleVillager.addTrade(hireTrade);
        source.sendSuccess(() -> Component.literal("Trade added!"), false);
        return 1;
    }

    private static int refreshAllTrades(CommandSourceStack source, Entity entity) {
        VillagerNobleEntity nobleVillager = getNobleVillager(source, entity);
        if (nobleVillager == null) {
            return 0;
        }

        List<RecruitsHireTrade> list = nobleVillager.getTrades();
        for (RecruitsHireTrade trade : list) {
            trade.uses = trade.maxUses;
        }

        nobleVillager.setTrades(list);
        source.sendSuccess(() -> Component.literal("Trades refreshed!"), false);
        return 1;
    }

    private static int levelUp(CommandSourceStack source, Entity entity) {
        VillagerNobleEntity nobleVillager = getNobleVillager(source, entity);
        if (nobleVillager == null) {
            return 0;
        }

        nobleVillager.addTraderProgress(100);
        source.sendSuccess(() -> Component.literal("Leveled up!"), false);
        return 1;
    }

    private static int removeNobleTrade(CommandSourceStack source, ResourceLocation resourceLocation, Entity entity) {
        VillagerNobleEntity nobleVillager = getNobleVillager(source, entity);
        if (nobleVillager == null) {
            return 0;
        }

        if (!nobleVillager.hasTrade(resourceLocation)) {
            source.sendFailure(Component.literal("No Trade for " + resourceLocation + " found!"));
            return 0;
        }

        nobleVillager.removeTrade(resourceLocation);
        source.sendSuccess(() -> Component.literal("Trade was removed!"), false);
        return 1;
    }

    private static VillagerNobleEntity getNobleVillager(CommandSourceStack source, Entity entity) {
        if (entity instanceof VillagerNobleEntity nobleVillager) {
            return nobleVillager;
        }

        source.sendFailure(Component.literal("Not a Noble Villager."));
        return null;
    }
}
