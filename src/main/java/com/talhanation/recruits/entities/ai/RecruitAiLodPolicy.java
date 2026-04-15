package com.talhanation.recruits.entities.ai;

import com.talhanation.recruits.config.RecruitsServerConfig;

public final class RecruitAiLodPolicy {
    public static final int DEFAULT_FULL_SEARCH_INTERVAL = 20;

    private RecruitAiLodPolicy() {
    }

    public static Evaluation evaluate(Context context, Settings settings) {
        LodTier tier = evaluateTier(context, settings);
        int searchInterval = searchInterval(tier, settings);
        boolean shouldRunSearch = Math.floorMod(context.tickCount() + context.tickOffset(), searchInterval) == 0;
        return new Evaluation(tier, searchInterval, shouldRunSearch);
    }

    public static LodTier evaluateTier(Context context, Settings settings) {
        if (!settings.enabled()) {
            return LodTier.FULL;
        }

        double fullDistanceSqr = settings.fullDistance() * settings.fullDistance();
        double reducedDistanceSqr = settings.reducedDistance() * settings.reducedDistance();

        if (context.recentlyHurt()
                || context.liveTargetDistanceSqr() <= fullDistanceSqr
                || context.nearestPlayerDistanceSqr() <= fullDistanceSqr) {
            return LodTier.FULL;
        }

        if (context.hasLiveTarget()
                || context.liveTargetDistanceSqr() <= reducedDistanceSqr
                || context.nearestPlayerDistanceSqr() <= reducedDistanceSqr) {
            return LodTier.REDUCED;
        }

        return LodTier.SHED;
    }

    public static int searchInterval(LodTier tier, Settings settings) {
        return switch (tier) {
            case FULL -> DEFAULT_FULL_SEARCH_INTERVAL;
            case REDUCED -> settings.reducedSearchInterval();
            case SHED -> settings.shedSearchInterval();
        };
    }

    public static Settings settingsFromConfig() {
        return new Settings(
                RecruitsServerConfig.EnableAiLod.get(),
                RecruitsServerConfig.AiLodFullDistance.get(),
                RecruitsServerConfig.AiLodReducedDistance.get(),
                RecruitsServerConfig.AiLodReducedTargetSearchInterval.get(),
                RecruitsServerConfig.AiLodShedTargetSearchInterval.get()
        );
    }

    public record Context(
            boolean recentlyHurt,
            boolean hasLiveTarget,
            double liveTargetDistanceSqr,
            double nearestPlayerDistanceSqr,
            int tickCount,
            int tickOffset
    ) {
    }

    public record Settings(
            boolean enabled,
            int fullDistance,
            int reducedDistance,
            int reducedSearchInterval,
            int shedSearchInterval
    ) {
        public Settings {
            if (fullDistance < 0) {
                throw new IllegalArgumentException("fullDistance must be non-negative");
            }
            if (reducedDistance < fullDistance) {
                throw new IllegalArgumentException("reducedDistance must be >= fullDistance");
            }
            if (reducedSearchInterval < DEFAULT_FULL_SEARCH_INTERVAL || reducedSearchInterval % DEFAULT_FULL_SEARCH_INTERVAL != 0) {
                throw new IllegalArgumentException("reducedSearchInterval must be a multiple of 20");
            }
            if (shedSearchInterval < reducedSearchInterval || shedSearchInterval % DEFAULT_FULL_SEARCH_INTERVAL != 0) {
                throw new IllegalArgumentException("shedSearchInterval must be a multiple of 20 and >= reducedSearchInterval");
            }
        }
    }

    public record Evaluation(LodTier tier, int searchInterval, boolean shouldRunSearch) {
    }

    public enum LodTier {
        FULL,
        REDUCED,
        SHED
    }
}
