package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FarmerPlantingPreparationTest {

    @Test
    void prefersConfiguredSeedOverInventorySeed() {
        assertEquals(FarmerPlantingPreparation.SeedSource.CONFIGURED, FarmerPlantingPreparation.resolveSeedSource(true, true));
    }

    @Test
    void usesInventorySeedWhenAreaIsUnconfigured() {
        assertEquals(FarmerPlantingPreparation.SeedSource.INVENTORY, FarmerPlantingPreparation.resolveSeedSource(false, true));
    }

    @Test
    void marksPlantingBlockedWhenNoSeedExists() {
        assertEquals(FarmerPlantingPreparation.SeedSource.MISSING, FarmerPlantingPreparation.resolveSeedSource(false, false));
    }
}
