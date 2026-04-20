package com.talhanation.bannermod.settlement.prefab.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchitectureTierTest {

    @Test
    void scoreZeroMapsToHovel() {
        assertEquals(ArchitectureTier.HOVEL, ArchitectureTier.fromScore(0));
        assertEquals(ArchitectureTier.HOVEL, ArchitectureTier.fromScore(34));
    }

    @Test
    void score35MapsToAcceptable() {
        assertEquals(ArchitectureTier.ACCEPTABLE, ArchitectureTier.fromScore(35));
        assertEquals(ArchitectureTier.ACCEPTABLE, ArchitectureTier.fromScore(54));
    }

    @Test
    void score55MapsToGood() {
        assertEquals(ArchitectureTier.GOOD, ArchitectureTier.fromScore(55));
        assertEquals(ArchitectureTier.GOOD, ArchitectureTier.fromScore(74));
    }

    @Test
    void score75MapsToGreat() {
        assertEquals(ArchitectureTier.GREAT, ArchitectureTier.fromScore(75));
        assertEquals(ArchitectureTier.GREAT, ArchitectureTier.fromScore(89));
    }

    @Test
    void score90MapsToMajestic() {
        assertEquals(ArchitectureTier.MAJESTIC, ArchitectureTier.fromScore(90));
        assertEquals(ArchitectureTier.MAJESTIC, ArchitectureTier.fromScore(100));
    }
}
