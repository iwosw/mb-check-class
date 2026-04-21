package com.talhanation.bannermod.ai.military;

/**
 * Stage 4.A: classification of incoming hits by direction relative to body yaw.
 *
 * <p>Pure helper: no Minecraft types referenced. The zones are:
 * <ul>
 *   <li>{@link #FRONT} — within the frontal shield cone
 *       ({@code <= ShieldBlockGeometry.FRONT_CONE_HALF_DEG}, default 60deg half-angle,
 *       so a 120deg frontal arc).</li>
 *   <li>{@link #BACK}  — the rear 90deg arc ({@code > 135deg} off-axis).</li>
 *   <li>{@link #SIDE}  — everything between: flanks that are neither blockable by the
 *       frontal shield nor a full back-stab.</li>
 * </ul>
 */
public enum FacingHitZone {
    FRONT,
    SIDE,
    BACK;

    /** Half-angle (degrees) of the rear arc. Hits past this count as back-stabs. */
    public static final float BACK_ARC_HALF_DEG = 45f;

    /**
     * Classify the relative direction of an attacker at {@code (fromX, fromZ)} acting on a
     * defender at {@code (toX, toZ)} with body yaw {@code bodyYawDeg}.
     *
     * <p>Attacker is at {@code (fromX, fromZ)}, defender is at {@code (toX, toZ)}. The
     * classification is computed from the vector defender→attacker (i.e. the direction the
     * blow is coming FROM). Coincident points are treated as {@link #FRONT} so the default
     * is the safest (no flank multiplier).
     */
    public static FacingHitZone classify(float bodyYawDeg,
                                         double fromX, double fromZ,
                                         double toX, double toZ) {
        double dx = fromX - toX;
        double dz = fromZ - toZ;
        double distSqr = dx * dx + dz * dz;
        if (distSqr < 1.0e-6) {
            return FRONT;
        }

        double yawRad = Math.toRadians(bodyYawDeg);
        double facingX = -Math.sin(yawRad);
        double facingZ = Math.cos(yawRad);

        double dist = Math.sqrt(distSqr);
        double cosAngle = (facingX * dx + facingZ * dz) / dist;
        if (cosAngle > 1.0) cosAngle = 1.0;
        if (cosAngle < -1.0) cosAngle = -1.0;

        double cosFrontHalf = Math.cos(Math.toRadians(ShieldBlockGeometry.FRONT_CONE_HALF_DEG));
        double cosBackHalf = Math.cos(Math.toRadians(180.0 - BACK_ARC_HALF_DEG));

        if (cosAngle >= cosFrontHalf) {
            return FRONT;
        }
        if (cosAngle <= cosBackHalf) {
            return BACK;
        }
        return SIDE;
    }
}
