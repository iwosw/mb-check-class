package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pools the per-recruit AABB hostile scan that {@link UseShield} (and later
 * {@code RecruitMoraleRoutGoal} / {@code RangedSpacingService}) used to issue
 * once per recruit per {@code HOSTILE_SCAN_INTERVAL_TICKS}. At 80 recruits in
 * a single shield-wall group this turned into 80 overlapping
 * {@code level.getEntitiesOfClass(LivingEntity, AABB)} calls per scan tick.
 *
 * <p>The cache is keyed on (level dimension, owner UUID, scan-tick bucket).
 * On a miss the first querying recruit performs <strong>one</strong>
 * level scan whose AABB is inflated by {@link #GROUP_AABB_PADDING} so that
 * other in-formation recruits can filter their own per-recruit radius from
 * the cached candidate list without re-scanning. A recruit that has wandered
 * outside the cached scan envelope falls back to performing its own scan and
 * replaces the snapshot for the rest of the bucket.
 *
 * <p>The single static {@link #SCAN_COUNT} counter is observable by tests and
 * is the operative proxy for the "exactly one hostile scan per group per
 * scan-interval" acceptance check on SCANPOOL-001.
 *
 * <p>The pure helpers {@link #covers}, {@link #computeKey}, and
 * {@link #pickNearest} are public so the cache invariants can be unit-tested
 * without a live {@link AbstractRecruitEntity} or {@link Level}.
 */
public final class CommanderHostileScanCache {

    /**
     * Padding (in blocks) added to the per-recruit scan radius before the
     * cached AABB is built. Sized to comfortably cover an 80-recruit
     * shield-wall while staying well under {@link Level} chunk-loading limits.
     */
    public static final double GROUP_AABB_PADDING = 32.0D;

    private static final AtomicLong SCAN_COUNT = new AtomicLong();
    private static final ConcurrentHashMap<Key, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private CommanderHostileScanCache() {
    }

    /**
     * Production scan source backed by {@link Level#getEntitiesOfClass}.
     */
    public static final Function<ScanRequest, List<LivingEntity>> LEVEL_SCANNER =
        request -> request.level().getEntitiesOfClass(LivingEntity.class, request.aabb());

    /**
     * Returns the nearest hostile to {@code recruit} within {@code recruitScanRadius},
     * filtered by {@code targeting}, going through the per-group cache.
     *
     * <p>Use {@link #LEVEL_SCANNER} as the {@code scanner} in production code.
     */
    @Nullable
    public static LivingEntity findNearestHostile(
        AbstractRecruitEntity recruit,
        double recruitScanRadius,
        int scanIntervalTicks,
        BiPredicate<AbstractRecruitEntity, LivingEntity> targeting,
        Function<ScanRequest, List<LivingEntity>> scanner
    ) {
        Snapshot snap = snapshotFor(recruit, recruitScanRadius, scanIntervalTicks, scanner);
        Vec3 origin = recruit.position();
        double radiusSqr = recruitScanRadius * recruitScanRadius;
        return pickNearest(
                origin,
                radiusSqr,
                snap.hostiles(),
                LivingEntity::position,
                candidate -> candidate != recruit && candidate.isAlive() && targeting.test(recruit, candidate)
        );
    }

    /**
     * Looks up — or computes and stores — the cached snapshot for the recruit's
     * group and the current scan-tick bucket. Performs at most one
     * {@code scanner} call per (level, owner, bucket) for an in-formation group.
     */
    public static Snapshot snapshotFor(
        AbstractRecruitEntity recruit,
        double recruitScanRadius,
        int scanIntervalTicks,
        Function<ScanRequest, List<LivingEntity>> scanner
    ) {
        int interval = Math.max(1, scanIntervalTicks);
        int bucket = Math.floorDiv(recruit.tickCount, interval);
        Key key = computeKey(recruit.level().dimension(), ownerKeyFor(recruit), bucket);
        Snapshot existing = SNAPSHOTS.get(key);
        Vec3 recruitPos = recruit.position();
        if (existing != null && covers(existing.center(), existing.scanRadius(), recruitPos, recruitScanRadius)) {
            return existing;
        }
        double scanRadius = recruitScanRadius + GROUP_AABB_PADDING;
        AABB aabb = recruit.getBoundingBox().inflate(scanRadius);
        List<LivingEntity> hostiles = List.copyOf(scanner.apply(new ScanRequest(recruit.level(), aabb)));
        SCAN_COUNT.incrementAndGet();
        Snapshot fresh = new Snapshot(bucket, recruitPos, scanRadius, hostiles);
        SNAPSHOTS.put(key, fresh);
        return fresh;
    }

    /**
     * Pure: returns the nearest candidate within {@code radiusSqr} of {@code origin}
     * that satisfies {@code accept}. Candidates outside the radius are skipped.
     */
    @Nullable
    public static <T> T pickNearest(
        Vec3 origin,
        double radiusSqr,
        List<T> candidates,
        Function<T, Vec3> positionOf,
        Predicate<T> accept
    ) {
        T best = null;
        double bestDistSqr = Double.POSITIVE_INFINITY;
        for (T candidate : candidates) {
            if (!accept.test(candidate)) continue;
            double d = origin.distanceToSqr(positionOf.apply(candidate));
            if (d <= radiusSqr && d < bestDistSqr) {
                bestDistSqr = d;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Pure: returns true when a per-recruit query at {@code queryCenter} with
     * radius {@code queryRadius} fits entirely inside the cached scan envelope
     * centered at {@code snapCenter} with radius {@code snapRadius}.
     */
    public static boolean covers(Vec3 snapCenter, double snapRadius, Vec3 queryCenter, double queryRadius) {
        double allowedDist = snapRadius - queryRadius;
        if (allowedDist <= 0.0D) {
            return false;
        }
        return queryCenter.distanceToSqr(snapCenter) <= allowedDist * allowedDist;
    }

    /** Pure: builds the snapshot key for a (dimension, owner, bucket) triple. */
    public static Key computeKey(ResourceKey<Level> dimension, UUID ownerId, int bucket) {
        return new Key(dimension, ownerId, bucket);
    }

    /** Total number of underlying world scans performed. Test observable. */
    public static long scanCount() {
        return SCAN_COUNT.get();
    }

    /** Wipes counters and snapshots. Tests only. */
    public static void resetForTesting() {
        SCAN_COUNT.set(0);
        SNAPSHOTS.clear();
    }

    /**
     * Test entry point that drives the cache the same way the production
     * {@link #snapshotFor} path does, but takes the (dimension, owner, bucket,
     * center, radius) inputs directly so the per-group dedup contract can be
     * verified without instantiating a real {@link AbstractRecruitEntity} or
     * {@link Level}.
     */
    public static Snapshot snapshotForTesting(
        ResourceKey<Level> dimension,
        UUID ownerId,
        int tickCount,
        int scanIntervalTicks,
        Vec3 center,
        double recruitScanRadius,
        Function<AABB, List<LivingEntity>> scanner
    ) {
        int interval = Math.max(1, scanIntervalTicks);
        int bucket = Math.floorDiv(tickCount, interval);
        Key key = computeKey(dimension, ownerId, bucket);
        Snapshot existing = SNAPSHOTS.get(key);
        if (existing != null && covers(existing.center(), existing.scanRadius(), center, recruitScanRadius)) {
            return existing;
        }
        double scanRadius = recruitScanRadius + GROUP_AABB_PADDING;
        AABB aabb = new AABB(
            center.x - scanRadius, center.y - scanRadius, center.z - scanRadius,
            center.x + scanRadius, center.y + scanRadius, center.z + scanRadius
        );
        List<LivingEntity> hostiles = new ArrayList<>(scanner.apply(aabb));
        SCAN_COUNT.incrementAndGet();
        Snapshot fresh = new Snapshot(bucket, center, scanRadius, List.copyOf(hostiles));
        SNAPSHOTS.put(key, fresh);
        return fresh;
    }

    private static UUID ownerKeyFor(AbstractRecruitEntity recruit) {
        UUID ownerId = recruit.getOwnerUUID();
        return ownerId != null ? ownerId : recruit.getUUID();
    }

    /** Compact key used by the snapshot cache. */
    public record Key(ResourceKey<Level> dimension, UUID ownerId, int tickBucket) {
    }

    /** Frozen result of a single per-group scan within a bucket. */
    public record Snapshot(int bucket, Vec3 center, double scanRadius, List<LivingEntity> hostiles) {
    }

    /** Input to a scan provider so the cache can be unit-tested without a real Level. */
    public record ScanRequest(Level level, AABB aabb) {
        /** Convenience for tests that ignore the AABB. */
        public BlockPos centerBlock() {
            return BlockPos.containing(aabb.getCenter());
        }
    }
}
