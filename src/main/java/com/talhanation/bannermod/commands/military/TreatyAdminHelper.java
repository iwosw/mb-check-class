package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.context.CommandContext;
import com.talhanation.bannermod.events.FactionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

final class TreatyAdminHelper {
    private TreatyAdminHelper() {
    }

    static int getTreatyTime(CommandContext<CommandSourceStack> context, String faction1, String faction2) {
        long remainingMs = FactionEvents.recruitsTreatyManager.getTreatyRemainingMillis(faction1, faction2);

        if (remainingMs <= 0) {
            sendTreatyMessage(context, "No active treaty between " + faction1 + " and " + faction2 + ".");
            return 1;
        }

        long totalMinutes = remainingMs / 60_000;
        long hours = totalMinutes / 60;

        sendTreatyMessage(context, "Treaty between " + faction1 + " and " + faction2
                + ": " + totalMinutes + " min (" + hours + "h) remaining.");
        return 1;
    }

    static int setTreatyTime(CommandContext<CommandSourceStack> context, String faction1, String faction2, int minutes) {
        ServerLevel level = context.getSource().getLevel();

        if (minutes == 0) {
            FactionEvents.recruitsTreatyManager.removeTreaty(faction1, faction2, level);
            sendTreatyMessage(context, "Treaty between " + faction1 + " and " + faction2 + " has been removed.");
            return 1;
        }

        long expiryMs = System.currentTimeMillis() + (long) minutes * 60_000;
        FactionEvents.recruitsTreatyManager.getTreaties();
        FactionEvents.recruitsTreatyManager.addTreatyRaw(faction1, faction2, expiryMs, level);

        long hours = minutes / 60;
        sendTreatyMessage(context, "Treaty between " + faction1 + " and " + faction2
                + " set to " + minutes + " min (" + hours + "h).");
        return 1;
    }

    private static void sendTreatyMessage(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }
}
