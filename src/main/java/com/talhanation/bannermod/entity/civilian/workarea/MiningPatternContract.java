package com.talhanation.bannermod.entity.civilian.workarea;

public final class MiningPatternContract {

    public static final int DEFAULT_PATTERN_SEGMENTS = 16;

    private MiningPatternContract() {
    }

    public static int resolveTotalSegments(PatternMode mode, int depthSize) {
        if (mode == PatternMode.TUNNEL || mode == PatternMode.BRANCH) {
            return DEFAULT_PATTERN_SEGMENTS;
        }
        return Math.max(1, depthSize);
    }

    public static PatternApplication projectPatternSettings(MiningPatternSettings settings, int currentDepthSize) {
        PatternMode mode = switch (settings.mode()) {
            case CUSTOM -> PatternMode.CUSTOM;
            case MINE -> PatternMode.MINE;
            case BRANCH -> PatternMode.BRANCH;
            default -> PatternMode.TUNNEL;
        };
        return new PatternApplication(
                settings.width(),
                settings.height(),
                currentDepthSize,
                settings.heightOffset(),
                settings.closeFloor(),
                settings.branchSpacing(),
                settings.branchLength(),
                settings.descentStep(),
                mode
        );
    }

    public static PatternMode fromMiningMode(MiningArea.MiningMode mode) {
        return switch (mode) {
            case CUSTOM -> PatternMode.CUSTOM;
            case MINE -> PatternMode.MINE;
            case BRANCH -> PatternMode.BRANCH;
            default -> PatternMode.TUNNEL;
        };
    }

    public enum PatternMode {
        CUSTOM,
        MINE,
        TUNNEL,
        BRANCH
    }

    public record PatternApplication(
            int widthSize,
            int heightSize,
            int depthSize,
            int heightOffset,
            boolean closeFloor,
            int branchSpacing,
            int branchLength,
            int descentStep,
            PatternMode miningMode
    ) {
    }
}
