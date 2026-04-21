package com.talhanation.bannermod.ai.military;

/**
 * Stage 4.A: damage multiplier for facing-based hit vulnerability.
 *
 * <p>Pure helper: given a {@link FacingHitZone}, return the multiplier applied to
 * incoming damage after shield mitigation has already been considered. A hit to
 * the back is ×1.5, a hit to the flanks is ×1.15, and a hit to the front is ×1.0
 * (the shield cone does the work there).
 */
public final class FlankDamage {

    public static final float FRONT_MULTIPLIER = 1.0f;
    public static final float SIDE_MULTIPLIER = 1.15f;
    public static final float BACK_MULTIPLIER = 1.5f;

    private FlankDamage() {
    }

    public static float multiplierFor(FacingHitZone zone) {
        if (zone == null) {
            return FRONT_MULTIPLIER;
        }
        switch (zone) {
            case BACK:
                return BACK_MULTIPLIER;
            case SIDE:
                return SIDE_MULTIPLIER;
            case FRONT:
            default:
                return FRONT_MULTIPLIER;
        }
    }
}
