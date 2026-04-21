package com.talhanation.bannermod.ai.military;

/**
 * Stage 2 pure helpers: stance-aware damage reduction when a directional
 * shield block fires.
 *
 * <p>Kept framework-free so unit tests do not need a Minecraft runtime.
 */
public final class ShieldMitigation {

    /** Fraction of damage REMAINING after block in LOOSE stance (45% absorbed). */
    public static final float LOOSE_REMAINING = 0.55f;
    /** Fraction of damage REMAINING after block in LINE_HOLD stance (55% absorbed). */
    public static final float LINE_HOLD_REMAINING = 0.45f;
    /** Fraction of damage REMAINING after block in SHIELD_WALL stance (70% absorbed). */
    public static final float SHIELD_WALL_REMAINING = 0.30f;

    /** Stagger factor: while {@code blockCoolDown > 0}, reduce mitigation by 40%. */
    public static final float STAGGER_REDUCTION = 0.40f;

    private ShieldMitigation() {
    }

    /** Returns the per-stance "remaining damage" fraction. */
    public static float remainingFractionFor(CombatStance stance) {
        if (stance == null) {
            return LOOSE_REMAINING;
        }
        switch (stance) {
            case SHIELD_WALL:
                return SHIELD_WALL_REMAINING;
            case LINE_HOLD:
                return LINE_HOLD_REMAINING;
            case LOOSE:
            default:
                return LOOSE_REMAINING;
        }
    }

    /**
     * Return the damage to apply after a potential shield block.
     *
     * <p>If {@code shieldUp} is false or {@code inCone} is false, the raw damage
     * is returned unchanged. Otherwise the stance's remaining-fraction is applied.
     * When {@code blockCooldownActive} is true, the absorption is reduced by
     * {@link #STAGGER_REDUCTION} (i.e. only 60% of the nominal absorption remains).
     *
     * <p>A negative raw damage is returned unchanged; we never inflate incoming
     * damage via mitigation.
     */
    public static float damageAfterBlock(CombatStance stance,
                                         float rawDamage,
                                         boolean inCone,
                                         boolean shieldUp,
                                         boolean blockCooldownActive) {
        if (rawDamage <= 0f) {
            return rawDamage;
        }
        if (!shieldUp || !inCone) {
            return rawDamage;
        }
        float remaining = remainingFractionFor(stance);
        if (blockCooldownActive) {
            float absorbed = 1f - remaining;
            absorbed *= (1f - STAGGER_REDUCTION);
            remaining = 1f - absorbed;
        }
        return rawDamage * remaining;
    }
}
