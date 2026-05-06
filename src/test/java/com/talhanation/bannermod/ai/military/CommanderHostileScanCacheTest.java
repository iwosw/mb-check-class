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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives {@link CommanderHostileScanCache} through its testing entry points
 * and pure helpers so the per-group dedup contract — the operative behaviour
 * SCANPOOL-001 was opened to deliver — can be verified without a live
 * {@link net.minecraft.server.level.ServerLevel}.
 */
class CommanderHostileScanCacheTest {

    private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld")
    );

    @BeforeEach
    void resetCache() {
        CommanderHostileScanCache.resetForTesting();
    }

    @Test
    void eightyRecruitsInOneGroupCauseExactlyOneScanPerBucket() {
        UUID owner = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        // 80 recruits sharing one owner, all within ~5 blocks of the formation
        // anchor: this is the "shield-wall" scenario from the task acceptance.
        for (int i = 0; i < 80; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, owner, /*tick*/ 100, /*interval*/ 10,
                    new Vec3(i % 8, 0, i / 8),
                    /*radius*/ 8.0D,
                    scanner
            );
        }

        assertEquals(1, scanner.calls(),
                "80 recruits in one group must share a single underlying scan");
        assertEquals(1L, CommanderHostileScanCache.scanCount(),
                "scanCount() must report the same single scan");
    }

    @Test
    void crossingTheScanIntervalBoundaryTriggersOneFreshScanForTheGroup() {
        UUID owner = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        for (int i = 0; i < 80; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, owner, 100, 10,
                    new Vec3(i % 8, 0, i / 8), 8.0D, scanner
            );
        }
        assertEquals(1, scanner.calls(), "first bucket — exactly one scan");

        // Tick 110 -> bucket 11 (was 10). Must invalidate.
        for (int i = 0; i < 80; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, owner, 110, 10,
                    new Vec3(i % 8, 0, i / 8), 8.0D, scanner
            );
        }
        assertEquals(2, scanner.calls(), "second bucket — exactly one more scan");
    }

    @Test
    void distinctOwnersDoNotShareCacheEntries() {
        UUID alpha = UUID.randomUUID();
        UUID beta = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        // Keep both groups tightly packed (within GROUP_AABB_PADDING of each
        // group's first call) so the only multiplier left in scan count is the
        // number of distinct owners.
        for (int i = 0; i < 40; i++) {
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, alpha, 100, 10, new Vec3(i % 8, 0, 0), 8.0D, scanner
            );
            CommanderHostileScanCache.snapshotForTesting(
                    OVERWORLD, beta, 100, 10, new Vec3(100 + (i % 8), 0, 0), 8.0D, scanner
            );
        }
        assertEquals(2, scanner.calls(),
                "two owners must produce two scans, not eighty");
    }

    @Test
    void recruitOutsideCachedEnvelopeRescansAndReplacesSnapshot() {
        UUID owner = UUID.randomUUID();
        CountingScanner scanner = new CountingScanner(List.of());

        CommanderHostileScanCache.snapshotForTesting(
                OVERWORLD, owner, 100, 10, new Vec3(0, 0, 0), 8.0D, scanner
        );
        assertEquals(1, scanner.calls());

        // 500 blocks out — way beyond GROUP_AABB_PADDING=32. covers() must reject.
        CommanderHostileScanCache.snapshotForTesting(
                OVERWORLD, owner, 100, 10, new Vec3(500, 0, 500), 8.0D, scanner
        );
        assertEquals(2, scanner.calls(), "out-of-envelope recruit must re-scan");
    }

    @Test
    void coversAcceptsQueryInsideEnvelopeAndRejectsQueryOutside() {
        Vec3 center = new Vec3(0, 0, 0);
        double scanRadius = 8.0D + CommanderHostileScanCache.GROUP_AABB_PADDING;

        // A recruit at the formation center with the same per-recruit radius — covered.
        assertTrue(CommanderHostileScanCache.covers(center, scanRadius, new Vec3(1, 0, 1), 8.0D));
        // A recruit just barely inside the padding — covered.
        assertTrue(CommanderHostileScanCache.covers(center, scanRadius, new Vec3(31.9, 0, 0), 8.0D));
        // A recruit at the padding edge with the same radius — NOT covered (allowed=padding=32, dist=33).
        assertFalse(CommanderHostileScanCache.covers(center, scanRadius, new Vec3(33, 0, 0), 8.0D));
        // A query whose own radius equals the snapshot radius can never be covered.
        assertFalse(CommanderHostileScanCache.covers(center, scanRadius, center, scanRadius));
    }

    @Test
    void pickNearestPicksClosestWithinRadiusAndRespectsAcceptPredicate() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile near = new FakeHostile(new Vec3(2, 0, 0), true);
        FakeHostile far = new FakeHostile(new Vec3(30, 0, 0), true);
        FakeHostile dead = new FakeHostile(new Vec3(1, 0, 0), false);

        FakeHostile picked = CommanderHostileScanCache.pickNearest(
                origin,
                /*radiusSqr*/ 8.0D * 8.0D,
                List.of(dead, near, far),
                FakeHostile::pos,
                FakeHostile::alive
        );

        assertSame(near, picked, "nearest live within radius wins");

        // Now exclude the live near-hostile via the accept predicate.
        FakeHostile pickedExcluded = CommanderHostileScanCache.pickNearest(
                origin,
                8.0D * 8.0D,
                List.of(near, far),
                FakeHostile::pos,
                h -> h != near && h.alive()
        );
        assertNull(pickedExcluded,
                "with the only in-radius hostile excluded, no candidate is picked");
    }

    @Test
    void pickNearestSkipsCandidatesOutsidePerRecruitRadiusEvenIfInTheCache() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile justOutsideRecruitRadius = new FakeHostile(new Vec3(25, 0, 0), true);

        FakeHostile picked = CommanderHostileScanCache.pickNearest(
                origin,
                8.0D * 8.0D,
                List.of(justOutsideRecruitRadius),
                FakeHostile::pos,
                FakeHostile::alive
        );

        assertNull(picked,
                "hostile beyond per-recruit radius must not be picked even if cached");
    }

    @Test
    void newPoolBehaviorMatchesLegacyDirectScanResultOnFixedScenario() {
        Vec3 origin = new Vec3(0, 0, 0);
        FakeHostile h1 = new FakeHostile(new Vec3(3, 0, 0), true);
        FakeHostile h2 = new FakeHostile(new Vec3(6, 0, 0), true);
        FakeHostile h3 = new FakeHostile(new Vec3(1, 0, 5), true);
        List<FakeHostile> all = List.of(h1, h2, h3);
        double radius = 8.0D;

        FakeHostile poolPick = CommanderHostileScanCache.pickNearest(
                origin, radius * radius, all, FakeHostile::pos, FakeHostile::alive
        );
        FakeHostile legacyPick = legacyFindNearest(origin, radius, all);

        assertNotNull(poolPick);
        assertSame(legacyPick, poolPick,
                "pool result must match the pre-pool direct-scan result on the same scenario");
    }

    @Test
    void computeKeyHashesAndEqualsByAllFields() {
        UUID a = UUID.randomUUID();
        CommanderHostileScanCache.Key k1 = CommanderHostileScanCache.computeKey(OVERWORLD, a, 7);
        CommanderHostileScanCache.Key k2 = CommanderHostileScanCache.computeKey(OVERWORLD, a, 7);
        CommanderHostileScanCache.Key k3 = CommanderHostileScanCache.computeKey(OVERWORLD, a, 8);
        assertEquals(k1, k2, "same triple → equal");
        assertEquals(k1.hashCode(), k2.hashCode());
        assertSame(false, k1.equals(k3),
                "different bucket → not equal");
    }

    private static FakeHostile legacyFindNearest(Vec3 origin, double radius, List<FakeHostile> candidates) {
        FakeHostile best = null;
        double bestSqr = Double.POSITIVE_INFINITY;
        double radiusSqr = radius * radius;
        for (FakeHostile c : candidates) {
            if (!c.alive()) continue;
            double d = origin.distanceToSqr(c.pos());
            if (d <= radiusSqr && d < bestSqr) {
                bestSqr = d;
                best = c;
            }
        }
        return best;
    }

    private record FakeHostile(Vec3 pos, boolean alive) {
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
