package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CoarsePathCacheTest {
    private static final String TEST_DIMENSION = "bannermod:coarse_cache_test";

    @Test
    void lookupReturnsRefinedPathWhenCacheEntryMatches() {
        CoarsePathCache cache = new CoarsePathCache();
        PathRequestSnapshot original = request(11L, new BlockPos(0, 64, 0), new BlockPos(32, 64, 32));
        CoarsePathKey key = CoarsePathKey.of(TEST_DIMENSION, original.start(), original.targets().get(0), PathPriority.FOLLOW);
        PathResult result = solved(original, List.of(
                new BlockPos(0, 64, 0),
                new BlockPos(8, 64, 8),
                new BlockPos(16, 64, 16),
                new BlockPos(32, 64, 32)
        ));
        cache.store(key, original, result, original.targets().get(0), 100L, 16);

        PathRequestSnapshot next = request(12L, new BlockPos(1, 64, 1), new BlockPos(31, 64, 31));
        PathResult cached = cache.lookup(key, next, next.targets().get(0), 110L, 200L, 4.0D).orElse(null);
        assertNotNull(cached);
        assertEquals(PathResultStatus.PARTIAL, cached.status());
        assertEquals(next.start(), cached.nodes().get(0));
        assertEquals(next.targets().get(0), cached.nodes().get(cached.nodes().size() - 1));
        assertEquals("coarse_cache_hit", cached.debugReason());
    }

    @Test
    void lookupMissesWhenEntryExpired() {
        CoarsePathCache cache = new CoarsePathCache();
        PathRequestSnapshot request = request(21L, new BlockPos(0, 64, 0), new BlockPos(16, 64, 16));
        CoarsePathKey key = CoarsePathKey.of(TEST_DIMENSION, request.start(), request.targets().get(0), PathPriority.FOLLOW);
        cache.store(key, request, solved(request, List.of(request.start(), request.targets().get(0))), request.targets().get(0), 10L, 4);

        assertTrue(cache.lookup(key, request, request.targets().get(0), 30L, 5L, 2.0D).isEmpty());
    }

    @Test
    void trimEvictsLeastRecentlyUsedEntries() {
        CoarsePathCache cache = new CoarsePathCache();
        PathRequestSnapshot a = request(31L, new BlockPos(0, 64, 0), new BlockPos(8, 64, 8));
        PathRequestSnapshot b = request(32L, new BlockPos(16, 64, 16), new BlockPos(24, 64, 24));
        PathRequestSnapshot c = request(33L, new BlockPos(32, 64, 32), new BlockPos(40, 64, 40));

        CoarsePathKey keyA = CoarsePathKey.of(TEST_DIMENSION, a.start(), a.targets().get(0), PathPriority.FOLLOW);
        CoarsePathKey keyB = CoarsePathKey.of(TEST_DIMENSION, b.start(), b.targets().get(0), PathPriority.FOLLOW);
        CoarsePathKey keyC = CoarsePathKey.of(TEST_DIMENSION, c.start(), c.targets().get(0), PathPriority.FOLLOW);

        cache.store(keyA, a, solved(a, List.of(a.start(), a.targets().get(0))), a.targets().get(0), 1L, 2);
        cache.store(keyB, b, solved(b, List.of(b.start(), b.targets().get(0))), b.targets().get(0), 2L, 2);
        assertTrue(cache.lookup(keyA, a, a.targets().get(0), 3L, 100L, 1.0D).isPresent());
        cache.store(keyC, c, solved(c, List.of(c.start(), c.targets().get(0))), c.targets().get(0), 4L, 2);

        assertTrue(cache.lookup(keyA, a, a.targets().get(0), 5L, 100L, 1.0D).isPresent());
        assertTrue(cache.lookup(keyC, c, c.targets().get(0), 5L, 100L, 1.0D).isPresent());
        assertTrue(cache.lookup(keyB, b, b.targets().get(0), 5L, 100L, 1.0D).isEmpty());
    }

    private static PathRequestSnapshot request(long requestId, BlockPos start, BlockPos target) {
        return new PathRequestSnapshot(
                UUID.randomUUID(),
                requestId,
                1L,
                start,
                List.of(target),
                128,
                64.0F,
                0.9D,
                1.8D,
                0.6D,
                false,
                false,
                false,
                PathPriority.FOLLOW,
                0L,
                Long.MAX_VALUE
        );
    }

    private static PathResult solved(PathRequestSnapshot request, List<BlockPos> nodes) {
        return new PathResult(
                request.entityUuid(),
                request.requestId(),
                request.epoch(),
                PathResultStatus.SUCCESS,
                nodes,
                true,
                10.0D,
                nodes.size(),
                1000L,
                ""
        );
    }
}
