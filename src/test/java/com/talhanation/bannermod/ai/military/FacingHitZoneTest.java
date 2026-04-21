package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Stage 4.A: facing-based hit zone classification.
 *
 * <p>Minecraft body-yaw convention: yaw=0 faces +Z (south); yaw=90 faces -X (west).
 * The classify() helper expects attacker at (fromX,fromZ), defender at (toX,toZ).
 */
class FacingHitZoneTest {

    @Test
    void attackerDirectlyInFrontIsFront() {
        // Defender at origin facing +Z, attacker 5 blocks south (+Z).
        assertEquals(FacingHitZone.FRONT, FacingHitZone.classify(0f, 0, 5, 0, 0));
    }

    @Test
    void attackerDirectlyBehindIsBack() {
        // Defender at origin facing +Z, attacker 5 blocks north (-Z).
        assertEquals(FacingHitZone.BACK, FacingHitZone.classify(0f, 0, -5, 0, 0));
    }

    @Test
    void attackerDueWestIsSide() {
        // Defender at origin facing +Z, attacker 5 blocks west (-X). 90deg off-axis.
        assertEquals(FacingHitZone.SIDE, FacingHitZone.classify(0f, -5, 0, 0, 0));
    }

    @Test
    void attackerDueEastIsSide() {
        assertEquals(FacingHitZone.SIDE, FacingHitZone.classify(0f, 5, 0, 0, 0));
    }

    @Test
    void attackerJustInsideFrontConeIsFront() {
        // 59deg off-axis (just under the 60deg front cone half-angle).
        double angleRad = Math.toRadians(59.0);
        double fromX = Math.sin(angleRad) * 5;
        double fromZ = Math.cos(angleRad) * 5;
        assertEquals(FacingHitZone.FRONT, FacingHitZone.classify(0f, fromX, fromZ, 0, 0));
    }

    @Test
    void attackerJustOutsideFrontConeIsSide() {
        // 61deg off-axis — outside front cone, not yet in back arc.
        double angleRad = Math.toRadians(61.0);
        double fromX = Math.sin(angleRad) * 5;
        double fromZ = Math.cos(angleRad) * 5;
        assertEquals(FacingHitZone.SIDE, FacingHitZone.classify(0f, fromX, fromZ, 0, 0));
    }

    @Test
    void attackerInsideBackArcIsBack() {
        // 140deg off-axis — inside the rear 90deg arc (past 135deg).
        double angleRad = Math.toRadians(140.0);
        double fromX = Math.sin(angleRad) * 5;
        double fromZ = Math.cos(angleRad) * 5;
        assertEquals(FacingHitZone.BACK, FacingHitZone.classify(0f, fromX, fromZ, 0, 0));
    }

    @Test
    void attackerAtBackArcEdgeIsSide() {
        // 130deg off-axis — still inside the SIDE arc (not yet back).
        double angleRad = Math.toRadians(130.0);
        double fromX = Math.sin(angleRad) * 5;
        double fromZ = Math.cos(angleRad) * 5;
        assertEquals(FacingHitZone.SIDE, FacingHitZone.classify(0f, fromX, fromZ, 0, 0));
    }

    @Test
    void rotatedBodyYawShiftsZones() {
        // Defender facing -X (yaw=90): attacker due west is now FRONT.
        assertEquals(FacingHitZone.FRONT, FacingHitZone.classify(90f, -5, 0, 0, 0));
        // Attacker due east is now BACK.
        assertEquals(FacingHitZone.BACK, FacingHitZone.classify(90f, 5, 0, 0, 0));
        // Attacker due south is now SIDE.
        assertEquals(FacingHitZone.SIDE, FacingHitZone.classify(90f, 0, 5, 0, 0));
    }

    @Test
    void coincidentPointsDefaultToFront() {
        assertEquals(FacingHitZone.FRONT, FacingHitZone.classify(0f, 0, 0, 0, 0));
    }
}
