package com.talhanation.bannermod.settlement.prefab.player;

import com.talhanation.bannermod.settlement.prefab.validation.BuildingValidationService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerBuildingRegistrationService {
    private PlayerBuildingRegistrationService() {
    }

    public static boolean register(@Nullable ServerPlayer player,
                                   ResourceLocation prefabId,
                                   BlockPos cornerA,
                                   BlockPos cornerB,
                                   BlockPos center,
                                   BlockPos keyBlock) {
        if (player == null || prefabId == null || cornerA == null || cornerB == null || center == null || keyBlock == null) {
            return false;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        BuildingValidationService.ValidationOutcome validation = BuildingValidationService.validate(
                player, prefabId, cornerA, cornerB, center);
        if (validation.outcome() != BuildingValidationService.Outcome.OK
                || validation.result() == null
                || !validation.result().passed()) {
            player.sendSystemMessage(Component.translatable("bannermod.prefab.register.failed_validation")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        BlockPos min = new BlockPos(
                Math.min(cornerA.getX(), cornerB.getX()),
                Math.min(cornerA.getY(), cornerB.getY()),
                Math.min(cornerA.getZ(), cornerB.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(cornerA.getX(), cornerB.getX()),
                Math.max(cornerA.getY(), cornerB.getY()),
                Math.max(cornerA.getZ(), cornerB.getZ())
        );
        if (!inside(keyBlock, min, max)) {
            player.sendSystemMessage(Component.translatable("bannermod.prefab.register.key_outside")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        BlockState keyState = serverLevel.getBlockState(keyBlock);
        if (keyState.isAir()) {
            player.sendSystemMessage(Component.translatable("bannermod.prefab.register.key_air")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        PlayerBuildingRegistrySavedData data = PlayerBuildingRegistrySavedData.get(serverLevel);
        PlayerBuildingRegistrySavedData.Entry entry = new PlayerBuildingRegistrySavedData.Entry(
                UUID.randomUUID(),
                player.getUUID(),
                prefabId.toString(),
                min,
                max,
                center,
                keyBlock,
                serverLevel.getGameTime()
        );
        data.add(entry);
        player.sendSystemMessage(Component.translatable("bannermod.prefab.register.ok", prefabId.toString())
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    private static boolean inside(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
}
