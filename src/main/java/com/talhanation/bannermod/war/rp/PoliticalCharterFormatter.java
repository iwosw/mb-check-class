package com.talhanation.bannermod.war.rp;

import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.UUID;

public final class PoliticalCharterFormatter {
    private PoliticalCharterFormatter() {
    }

    public static Component summary(PoliticalEntityRecord record) {
        if (record == null) {
            return Component.literal("(unknown)");
        }
        MutableComponent line = Component.literal("[" + record.status().name() + "] " + record.name());
        line.append(Component.literal(" id=" + shortId(record.id())));
        return line;
    }

    public static Component detail(PoliticalEntityRecord record) {
        if (record == null) {
            return Component.literal("(unknown)");
        }
        MutableComponent text = Component.literal("");
        text.append(Component.literal("State: " + record.name() + "\n"));
        text.append(Component.literal("Status: " + record.status().name() + "\n"));
        text.append(Component.literal("Id: " + record.id() + "\n"));
        text.append(Component.literal("Leader: " + record.leaderUuid() + "\n"));
        text.append(Component.literal("Capital: " + record.capitalPos().toShortString() + "\n"));
        if (!record.color().isEmpty()) {
            text.append(Component.literal("Color: " + record.color() + "\n"));
        }
        if (!record.homeRegion().isEmpty()) {
            text.append(Component.literal("Home region: " + record.homeRegion() + "\n"));
        }
        if (!record.charter().isEmpty()) {
            text.append(Component.literal("Charter: " + record.charter() + "\n"));
        }
        if (!record.ideology().isEmpty()) {
            text.append(Component.literal("Ideology: " + record.ideology() + "\n"));
        }
        return text;
    }

    public static String shortId(UUID id) {
        if (id == null) {
            return "?";
        }
        String full = id.toString();
        return full.length() > 8 ? full.substring(0, 8) : full;
    }
}
