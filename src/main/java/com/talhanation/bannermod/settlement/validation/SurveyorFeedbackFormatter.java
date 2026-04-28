package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.BuildingType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public final class SurveyorFeedbackFormatter {
    private SurveyorFeedbackFormatter() {
    }

    public static void sendValidationResult(ServerPlayer player, BuildingValidationResult result) {
        if (player == null || result == null) {
            return;
        }
        if (!result.valid()) {
            player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.failed", buildingTypeLabel(result.type())).withStyle(ChatFormatting.RED));
            for (ValidationIssue issue : result.failures()) {
                player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.blocker", issue.message()).withStyle(ChatFormatting.RED));
            }
            return;
        }

        player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.passed", buildingTypeLabel(result.type())).withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.summary", result.capacity(), result.qualityScore()).withStyle(ChatFormatting.YELLOW));
        for (ValidationIssue warning : result.warnings()) {
            player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.warning", warning.message()).withStyle(ChatFormatting.GOLD));
        }
    }

    private static Component buildingTypeLabel(BuildingType type) {
        BuildingType safeType = type == null ? BuildingType.HOUSE : type;
        return Component.translatable("bannermod.surveyor.building_type." + safeType.name().toLowerCase(Locale.ROOT));
    }
}
