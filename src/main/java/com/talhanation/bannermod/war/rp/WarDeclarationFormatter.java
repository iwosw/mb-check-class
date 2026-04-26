package com.talhanation.bannermod.war.rp;

import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Optional;

public final class WarDeclarationFormatter {
    private WarDeclarationFormatter() {
    }

    public static Component summary(WarDeclarationRecord war, PoliticalRegistryRuntime registry) {
        if (war == null) {
            return Component.literal("(unknown)");
        }
        String attackerName = nameOf(registry, war.attackerPoliticalEntityId());
        String defenderName = nameOf(registry, war.defenderPoliticalEntityId());
        MutableComponent line = Component.literal("[" + war.state().name() + "] ");
        line.append(Component.literal(attackerName + " -> " + defenderName));
        line.append(Component.literal(" goal=" + war.goalType().name()));
        line.append(Component.literal(" id=" + PoliticalCharterFormatter.shortId(war.id())));
        return line;
    }

    public static Component detail(WarDeclarationRecord war, PoliticalRegistryRuntime registry) {
        if (war == null) {
            return Component.literal("(unknown)");
        }
        String attackerName = nameOf(registry, war.attackerPoliticalEntityId());
        String defenderName = nameOf(registry, war.defenderPoliticalEntityId());
        MutableComponent text = Component.literal("");
        text.append(Component.literal("War: " + attackerName + " vs " + defenderName + "\n"));
        text.append(Component.literal("State: " + war.state().name() + "\n"));
        text.append(Component.literal("Goal: " + war.goalType().name() + "\n"));
        text.append(Component.literal("Id: " + war.id() + "\n"));
        text.append(Component.literal("Casus belli: " + (war.casusBelli().isEmpty() ? "(none)" : war.casusBelli()) + "\n"));
        text.append(Component.literal("Declared at gameTime: " + war.declaredAtGameTime() + "\n"));
        text.append(Component.literal("Earliest activation: " + war.earliestActivationGameTime() + "\n"));
        text.append(Component.literal("Targets: " + war.targetPositions().size() + "\n"));
        text.append(Component.literal("Allies attacker: " + war.attackerAllyIds().size() + "\n"));
        text.append(Component.literal("Allies defender: " + war.defenderAllyIds().size() + "\n"));
        return text;
    }

    private static String nameOf(PoliticalRegistryRuntime registry, java.util.UUID id) {
        if (registry == null || id == null) {
            return "(unknown)";
        }
        Optional<PoliticalEntityRecord> record = registry.byId(id);
        return record.map(PoliticalEntityRecord::name).orElse(PoliticalCharterFormatter.shortId(id));
    }
}
