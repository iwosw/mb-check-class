package com.talhanation.bannermod.util;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for FORMATIONDIM-001's cross-dimension safety helper.
 *
 * <p>The dim-check is a static helper so it can be exercised without spinning up
 * a Minecraft level: the project does not have Mockito on the classpath, so we
 * drive the helper through its {@link Level}-free overload that takes
 * {@code ResourceKey<Level>} arguments directly.
 */
class FormationDimensionGuardTest {

    @BeforeEach
    void resetCounters() {
        RuntimeProfilingCounters.reset();
    }

    @Test
    void identicalDimensionsAreNotMismatched() {
        assertFalse(FormationDimensionGuard.dimensionMismatch(Level.OVERWORLD, Level.OVERWORLD),
                "Same dimension keys must not register as mismatched");
        assertFalse(FormationDimensionGuard.dimensionMismatch(Level.NETHER, Level.NETHER),
                "Same dimension keys must not register as mismatched");
    }

    @Test
    void crossDimensionPairsAreMismatched() {
        assertTrue(FormationDimensionGuard.dimensionMismatch(Level.OVERWORLD, Level.NETHER),
                "Overworld recruit + Nether leader must register as mismatched");
        assertTrue(FormationDimensionGuard.dimensionMismatch(Level.OVERWORLD, Level.END),
                "Overworld recruit + End leader must register as mismatched");
        assertTrue(FormationDimensionGuard.dimensionMismatch(Level.NETHER, Level.END),
                "Nether recruit + End leader must register as mismatched");
    }

    @Test
    void nullKeysAreTreatedAsMismatched() {
        assertTrue(FormationDimensionGuard.dimensionMismatch(null, Level.OVERWORLD),
                "Null recruit dimension must be treated as mismatched (cannot prove safe follow)");
        assertTrue(FormationDimensionGuard.dimensionMismatch(Level.OVERWORLD, null),
                "Null leader dimension must be treated as mismatched (cannot prove safe follow)");
        assertTrue(FormationDimensionGuard.dimensionMismatch(null, null),
                "Both-null dimensions must be treated as mismatched");
    }

    @Test
    void nullLevelsAreTreatedAsMismatched() {
        assertTrue(FormationDimensionGuard.leaderInDifferentDimension(null, null),
                "Null levels degrade to the safe (mismatched) result");
    }

    @Test
    void recordOrphanedGroupBumpsCounterByGroupSize() {
        FormationDimensionGuard.recordOrphanedGroup(7);
        Long observed = RuntimeProfilingCounters.snapshot().get(FormationDimensionGuard.COUNTER_KEY);
        assertEquals(7L, observed, "Group orphan event must add the cohort size to the counter");
    }

    @Test
    void recordOrphanedGroupIgnoresNonPositiveSizes() {
        FormationDimensionGuard.recordOrphanedGroup(0);
        FormationDimensionGuard.recordOrphanedGroup(-3);
        Long observed = RuntimeProfilingCounters.snapshot().get(FormationDimensionGuard.COUNTER_KEY);
        assertNull(observed, "Non-positive group sizes must not register an orphan event");
    }

    @Test
    void shouldHoldAccumulatesCounterPerCheck() {
        // Six recruits in the overworld whose leader is now in the nether.
        // dimension-check should fire six times and the counter advance by six.
        for (int i = 0; i < 6; i++) {
            assertTrue(FormationDimensionGuard.shouldHoldDueToDimensionMismatch(
                    (net.minecraft.world.entity.Entity) null,
                    null
            ), "Null leader/recruit must still report a hold-required mismatch");
        }
        Long observed = RuntimeProfilingCounters.snapshot().get(FormationDimensionGuard.COUNTER_KEY);
        assertEquals(6L, observed, "Per-recruit dim-check failures must each increment the counter");
    }
}
