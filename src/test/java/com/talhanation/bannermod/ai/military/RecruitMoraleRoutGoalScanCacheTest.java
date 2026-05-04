package com.talhanation.bannermod.ai.military;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * SCANPOOL-002 acceptance — exercises the same {@link CommanderHostileScanCache}
 * call shape that {@link RecruitMoraleRoutGoal#tick()} now uses, with the rout
 * goal's own constants ({@link RecruitMoraleRoutGoal#FLEE_SCAN_RADIUS},
 * {@link RecruitMoraleRoutGoal#FLEE_SCAN_INTERVAL_TICKS}).
 *
 * <p>Two contracts are pinned here:
 * <ol>
 *   <li>50 routed recruits sharing one owner and one scan-tick bucket cause
 *       <strong>exactly one</strong> underlying world scan, not 50 (the
 *       per-recruit goal-tick AABB-allocation count drops to ≤ 1 per N ticks
 *       acceptance from the backlog task).</li>
 *   <li>Visible flee-target parity: on a fixed scenario the cache-routed pick
 *       is identical to the legacy direct-{@code getEntitiesOfClass} pick the
 *       goal used before SCANPOOL-002. This is the "visible flee behavior
 *       parity confirmed in a fixed scenario test" half of the acceptance.</li>
 * </ol>
 *
 * <p>JUnit chosen over a gametest for the parity check because the rout goal's
 * decision is purely a function of the cached candidate list and the
 * {@code canAttack} predicate — both observable without standing up a real
 * {@link net.minecraft.server.level.ServerLevel}. A live-recruit gametest
 * lives next to the SCANPOOL-001 acceptance test.
 */
class RecruitMoraleRoutGoalScanCacheTest {

    private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld")
    );

    @BeforeEach
    void resetCache() {
        CommanderHostileScanCache.resetForTesting();
    }

    @Test
    void fiftyRoutedRecruitsInOneGroupCauseAtMostOneScanPerInterval() {
        UUID owner = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        // 50 routed recruits — formation tightly packed under a routed shield
        // wall. Same owner, same tick-bucket → must collapse to one scan.
        for (int i = 0; i < 50; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD,
                    owner,
                    /*tick*/ 200,
                    RecruitMoraleRoutGoal.FLEE_SCAN_INTERVAL_TICKS,
                    new Vec3(i % 10, 0, i / 10),
                    RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS,
                    scanner
            );
        }

        assertEquals(1, scanner.calls(),
                "50 routed recruits sharing one owner and one bucket must produce "
                        + "exactly one underlying world scan");
        assertEquals(1L, CommanderHostileScanCache.scanCount(),
                "scanCount() must report the same single scan");
    }

    @Test
    void fiftyRoutedRecruitsCrossingBucketBoundaryCauseExactlyOneFreshScan() {
        UUID owner = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        for (int i = 0; i < 50; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, owner, 200, RecruitMoraleRoutGoal.FLEE_SCAN_INTERVAL_TICKS,
                    new Vec3(i % 10, 0, i / 10),
                    RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS, scanner
            );
        }
        assertEquals(1, scanner.calls(), "first bucket — exactly one scan");

        // Bump every recruit's effective tick past the interval boundary.
        for (int i = 0; i < 50; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, owner,
                    200 + RecruitMoraleRoutGoal.FLEE_SCAN_INTERVAL_TICKS,
                    RecruitMoraleRoutGoal.FLEE_SCAN_INTERVAL_TICKS,
                    new Vec3(i % 10, 0, i / 10),
                    RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS, scanner
            );
        }
        assertEquals(2, scanner.calls(),
                "next bucket — exactly one additional scan, not 50");
    }

    /**
     * Visible flee-behavior parity: the routed recruit picks the same nearest
     * hostile from the cached snapshot that the pre-pool {@code
     * level.getEntitiesOfClass} pick would have produced. The rout goal's
     * decision is deterministic in the candidate list + {@code canAttack}
     * predicate, so equivalence on a fixed scenario is sufficient evidence
     * that flee-target selection has not regressed.
     */
    @Test
    void cachedFleeTargetMatchesLegacyDirectScanOnFixedScenario() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile inFront = new FakeHostile(new Vec3(3, 0, 0), true, true);
        FakeHostile flank = new FakeHostile(new Vec3(0, 0, 6), true, true);
        FakeHostile distant = new FakeHostile(new Vec3(20, 0, 0), true, true);
        FakeHostile dead = new FakeHostile(new Vec3(1, 0, 0), false, true);
        FakeHostile friendly = new FakeHostile(new Vec3(2, 0, 0), true, false);
        List<FakeHostile> all = List.of(inFront, flank, distant, dead, friendly);
        double radius = RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS;

        FakeHostile poolPick = CommanderHostileScanCache.pickNearest(
                origin,
                radius * radius,
                all,
                FakeHostile::pos,
                h -> h != null && h.alive() && h.attackable()
        );
        FakeHostile legacyPick = legacyRoutNearestHostile(origin, radius, all);

        assertNotNull(poolPick, "in-radius live attackable hostile must be picked");
        assertSame(legacyPick, poolPick,
                "cached flee-target pick must match the pre-pool direct-scan pick");
        assertSame(inFront, poolPick,
                "nearest live attackable in radius is the closest in-front hostile");
    }

    @Test
    void emptyCandidateListYieldsNoFleeTarget() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile picked = CommanderHostileScanCache.pickNearest(
                origin,
                RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS * RecruitMoraleRoutGoal.FLEE_SCAN_RADIUS,
                List.of(),
                FakeHostile::pos,
                h -> h != null && h.alive() && h.attackable()
        );
        assertNull(picked,
                "no candidates → no flee target; rout goal then walks the recruit forward");
    }

    /**
     * Mirrors the pre-SCANPOOL-002 RecruitMoraleRoutGoal.nearestHostile() body:
     * iterate all candidates, drop self / dead / non-attackable, return the
     * minimum distance within radius.
     */
    private static FakeHostile legacyRoutNearestHostile(Vec3 origin, double radius, List<FakeHostile> candidates) {
        FakeHostile best = null;
        double bestSqr = Double.POSITIVE_INFINITY;
        double radiusSqr = radius * radius;
        for (FakeHostile c : candidates) {
            if (!c.alive() || !c.attackable()) continue;
            double d = origin.distanceToSqr(c.pos());
            if (d <= radiusSqr && d < bestSqr) {
                bestSqr = d;
                best = c;
            }
        }
        return best;
    }

    /**
     * {@code attackable} models {@link com.talhanation.bannermod.entity.military.AbstractRecruitEntity#canAttack}
     * for the purposes of the parity check; {@code alive} models
     * {@link LivingEntity#isAlive}.
     */
    private record FakeHostile(Vec3 pos, boolean alive, boolean attackable) {
    }

    private static final class CountingScanner implements Function<AABB, List<LivingEntity>> {
        private final List<LivingEntity> source;
        private final AtomicInteger calls = new AtomicInteger();

        CountingScanner(List<LivingEntity> source) {
            this.source = source;
        }

        @Override
        public List<LivingEntity> apply(AABB aabb) {
            calls.incrementAndGet();
            List<LivingEntity> out = new ArrayList<>();
            for (LivingEntity entity : source) {
                if (aabb.contains(entity.getX(), entity.getY(), entity.getZ())) {
                    out.add(entity);
                }
            }
            return out;
        }

        int calls() {
            return calls.get();
        }
    }
}
