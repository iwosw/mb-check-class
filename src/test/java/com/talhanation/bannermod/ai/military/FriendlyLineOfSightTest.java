package com.talhanation.bannermod.ai.military;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage 3.B: friendly-allowed LOS — raycasts injected via a small functional
 * interface so we never need a Minecraft runtime.
 */
class FriendlyLineOfSightTest {

    /** Tiny standin for an entity so we don't pull the Minecraft entity class in. */
    private record Dummy(String name, boolean ally) {
    }

    private static final Predicate<Dummy> IS_ALLY = d -> d != null && d.ally();

    @Test
    void clearBlockPathAndNoEntitiesReturnsTrue() {
        FriendlyLineOfSight.SightProbe<Dummy> probe =
                new FriendlyLineOfSight.SightProbe<>(false, List.of());
        assertTrue(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }

    @Test
    void blockColumnInTheWayReturnsFalseEvenIfAlliesInFront() {
        FriendlyLineOfSight.SightProbe<Dummy> probe =
                new FriendlyLineOfSight.SightProbe<>(true, List.of(new Dummy("rank1", true)));
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }

    @Test
    void alliesInPathAreTransparent() {
        FriendlyLineOfSight.SightProbe<Dummy> probe = new FriendlyLineOfSight.SightProbe<>(
                false,
                List.of(new Dummy("rank1_a", true), new Dummy("rank1_b", true)));
        assertTrue(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }

    @Test
    void nonAlliedEntityInPathBreaksLineOfSight() {
        FriendlyLineOfSight.SightProbe<Dummy> probe = new FriendlyLineOfSight.SightProbe<>(
                false,
                List.of(new Dummy("ally", true), new Dummy("enemy", false)));
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }

    @Test
    void firstNonAllyShortCircuitsEvenIfLaterEntitiesAreAllies() {
        FriendlyLineOfSight.SightProbe<Dummy> probe = new FriendlyLineOfSight.SightProbe<>(
                false,
                List.of(new Dummy("enemy", false), new Dummy("ally", true)));
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }

    @Test
    void nullProbeReturnsFalse() {
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(() -> null, IS_ALLY));
    }

    @Test
    void nullInputsReturnFalse() {
        FriendlyLineOfSight.SightProbe<Dummy> probe =
                new FriendlyLineOfSight.SightProbe<>(false, List.of());
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(null, IS_ALLY));
        assertFalse(FriendlyLineOfSight.canReachThroughAllies(() -> probe, null));
    }

    @Test
    void nullEntitiesListIsTreatedAsEmpty() {
        FriendlyLineOfSight.SightProbe<Dummy> probe =
                new FriendlyLineOfSight.SightProbe<>(false, null);
        assertTrue(FriendlyLineOfSight.canReachThroughAllies(() -> probe, IS_ALLY));
    }
}
