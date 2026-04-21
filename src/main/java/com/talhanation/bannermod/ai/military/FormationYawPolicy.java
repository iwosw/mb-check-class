package com.talhanation.bannermod.ai.military;

/**
 * Pure helpers for Step 1.D formation-yaw clamping.
 *
 * <p>Kept framework-free so unit tests do not need a Minecraft runtime.
 */
public final class FormationYawPolicy {

    public static final float LINE_HOLD_YAW_DELTA_LIMIT_DEG = 10f;
    public static final float SHIELD_WALL_YAW_DELTA_LIMIT_DEG = 6f;

    private FormationYawPolicy() {
    }

    /**
     * Returns the maximum per-tick body-yaw delta (degrees) permitted for the stance.
     *
     * @return the limit, or {@link Float#NaN} if no clamp applies (LOOSE stance or null).
     */
    public static float perTickBodyYawLimitDegrees(CombatStance stance) {
        if (stance == null) {
            return Float.NaN;
        }
        switch (stance) {
            case LINE_HOLD:
                return LINE_HOLD_YAW_DELTA_LIMIT_DEG;
            case SHIELD_WALL:
                return SHIELD_WALL_YAW_DELTA_LIMIT_DEG;
            case LOOSE:
            default:
                return Float.NaN;
        }
    }

    /**
     * Clamp the body-yaw change from {@code from} to {@code to} by {@code limitDeg}.
     *
     * <p>Works on signed yaw values in degrees. Wraps the delta into the [-180, 180] range
     * so that e.g. 179 -&gt; -179 is treated as a 2-degree step, not a 358-degree one.
     */
    public static float clampBodyYaw(float from, float to, float limitDeg) {
        if (!(limitDeg > 0f) || Float.isNaN(limitDeg)) {
            return to;
        }
        float delta = wrapDegrees(to - from);
        if (delta > limitDeg) {
            delta = limitDeg;
        } else if (delta < -limitDeg) {
            delta = -limitDeg;
        }
        return from + delta;
    }

    /** Wrap an angle in degrees into the [-180, 180] range. */
    public static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360f;
        if (wrapped >= 180f) {
            wrapped -= 360f;
        }
        if (wrapped < -180f) {
            wrapped += 360f;
        }
        return wrapped;
    }
}
