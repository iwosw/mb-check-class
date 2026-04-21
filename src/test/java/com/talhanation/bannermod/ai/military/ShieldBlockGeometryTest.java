package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 2.A: directional shield-block geometry.
 *
 * <p>Minecraft body-yaw convention: yaw=0 faces +Z (south); yaw increases clockwise
 * from above so yaw=90 faces -X (west) and yaw=-90 faces +X (east).
 */
class ShieldBlockGeometryTest {

    private static final float HALF = ShieldBlockGeometry.FRONT_CONE_HALF_DEG;
    private static final float EPS = 1e-3f;

    @Test
    void attackerDirectlyInFrontIsInCone() {
        // Recruit at origin facing +Z (yaw=0), attacker 5 blocks south.
        assertTrue(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, 0, 5, HALF));
    }

    @Test
    void attackerDirectlyBehindIsOutOfCone() {
        // Recruit facing +Z, attacker 5 blocks north.
        assertFalse(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, 0, -5, HALF));
    }

    @Test
    void attackerToFlankIsOutOfCone() {
        // Recruit facing +Z, attacker 5 blocks west -> -X -> outside 120-degree front cone.
        assertFalse(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, -5, 0, HALF));
        assertFalse(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, 5, 0, HALF));
    }

    @Test
    void attackerAtConeEdgeIsInCone() {
        // At exactly the cone edge (60deg off-axis), should still count as inside.
        double angleRad = Math.toRadians(HALF - 0.5);
        double tx = Math.sin(angleRad) * 5;
        double tz = Math.cos(angleRad) * 5;
        assertTrue(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, -tx, tz, HALF));
    }

    @Test
    void rotatedRecruitFacesAttackerCorrectly() {
        // Recruit at origin facing -X (yaw=90); attacker 5 blocks west.
        assertTrue(ShieldBlockGeometry.isInFrontCone(90f, 0, 0, -5, 0, HALF));
        // And an attacker to the south is now flanking -> outside cone.
        assertFalse(ShieldBlockGeometry.isInFrontCone(90f, 0, 0, 0, 5, HALF));
    }

    @Test
    void coincidentAttackerDefaultsToInCone() {
        // Degenerate case: no separation. Default to in-cone so the code paths are safe.
        assertTrue(ShieldBlockGeometry.isInFrontCone(0f, 0, 0, 0, 0, HALF));
    }

    @Test
    void yawTowardProducesZeroForDirectlySouthTarget() {
        // Target directly to the +Z side -> yaw=0.
        float yaw = ShieldBlockGeometry.yawToward(45f, 0, 0, 0, 5);
        assertEquals(0f, yaw, EPS);
    }

    @Test
    void yawTowardProducesNinetyForDirectlyWestTarget() {
        // Target at -X -> yaw=90 (Minecraft convention).
        float yaw = ShieldBlockGeometry.yawToward(0f, 0, 0, -5, 0);
        assertEquals(90f, yaw, EPS);
    }

    @Test
    void yawTowardReturnsCurrentForCoincidentPoint() {
        float yaw = ShieldBlockGeometry.yawToward(123f, 1, 1, 1, 1);
        assertEquals(123f, yaw, EPS);
    }
}
