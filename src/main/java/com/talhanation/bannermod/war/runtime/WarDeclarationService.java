package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.cooldown.WarCooldownPolicy;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.rp.WarDeclarationFormatter;
import com.talhanation.bannermod.war.rp.WarNoticeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WarDeclarationService {
    private WarDeclarationService() {
    }

    public static Result declare(MinecraftServer server,
                                 ServerLevel level,
                                 @Nullable UUID actorUuid,
                                 boolean operator,
                                 PoliticalEntityRecord attacker,
                                 PoliticalEntityRecord defender,
                                 WarGoalType goal,
                                 String casusBelli) {
        if (!PoliticalEntityAuthority.canAct(actorUuid, operator, attacker)) {
            return Result.denied(PoliticalEntityAuthority.denialReason(actorUuid, operator, attacker));
        }
        if (!attacker.status().canDeclareOffensiveWar()) {
            return Result.denied(Component.translatable("gui.bannermod.war.denial.attacker_status", attacker.status().name()));
        }

        WarDeclarationRuntime declarations = WarRuntimeContext.declarations(level);
        long gameTime = level.getGameTime();
        WarCooldownPolicy.Result cooldown = WarCooldownPolicy.canDeclareWithImmunity(
                attacker.id(), defender.id(),
                declarations.all(), gameTime, WarServerConfig.peaceCooldownTicks(), WarServerConfig.DefenderDailyDeclarations.get(),
                WarRuntimeContext.demilitarizations(level),
                WarRuntimeContext.cooldowns(level));
        if (!cooldown.valid()) {
            return Result.denied(Component.translatable("gui.bannermod.war.denial.cooldown", cooldown.reason()));
        }

        Optional<WarDeclarationRecord> declared = declarations.declareWar(
                attacker.id(),
                defender.id(),
                goal,
                casusBelli == null ? "" : casusBelli,
                List.of(),
                List.of(),
                List.of(),
                gameTime,
                WarServerConfig.MinDeclarationDelayTicks.get());
        if (declared.isEmpty()) {
            return Result.denied(Component.literal("Failed to declare war."));
        }

        WarDeclarationRecord war = declared.get();
        WarAuditLogSavedData audit = WarRuntimeContext.audit(level);
        audit.append(war.id(), "WAR_DECLARED",
                "attacker=" + attacker.id() + ";defender=" + defender.id() + ";goal=" + goal.name(),
                gameTime);

        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        WarNoticeService.broadcastDeclaration(server, war, registry);
        return Result.declared(Component.literal("War declared: ").append(WarDeclarationFormatter.summary(war, registry)), war);
    }

    public record Result(boolean success, Component message, @Nullable WarDeclarationRecord war) {
        private static Result declared(Component message, WarDeclarationRecord war) {
            return new Result(true, message, war);
        }

        private static Result denied(Component message) {
            return new Result(false, message, null);
        }
    }
}
