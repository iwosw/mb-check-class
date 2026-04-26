package com.talhanation.bannermod.war.cooldown;

import com.talhanation.bannermod.war.runtime.DemilitarizationRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarState;

import java.util.Collection;
import java.util.UUID;

public final class WarCooldownPolicy {
    public static final long TICKS_PER_DAY = 24L * 60L * 60L * 20L;
    public static final long DEFAULT_PEACE_COOLDOWN_TICKS = 7L * TICKS_PER_DAY;
    public static final int DEFAULT_DEFENDER_DAILY_DECLARATIONS = 1;
    public static final long DEFAULT_LOST_TERRITORY_IMMUNITY_TICKS = 3L * TICKS_PER_DAY;
    public static final long DEFAULT_PEACEFUL_TOGGLE_COOLDOWN_TICKS = 2L * TICKS_PER_DAY;

    private WarCooldownPolicy() {
    }

    /**
     * Wrapper for the existing {@link #canDeclare} that also checks lost-territory
     * immunity on the defender. Both checks use the same {@link Result} shape so a caller
     * can surface the denial reason directly to the player.
     */
    public static Result canDeclareWithImmunity(UUID attackerId,
                                                UUID defenderId,
                                                Collection<WarDeclarationRecord> existingWars,
                                                long nowGameTime,
                                                long peaceCooldownTicks,
                                                int defenderDailyDeclarations,
                                                DemilitarizationRuntime demilitarizations,
                                                WarCooldownRuntime cooldowns) {
        Result base = canDeclare(attackerId, defenderId, existingWars, nowGameTime,
                peaceCooldownTicks, defenderDailyDeclarations, demilitarizations);
        if (!base.valid()) {
            return base;
        }
        if (cooldowns != null && cooldowns.isActive(defenderId, WarCooldownKind.LOST_TERRITORY_IMMUNITY, nowGameTime)) {
            return Result.invalid("defender_lost_territory_immunity");
        }
        return Result.ok();
    }

    public static Result canTogglePeacefulStatus(UUID politicalEntityId,
                                                 long nowGameTime,
                                                 WarCooldownRuntime cooldowns) {
        if (politicalEntityId == null) {
            return Result.invalid("missing_party");
        }
        if (cooldowns != null && cooldowns.isActive(politicalEntityId, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, nowGameTime)) {
            return Result.invalid("peaceful_toggle_cooldown_active");
        }
        return Result.ok();
    }

    public static Result canDeclare(UUID attackerId,
                                    UUID defenderId,
                                    Collection<WarDeclarationRecord> existingWars,
                                    long nowGameTime,
                                    long peaceCooldownTicks,
                                    int defenderDailyDeclarations,
                                    DemilitarizationRuntime demilitarizations) {
        if (attackerId == null || defenderId == null) {
            return Result.invalid("missing_party");
        }
        if (attackerId.equals(defenderId)) {
            return Result.invalid("self_declaration");
        }
        if (demilitarizations.isDemilitarized(attackerId, nowGameTime)) {
            return Result.invalid("attacker_demilitarized");
        }
        if (existingWars == null) {
            return Result.ok();
        }
        long peaceCutoff = nowGameTime - Math.max(0L, peaceCooldownTicks);
        long dayCutoff = nowGameTime - TICKS_PER_DAY;
        int defenderTodayCount = 0;
        for (WarDeclarationRecord war : existingWars) {
            boolean samePair = isSamePair(war, attackerId, defenderId);
            if (samePair && war.declaredAtGameTime() >= peaceCutoff && isLiveOrRecent(war.state())) {
                return Result.invalid("peace_cooldown_active");
            }
            if (!samePair
                    && war.defenderPoliticalEntityId().equals(defenderId)
                    && war.declaredAtGameTime() >= dayCutoff
                    && war.state() != WarState.CANCELLED) {
                defenderTodayCount++;
            }
        }
        if (defenderTodayCount >= defenderDailyDeclarations) {
            return Result.invalid("defender_daily_limit");
        }
        return Result.ok();
    }

    private static boolean isSamePair(WarDeclarationRecord war, UUID a, UUID b) {
        UUID attacker = war.attackerPoliticalEntityId();
        UUID defender = war.defenderPoliticalEntityId();
        return (attacker.equals(a) && defender.equals(b))
                || (attacker.equals(b) && defender.equals(a));
    }

    private static boolean isLiveOrRecent(WarState state) {
        return state == WarState.RESOLVED || state.allowsBattleWindowActivation();
    }

    public record Result(boolean valid, String reason) {
        public static Result ok() {
            return new Result(true, "");
        }

        public static Result invalid(String reason) {
            return new Result(false, reason);
        }
    }
}
