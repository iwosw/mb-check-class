package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.Objects;
import java.util.UUID;

/**
 * Builds a {@link MoraleSnapshot} for a single {@link AbstractRecruitEntity} from its live
 * battlefield neighbourhood. Pulled out of the AI-tick path so the policy resolution stays a
 * pure function: this class only owns the world-scan part, and {@link MoralePolicy} stays
 * unit-testable without a live world.
 *
 * <p>"Squad" is operationalised as "this recruit plus every other recruit within
 * {@link #NEIGHBOURHOOD_RADIUS} blocks that shares its owner UUID". Commander presence is the
 * pure-logic predicate from {@link CommanderAuraPolicy} applied to leader entities sharing
 * the same owner UUID — politically-neutral leaders never project an aura under that policy.
 *
 * <p>Casualty-fraction is approximated from the recruit's own HP fraction: a healthy
 * front-rank recruit reports 0 casualties, a half-HP recruit reports the squad as
 * "moderate losses", and a near-death recruit reports it as "heavy losses". This is a
 * deliberate proxy — squad-level casualty tracking would need a per-engagement ledger. The
 * proxy degrades gracefully: a fresh squad with no losses will not rout, and an HP-shredded
 * squad will, which is what gameplay needs. Tunable thresholds are public constants so a
 * future session can swap the proxy for a real ledger without touching call sites.</p>
 */
public final class RecruitMoraleSampler {

    /** Radius (blocks) around the recruit that counts as its "squad neighbourhood". */
    public static final double NEIGHBOURHOOD_RADIUS = 32.0D;

    /** HP fraction at or below which the squad is treated as "heavy" casualties. */
    public static final double HEAVY_HP_FRACTION = 0.25D;

    /** HP fraction at or below which the squad is treated as "moderate" casualties. */
    public static final double MODERATE_HP_FRACTION = 0.50D;

    /**
     * Recent-damage event count emitted when {@code hurtTime > 0} this tick. The policy's
     * SUSTAINED_FIRE_THRESHOLD is 6 events, so a single hurtTime hit alone never trips
     * suppression — it has to coincide with outnumbered / casualties pressure to matter.
     */
    public static final int HURT_TICK_EVENT_VALUE = 1;

    private RecruitMoraleSampler() {
    }

    public static MoraleSnapshot sample(AbstractRecruitEntity recruit, ServerLevel level) {
        if (recruit == null || level == null) {
            return new MoraleSnapshot(1, 0, 0, false, 0, 0, false);
        }

        int squadSize = 1;
        int nearbyAlly = 0;
        int hostiles = 0;
        boolean commanderPresent = false;

        UUID ownerUuid = recruit.getOwnerUUID();
        AABB box = recruit.getBoundingBox().inflate(NEIGHBOURHOOD_RADIUS);
        for (LivingEntity neighbour : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (neighbour == recruit || !neighbour.isAlive()) {
                continue;
            }
            if (neighbour instanceof AbstractRecruitEntity other
                    && ownerUuid != null
                    && Objects.equals(other.getOwnerUUID(), ownerUuid)) {
                squadSize++;
                nearbyAlly++;
                if (other instanceof AbstractLeaderEntity
                        && CommanderAuraPolicy.isAuraActive(
                                java.util.List.of(CommanderAura.at(ownerUuid, other.getX(), other.getY(), other.getZ())),
                                ownerUuid,
                                recruit.getX(), recruit.getY(), recruit.getZ())) {
                    commanderPresent = true;
                }
                continue;
            }
            if (recruit.canAttack(neighbour)) {
                hostiles++;
            }
        }

        int casualtiesProxy = casualtiesFromHpFraction(recruit, squadSize);
        int recentDamage = recruit.hurtTime > 0 ? HURT_TICK_EVENT_VALUE : 0;
        boolean isolated = nearbyAlly == 0;
        return new MoraleSnapshot(
                squadSize,
                casualtiesProxy,
                hostiles,
                commanderPresent,
                nearbyAlly,
                recentDamage,
                isolated
        );
    }

    static int casualtiesFromHpFraction(AbstractRecruitEntity recruit, int squadSize) {
        return casualtiesFromHpFraction(recruit.getHealth(), recruit.getMaxHealth(), squadSize);
    }

    /**
     * Pure helper used by the in-world sampler and exercised directly by JUnit. Maps an HP
     * fraction to a casualty count under {@link #HEAVY_HP_FRACTION} / {@link #MODERATE_HP_FRACTION}
     * thresholds so the mapping can be locked down without spinning up a live entity.
     */
    public static int casualtiesFromHpFraction(float currentHp, float maxHp, int squadSize) {
        if (maxHp <= 0.0F || squadSize <= 0) {
            return 0;
        }
        float frac = currentHp / maxHp;
        if (frac <= HEAVY_HP_FRACTION) {
            return Math.max(1, (int) Math.round(squadSize * 0.75D));
        }
        if (frac <= MODERATE_HP_FRACTION) {
            return Math.max(1, (int) Math.round(squadSize * 0.30D));
        }
        return 0;
    }
}
