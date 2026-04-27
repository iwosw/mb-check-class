package com.talhanation.bannermod.settlement.validation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SurveyorFeedbackFormatter {
    private SurveyorFeedbackFormatter() {
    }

    public static void sendValidationResult(ServerPlayer player, BuildingValidationResult result) {
        if (player == null || result == null) {
            return;
        }
        if (!result.valid()) {
            player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.failed", result.type().name()).withStyle(ChatFormatting.RED));
            for (ValidationIssue issue : result.failures()) {
                player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.blocker", issue.message()).withStyle(ChatFormatting.RED));
            }
            return;
        }

        player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.passed", result.type().name()).withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.summary", result.capacity(), result.qualityScore()).withStyle(ChatFormatting.YELLOW));
        for (ValidationIssue warning : result.warnings()) {
            player.sendSystemMessage(Component.translatable("bannermod.surveyor.validation.warning", warning.message()).withStyle(ChatFormatting.GOLD));
        }
    }
}
