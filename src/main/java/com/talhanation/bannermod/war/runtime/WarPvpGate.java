package com.talhanation.bannermod.war.runtime;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

/** Pure policy for regulated PvP activation. Event handlers should call this before damage applies. */
public final class WarPvpGate {
    private WarPvpGate() {
    }

    public static boolean allowsWarPvp(UUID attackerPoliticalEntityId,
                                       UUID defenderPoliticalEntityId,
                                       Collection<WarDeclarationRecord> wars,
                                       BattleWindowSchedule schedule,
                                       ZonedDateTime now,
                                       boolean insideWarZone) {
        if (attackerPoliticalEntityId == null || defenderPoliticalEntityId == null) {
            return false;
        }
        if (attackerPoliticalEntityId.equals(defenderPoliticalEntityId)) {
            return false;
        }
        if (!insideWarZone) {
            return false;
        }
        if (schedule == null || !schedule.isOpen(now)) {
            return false;
        }
        if (wars == null || wars.isEmpty()) {
            return false;
        }
        for (WarDeclarationRecord war : wars) {
            if (war.state().allowsBattleWindowActivation()
                    && war.opposingSides(attackerPoliticalEntityId, defenderPoliticalEntityId)) {
                return true;
            }
        }
        return false;
    }
}
