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
            player.sendSystemMessage(Component.literal("Validation failed for " + result.type().name() + ".").withStyle(ChatFormatting.RED));
            for (ValidationIssue issue : result.failures()) {
                player.sendSystemMessage(Component.literal("- " + issue.message()).withStyle(ChatFormatting.RED));
            }
            return;
        }

        player.sendSystemMessage(Component.literal("Validation passed for " + result.type().name() + ".").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Capacity: " + result.capacity() + " | Quality: " + result.qualityScore()).withStyle(ChatFormatting.YELLOW));
        for (ValidationIssue warning : result.warnings()) {
            player.sendSystemMessage(Component.literal("- " + warning.message()).withStyle(ChatFormatting.GOLD));
        }
    }
}
