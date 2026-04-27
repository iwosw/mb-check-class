package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
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
     * Legacy single-tick event-value constant, kept for callers that want to know what one
     * raw hurt-tick contributes. The live sampler now reads
     * {@link RecruitMoraleService#recentDamageEventCount(java.util.UUID, long)} instead so
     * the SUSTAINED_FIRE token actually trips after enough hits land within the suppression
     * window, rather than capping at 1 per snapshot.
     */
    public static final int HURT_TICK_EVENT_VALUE = 1;

    private RecruitMoraleSampler() {
    }

    public static MoraleSnapshot sample(AbstractRecruitEntity recruit, ServerLevel level) {
        if (recruit == null || level == null) {
            return new MoraleSnapshot(1, 0, 0, false, 0, 0, false);
        }

        UUID ownerUuid = recruit.getOwnerUUID();
        Vec3 center = recruit.position();

        // Friendlies come from the per-owner index — chunk-pruned distance check, no
        // entity-class scan. Owned recruits are guaranteed to be in the index because
        // RecruitIndexEvents.onJoin / onLeave drive the membership directly.
        int squadSize = 1;
        int nearbyAlly = 0;
        boolean commanderPresent = false;
        if (ownerUuid != null) {
            List<AbstractRecruitEntity> friendlies = RecruitIndex.instance()
                    .ownerInRange(level, ownerUuid, center, NEIGHBOURHOOD_RADIUS);
            if (friendlies != null) {
                for (AbstractRecruitEntity ally : friendlies) {
                    if (ally == recruit || !ally.isAlive()) continue;
                    squadSize++;
                    nearbyAlly++;
                    if (!commanderPresent && ally instanceof AbstractLeaderEntity
                            && CommanderAuraPolicy.isAuraActive(
                                    List.of(CommanderAura.at(ownerUuid, ally.getX(), ally.getY(), ally.getZ())),
                                    ownerUuid,
                                    center.x, center.y, center.z)) {
                        commanderPresent = true;
                    }
                }
            }
        }

        // Hostile counting still requires a level scan because hostiles include vanilla
        // mobs / players / non-recruit entities that no per-owner index covers. Restrict
        // to LivingEntity in the inflated AABB and let canAttack() decide.
        int hostiles = 0;
        AABB box = recruit.getBoundingBox().inflate(NEIGHBOURHOOD_RADIUS);
        for (LivingEntity neighbour : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != recruit && e.isAlive())) {
            if (neighbour instanceof AbstractRecruitEntity other
                    && ownerUuid != null
                    && ownerUuid.equals(other.getOwnerUUID())) {
                continue; // already counted via the index, do not double-count.
            }
            if (recruit.canAttack(neighbour)) {
                hostiles++;
            }
        }

        int casualtiesProxy = casualtiesFromHpFraction(recruit, squadSize);
        // Suppression: read the rolling damage-event count maintained by RecruitMoraleService
        // off the recruit's hurt() hook, instead of mapping a single hurtTime tick to a 1.
        // The window expires automatically once SUPPRESSION_WINDOW_TICKS pass without a hit,
        // so leaving the line of fire clears the suppression token on its own.
        int recentDamage = RecruitMoraleService.recentDamageEventCount(
                recruit.getUUID(), level.getGameTime());
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
