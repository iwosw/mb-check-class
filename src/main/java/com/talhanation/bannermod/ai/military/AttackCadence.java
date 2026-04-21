package com.talhanation.bannermod.ai.military;

/**
 * Stage 3.D: per-unit attack cadence.
 *
 * <p>HYW tunes polearm attacks to have a real windup and slightly longer
 * cooldown than a plain sword swing. We model this without touching the damage
 * application path: the damage tick itself is unchanged, but the post-hit
 * cooldown is extended by a weapon-specific windup so the rhythm is
 * "swing → delayed recovery". This is option (b) from the stage brief — chosen
 * because option (a) (scheduling a deferred hit) would require a new per-recruit
 * scheduler and risks hitting through a blocked-in-between frame that the stance
 * auto-block cancels.
 *
 * <p>Kept framework-free so unit tests do not need a Minecraft runtime. The
 * integration callers pass the item's registry / description id in; they go
 * through {@link WeaponReach#effectiveReachForId(String)} here too so the reach
 * tag and cadence tag stay in lockstep.
 */
public final class AttackCadence {

    /** Extra cooldown ticks added after a spear hit. */
    public static final int SPEAR_WINDUP_TICKS = 2;
    /** Extra cooldown ticks added after a pike/halberd hit. */
    public static final int PIKE_WINDUP_TICKS = 4;
    /** Extra cooldown ticks added after a sarissa/long-spear hit. */
    public static final int SARISSA_WINDUP_TICKS = 5;

    /** Multiplier applied to the baseline cooldown for pike/halberd. */
    public static final double PIKE_COOLDOWN_MULT = 1.1D;
    /** Multiplier applied to the baseline cooldown for sarissa/long-spear. */
    public static final double SARISSA_COOLDOWN_MULT = 1.15D;

    private AttackCadence() {
    }

    /**
     * Returns the total post-hit cooldown in ticks for a weapon with the given
     * baseline cooldown and id.
     *
     * <p>baseline == {@code round(20 / attackSpeed) + 7} from the vanilla
     * formula, as produced by {@code AttackUtil.getAttackCooldown}.
     */
    public static int cooldownTicksFor(int baselineTicks, String itemId) {
        double extraReach = WeaponReach.effectiveReachForId(itemId);
        if (extraReach >= WeaponReach.SARISSA_EXTRA_REACH) {
            return (int) Math.round(baselineTicks * SARISSA_COOLDOWN_MULT) + SARISSA_WINDUP_TICKS;
        }
        if (extraReach >= WeaponReach.PIKE_EXTRA_REACH) {
            return (int) Math.round(baselineTicks * PIKE_COOLDOWN_MULT) + PIKE_WINDUP_TICKS;
        }
        if (extraReach >= WeaponReach.SPEAR_EXTRA_REACH) {
            return baselineTicks + SPEAR_WINDUP_TICKS;
        }
        return baselineTicks;
    }

    /**
     * Returns just the windup component (post-hit extra ticks) for the given id.
     * Useful when callers want to log / display the windup separately.
     */
    public static int windupTicksFor(String itemId) {
        double extraReach = WeaponReach.effectiveReachForId(itemId);
        if (extraReach >= WeaponReach.SARISSA_EXTRA_REACH) {
            return SARISSA_WINDUP_TICKS;
        }
        if (extraReach >= WeaponReach.PIKE_EXTRA_REACH) {
            return PIKE_WINDUP_TICKS;
        }
        if (extraReach >= WeaponReach.SPEAR_EXTRA_REACH) {
            return SPEAR_WINDUP_TICKS;
        }
        return 0;
    }
}
