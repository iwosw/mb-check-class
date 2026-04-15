package com.talhanation.bannermod.ai.military;

/**
 * Level-of-detail policy for recruit target-search cadence.
 *
 * <p>Reconstructed during Phase 21 Wave 9 source consolidation: the original implementation
 * existed only as compiled bytecode under {@code recruits/bin/main/} and was never tracked
 * in the recruits clone source tree. The contract here is derived from the surviving
 * {@code RecruitAiLodPolicyTest} cases (close-or-recently-engaged → FULL, distant live
 * target → REDUCED, fully uninvolved → SHED, deterministic per-tier cadence, disabled
 * policy falls back to FULL).</p>
 *
 * <p>The policy is pure: callers pass an immutable {@link Context} plus {@link Settings}
 * and receive an {@link Evaluation}. No world or entity state is held here.</p>
 */
public final class RecruitAiLodPolicy {

    /** Default cadence for FULL-tier target search, in ticks. */
    public static final int DEFAULT_FULL_SEARCH_INTERVAL = 20;

    private RecruitAiLodPolicy() {
    }

    public enum LodTier {
        FULL,
        REDUCED,
        SHED
    }

    /**
     * Tunable thresholds for the LOD policy.
     *
     * @param enabled                whether LOD logic runs at all; when {@code false}
     *                               every recruit stays on FULL cadence.
     * @param playerProximity        radius (in blocks) within which a nearby player keeps
     *                               the recruit on FULL cadence; compared as squared
     *                               distance against {@link Context#nearestPlayerDistanceSqr()}.
     * @param reducedDistance        radius (in blocks) inside which a live target keeps
     *                               the recruit on REDUCED cadence.
     * @param reducedSearchInterval  search cadence (in ticks) for REDUCED tier.
     * @param shedSearchInterval     search cadence (in ticks) for SHED tier.
     */
    public record Settings(boolean enabled,
                           int playerProximity,
                           int reducedDistance,
                           int reducedSearchInterval,
                           int shedSearchInterval) {
    }

    /**
     * Per-recruit per-tick evaluation context.
     *
     * @param recentlyDamaged          {@code true} when the recruit has been hurt very recently
     *                                 (caller decides the window; typically {@code hurtTime > 0}).
     * @param hasLiveTarget            whether the recruit currently has a live, valid target.
     * @param liveTargetDistanceSqr    squared distance to the live target, or
     *                                 {@link Double#POSITIVE_INFINITY} when none.
     * @param nearestPlayerDistanceSqr squared distance to the closest player within the
     *                                 caller's relevance radius, or
     *                                 {@link Double#POSITIVE_INFINITY} when none.
     * @param tickCount                recruit's current tick count.
     * @param tickOffset               per-recruit phase offset to spread cadence across
     *                                 the tick wheel.
     */
    public record Context(boolean recentlyDamaged,
                          boolean hasLiveTarget,
                          double liveTargetDistanceSqr,
                          double nearestPlayerDistanceSqr,
                          int tickCount,
                          int tickOffset) {
    }

    /** Result of evaluating a {@link Context} against a {@link Settings}. */
    public record Evaluation(LodTier tier, int searchInterval, boolean shouldRunSearch) {
    }

    /**
     * Evaluate the LOD tier and cadence for the supplied context and settings.
     *
     * <p>Tier selection (in order):</p>
     * <ol>
     *   <li>If {@code !settings.enabled()} → FULL.</li>
     *   <li>If {@code recentlyDamaged} → FULL.</li>
     *   <li>If a player is within {@code playerProximity} → FULL.</li>
     *   <li>If a live target is within {@code reducedDistance} → REDUCED.</li>
     *   <li>Otherwise → SHED.</li>
     * </ol>
     *
     * <p>Cadence: FULL uses {@link #DEFAULT_FULL_SEARCH_INTERVAL},
     * REDUCED uses {@code settings.reducedSearchInterval()},
     * SHED uses {@code settings.shedSearchInterval()}.
     * {@code shouldRunSearch} is true iff {@code (tickCount + tickOffset) % interval == 0}.</p>
     */
    public static Evaluation evaluate(Context context, Settings settings) {
        LodTier tier;
        int interval;

        if (!settings.enabled()) {
            tier = LodTier.FULL;
            interval = DEFAULT_FULL_SEARCH_INTERVAL;
        } else {
            double playerThresholdSqr = (double) settings.playerProximity() * settings.playerProximity();
            double reducedThresholdSqr = (double) settings.reducedDistance() * settings.reducedDistance();

            boolean playerClose = context.nearestPlayerDistanceSqr() <= playerThresholdSqr;
            boolean liveTargetInRange = context.hasLiveTarget()
                    && context.liveTargetDistanceSqr() <= reducedThresholdSqr;

            if (context.recentlyDamaged() || playerClose) {
                tier = LodTier.FULL;
                interval = DEFAULT_FULL_SEARCH_INTERVAL;
            } else if (liveTargetInRange) {
                tier = LodTier.REDUCED;
                interval = settings.reducedSearchInterval();
            } else {
                tier = LodTier.SHED;
                interval = settings.shedSearchInterval();
            }
        }

        if (interval <= 0) {
            interval = DEFAULT_FULL_SEARCH_INTERVAL;
        }

        boolean shouldRun = ((context.tickCount() + context.tickOffset()) % interval) == 0;
        return new Evaluation(tier, interval, shouldRun);
    }

    /**
     * Default settings used when no config-driven override is wired in.
     *
     * <p>The conservative defaults keep recruits on FULL cadence for any player within 16
     * blocks, drop to REDUCED (every 40 ticks) for distant live combat, and fully shed to
     * an 80-tick cadence when uninvolved. These match the test fixture.</p>
     */
    public static Settings settingsFromConfig() {
        return new Settings(true, 16, 40, 40, 80);
    }
}
