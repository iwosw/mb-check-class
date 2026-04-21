package com.talhanation.bannermod.ai.military;

/**
 * Stage 2 pure helpers: directional-block geometry for shield mitigation.
 *
 * <p>Kept framework-free so unit tests do not need a Minecraft runtime.
 */
public final class ShieldBlockGeometry {

    /** Default half-angle (degrees) of the frontal shield-block cone. */
    public static final float FRONT_CONE_HALF_DEG = 60f;

    private ShieldBlockGeometry() {
    }

    /**
     * Returns true if the world-space point {@code (toX, toZ)} lies within the frontal
     * cone of the entity located at {@code (fromX, fromZ)} with body-yaw
     * {@code bodyYawDeg} and half-angle {@code halfAngleDeg}.
     *
     * <p>Minecraft body-yaw convention: 0deg faces +Z (south), yaw increases clockwise
     * when viewed from above so 90deg faces -X (west).
     *
     * <p>If the attacker sits on top of the defender (zero separation), this function
     * returns {@code true} (the cone check is degenerate so we default to "in cone").
     */
    public static boolean isInFrontCone(float bodyYawDeg,
                                        double fromX, double fromZ,
                                        double toX, double toZ,
                                        float halfAngleDeg) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        double distSqr = dx * dx + dz * dz;
        if (distSqr < 1.0e-6) {
            return true;
        }

        // Minecraft yaw: facing vector is (-sin(yaw), +cos(yaw)) for yaw measured in
        // degrees, then converted to radians.
        double yawRad = Math.toRadians(bodyYawDeg);
        double facingX = -Math.sin(yawRad);
        double facingZ = Math.cos(yawRad);

        double dist = Math.sqrt(distSqr);
        double cosAngle = (facingX * dx + facingZ * dz) / dist;
        if (cosAngle > 1.0) cosAngle = 1.0;
        if (cosAngle < -1.0) cosAngle = -1.0;

        double cosHalf = Math.cos(Math.toRadians(halfAngleDeg));
        return cosAngle >= cosHalf;
    }

    /**
     * Returns the signed body-yaw (degrees) that would make the entity at
     * {@code (fromX, fromZ)} face the point {@code (toX, toZ)}.
     *
     * <p>If the points coincide, returns {@code currentYawDeg} unchanged.
     */
    public static float yawToward(float currentYawDeg,
                                  double fromX, double fromZ,
                                  double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        if (dx * dx + dz * dz < 1.0e-6) {
            return currentYawDeg;
        }
        // Inverse of facingX = -sin(yaw), facingZ = cos(yaw).
        double yawRad = Math.atan2(-dx, dz);
        return (float) Math.toDegrees(yawRad);
    }
}
