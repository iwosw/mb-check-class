package com.talhanation.bannermod.ai.military;

/**
 * Stage 4.D: HYW-parity unit-type damage counters.
 *
 * <p>BannerMod doesn't tag recruits with explicit unit classes, so we infer the
 * class from (a) whether the recruit is mounted, (b) the held weapon category,
 * and (c) whether it's a shieldman / wearing plate-tier chest armor.
 *
 * <p>The multiplier matrix is sparse; any matchup not listed returns
 * {@code 1.0} (no counter).
 *
 * <p>Pure helper: integration callers resolve world state into the
 * {@link UnitClass} inputs.
 */
public final class UnitTypeMatchup {

    /** Armor defense threshold (chest slot) that flags "heavy" plate-tier wearer. */
    public static final int HEAVY_PLATE_DEFENSE_THRESHOLD = 5;
    /** Extra-reach threshold above which the holder counts as a pike/anti-cav unit. */
    public static final double PIKE_REACH_THRESHOLD = 1.0D;

    public enum UnitClass {
        LIGHT,
        HEAVY,
        RANGED,
        CAVALRY,
        PIKE_INFANTRY;
    }

    private UnitTypeMatchup() {
    }

    /**
     * Classify a recruit from flags extracted upstream.
     *
     * <p>Precedence (highest priority first):
     * <ol>
     *   <li>Mounted → {@link UnitClass#CAVALRY}.</li>
     *   <li>Held weapon grants {@code >= PIKE_REACH_THRESHOLD} extra reach →
     *       {@link UnitClass#PIKE_INFANTRY}.</li>
     *   <li>Projectile weapon (bow / crossbow) → {@link UnitClass#RANGED}.</li>
     *   <li>Shieldman or plate-tier chest armor → {@link UnitClass#HEAVY}.</li>
     *   <li>Otherwise → {@link UnitClass#LIGHT}.</li>
     * </ol>
     *
     * @param mounted          recruit currently has a LivingEntity vehicle
     * @param extraReachBlocks main-hand weapon's extra reach (see {@link WeaponReach})
     * @param rangedWeapon     main-hand is a bow / crossbow / other projectile weapon
     * @param shieldman        recruit is a {@code RecruitShieldmanEntity}
     * @param chestDefense     defense value of equipped chest armor (0 if none)
     */
    public static UnitClass classify(boolean mounted,
                                     double extraReachBlocks,
                                     boolean rangedWeapon,
                                     boolean shieldman,
                                     int chestDefense) {
        if (mounted) {
            return UnitClass.CAVALRY;
        }
        if (extraReachBlocks >= PIKE_REACH_THRESHOLD) {
            return UnitClass.PIKE_INFANTRY;
        }
        if (rangedWeapon) {
            return UnitClass.RANGED;
        }
        if (shieldman || chestDefense >= HEAVY_PLATE_DEFENSE_THRESHOLD) {
            return UnitClass.HEAVY;
        }
        return UnitClass.LIGHT;
    }

    /**
     * Outgoing-damage multiplier for an attacker of {@code attacker} class hitting a
     * defender of {@code defender} class. Any matchup not listed returns {@code 1.0}.
     *
     * <p>Matrix (HYW parity, simplified):
     * <pre>
     *   LIGHT vs HEAVY                      = 0.80  (light weapons bounce off armour)
     *   HEAVY vs LIGHT                      = 1.20
     *   CAVALRY vs LIGHT or RANGED          = 1.40  (charge crushes foot)
     *   LIGHT or RANGED (foot) vs CAVALRY   = 0.90  (harder to hit mounted)
     *   PIKE_INFANTRY vs CAVALRY            = 1.50  (anti-cav pike)
     *   all others                          = 1.00
     * </pre>
     */
    public static double damageMultiplier(UnitClass attacker, UnitClass defender) {
        if (attacker == null || defender == null) {
            return 1.0D;
        }
        switch (attacker) {
            case LIGHT:
                if (defender == UnitClass.HEAVY) return 0.80D;
                if (defender == UnitClass.CAVALRY) return 0.90D;
                return 1.0D;
            case HEAVY:
                if (defender == UnitClass.LIGHT) return 1.20D;
                return 1.0D;
            case CAVALRY:
                if (defender == UnitClass.LIGHT || defender == UnitClass.RANGED) return 1.40D;
                return 1.0D;
            case RANGED:
                if (defender == UnitClass.CAVALRY) return 0.90D;
                return 1.0D;
            case PIKE_INFANTRY:
                if (defender == UnitClass.CAVALRY) return 1.50D;
                return 1.0D;
            default:
                return 1.0D;
        }
    }
}
