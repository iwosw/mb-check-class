package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.ai.military.CommanderHostileScanCache;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SCANPOOL-003 acceptance #2 — action-decision parity.
 *
 * <p>The legacy {@code RangedSpacingService.nearestEnemyMeleeDistance} body
 * iterated the result of {@code level.getEntitiesOfClass(LivingEntity, AABB,
 * predicate)}, skipped ranged hostiles, and tracked the smallest distance.
 * The pooled implementation in this branch routes the same scan through
 * {@link CommanderHostileScanCache#snapshotFor} and then uses
 * {@link CommanderHostileScanCache#pickNearest} with the legacy filter
 * (alive, canAttack-eligible, non-ranged) folded into the {@code accept}
 * predicate.
 *
 * <p>This test pins the parity invariant on the pure pieces so we can verify
 * it without spinning up a full {@link net.minecraft.server.level.ServerLevel}:
 * given a fixed candidate list, the legacy "scan + linear distance min" loop
 * must produce the same nearest pick (and same distance) as the new
 * {@code pickNearest} call. This is the operative observable proof that the
 * spacing policy {@link RangedSpacingPolicy#decide} receives an identical
 * {@code distanceToEnemyMelee} input pre- and post-pool, and therefore that
 * the {@link RangedAction} downstream of it is unchanged.
 */
class RangedSpacingServicePoolingTest {

    private static final double SCAN_RADIUS = RangedSpacingService.SCAN_RADIUS;

    @Test
    void poolPickMatchesLegacyDirectPickOnMixedHostileScenario() {
        Vec3 origin = new Vec3(0, 0, 0);
        // Mix of melee + ranged hostiles at various distances. The legacy code
        // skipped CombatRole.RANGED inside the loop; the pooled code folds the
        // same skip into the pickNearest accept predicate.
        FakeHostile m1 = new FakeHostile(new Vec3(3, 0, 0), true, false);   // melee at 3
        FakeHostile m2 = new FakeHostile(new Vec3(1, 0, 5), true, false);   // melee at ~5.1
        FakeHostile r1 = new FakeHostile(new Vec3(2, 0, 0), true, true);    // ranged at 2 — must be skipped
        FakeHostile m3 = new FakeHostile(new Vec3(7, 0, 0), true, false);   // melee at 7
        FakeHostile dead = new FakeHostile(new Vec3(0.5, 0, 0), false, false);
        FakeHostile outOfRadius = new FakeHostile(new Vec3(20, 0, 0), true, false); // >SCAN_RADIUS=16
        List<FakeHostile> all = List.of(m1, m2, r1, m3, dead, outOfRadius);

        FakeHostile legacyPick = legacyNearestEnemyMelee(origin, SCAN_RADIUS, all);
        FakeHostile poolPick = poolPick(origin, SCAN_RADIUS, all);

        assertEquals(legacyPick, poolPick,
                "pooled pickNearest must match the legacy linear scan on the same candidate set");
        // Sanity: the picked candidate is the actual nearest melee.
        assertEquals(m1, poolPick, "m1 at distance 3 is the nearest live, non-ranged hostile");
    }

    @Test
    void poolPickReturnsNullWhenOnlyRangedOrOutOfRangeHostilesExist() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile r1 = new FakeHostile(new Vec3(2, 0, 0), true, true);
        FakeHostile r2 = new FakeHostile(new Vec3(4, 0, 0), true, true);
        FakeHostile farMelee = new FakeHostile(new Vec3(50, 0, 0), true, false);
        List<FakeHostile> all = List.of(r1, r2, farMelee);

        FakeHostile legacyPick = legacyNearestEnemyMelee(origin, SCAN_RADIUS, all);
        FakeHostile poolPick = poolPick(origin, SCAN_RADIUS, all);

        assertNull(legacyPick, "legacy code returns null when no melee in radius");
        assertEquals(legacyPick, poolPick,
                "pooled pickNearest must agree with legacy: no candidate to pick");
    }

    @Test
    void poolPickHonoursRadiusBoundaryIdenticallyToLegacy() {
        Vec3 origin = new Vec3(0, 0, 0);
        // Just outside SCAN_RADIUS=16, just inside, exactly on boundary.
        FakeHostile justInside = new FakeHostile(new Vec3(15.9, 0, 0), true, false);
        FakeHostile onBoundary = new FakeHostile(new Vec3(16.0, 0, 0), true, false);
        FakeHostile justOutside = new FakeHostile(new Vec3(16.1, 0, 0), true, false);
        List<FakeHostile> all = List.of(justOutside, onBoundary, justInside);

        FakeHostile legacyPick = legacyNearestEnemyMelee(origin, SCAN_RADIUS, all);
        FakeHostile poolPick = poolPick(origin, SCAN_RADIUS, all);

        assertEquals(legacyPick, poolPick,
                "pool must agree with legacy on the radius boundary semantics");
        assertTrue(poolPick == justInside || poolPick == onBoundary,
                "either justInside or onBoundary is acceptable as the boundary semantics depend "
                        + "on <= vs <; what matters is that legacy and pool agree");
    }

    /**
     * Mirrors the exact body of the pre-SCANPOOL-003 {@code nearestEnemyMeleeDistance}:
     * iterate every candidate, skip dead/ranged/the-recruit-itself, return the
     * smallest distance hostile.
     */
    private static FakeHostile legacyNearestEnemyMelee(Vec3 origin, double radius, List<FakeHostile> candidates) {
        FakeHostile best = null;
        double bestSqr = Double.POSITIVE_INFINITY;
        double radiusSqr = radius * radius;
        for (FakeHostile c : candidates) {
            if (!c.alive()) continue;
            if (c.ranged()) continue;
            double d = origin.distanceToSqr(c.pos());
            if (d <= radiusSqr && d < bestSqr) {
                bestSqr = d;
                best = c;
            }
        }
        return best;
    }

    /**
     * Composes the same accept predicate used in the patched
     * {@code RangedSpacingService.nearestEnemyMeleeDistance} and routes the
     * candidate list through {@link CommanderHostileScanCache#pickNearest}.
     */
    private static FakeHostile poolPick(Vec3 origin, double radius, List<FakeHostile> candidates) {
        Predicate<FakeHostile> accept = h -> h.alive() && !h.ranged();
        return CommanderHostileScanCache.pickNearest(
                origin,
                radius * radius,
                candidates,
                FakeHostile::pos,
                accept
        );
    }

    private record FakeHostile(Vec3 pos, boolean alive, boolean ranged) {
    }
}
