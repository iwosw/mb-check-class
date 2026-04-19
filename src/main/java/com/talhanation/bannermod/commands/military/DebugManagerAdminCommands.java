package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.items.military.RecruitsSpawnEgg;
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

final class DebugManagerAdminCommands {
    private DebugManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("debugManager")
                .then(Commands.literal("spawnFromEgg")
                        .then(Commands.argument("Amount", IntegerArgumentType.integer(0))
                                .executes(context -> spawnFromEgg(context.getSource(), IntegerArgumentType.getInteger(context, "Amount")))));
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
        EntityType<?> entityType = recruitsSpawnEgg.getType(handItem.getTag());
        List<AbstractRecruitEntity> recruitEntities = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Entity entity = entityType.create(serverLevel);
            CompoundTag entityTag = handItem.getTag();
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
