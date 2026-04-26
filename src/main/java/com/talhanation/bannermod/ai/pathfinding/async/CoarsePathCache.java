package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

final class CoarsePathCache {
    private final Map<CoarsePathKey, Entry> entries = new ConcurrentHashMap<>();
    private final AtomicLong accessCounter = new AtomicLong();

    Optional<PathResult> lookup(CoarsePathKey key,
                                PathRequestSnapshot request,
                                BlockPos target,
                                long gameTime,
                                long maxAgeTicks,
                                double refineDistance) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (maxAgeTicks >= 0L && gameTime - entry.createdAtGameTime > maxAgeTicks) {
            entries.remove(key, entry);
            return Optional.empty();
        }
        if (!entry.matchesRequest(request, target, refineDistance)) {
            return Optional.empty();
        }
        entry.lastAccessOrder = accessCounter.incrementAndGet();
        return Optional.of(entry.toCachedResult(request));
    }

    void store(CoarsePathKey key, PathRequestSnapshot request, PathResult result, BlockPos target, long gameTime, int maxEntries) {
        if (result.status() != PathResultStatus.SUCCESS && result.status() != PathResultStatus.PARTIAL) {
            return;
        }
        if (result.nodes().isEmpty()) {
            return;
        }
        Entry entry = new Entry(
                request.start(),
                target,
                result.nodes(),
                extractCoarseNodes(result.nodes()),
                gameTime,
                accessCounter.incrementAndGet()
        );
        entries.put(key, entry);
        trimToSize(Math.max(1, maxEntries));
    }

    private void trimToSize(int maxEntries) {
        if (entries.size() <= maxEntries) {
            return;
        }
        List<Map.Entry<CoarsePathKey, Entry>> snapshot = new ArrayList<>(entries.entrySet());
        snapshot.sort(Comparator.comparingLong(value -> value.getValue().lastAccessOrder));
        int removeCount = Math.max(0, snapshot.size() - maxEntries);
        for (int i = 0; i < removeCount; i++) {
            Map.Entry<CoarsePathKey, Entry> candidate = snapshot.get(i);
            entries.remove(candidate.getKey(), candidate.getValue());
        }
    }

    private static List<ChunkPos> extractCoarseNodes(List<BlockPos> fineNodes) {
        if (fineNodes.isEmpty()) {
            return List.of();
        }
        List<ChunkPos> coarse = new ArrayList<>();
        ChunkPos previous = null;
        for (BlockPos node : fineNodes) {
            ChunkPos current = new ChunkPos(node);
            if (!current.equals(previous)) {
                coarse.add(current);
                previous = current;
            }
        }
        return List.copyOf(coarse);
    }

    private static final class Entry {
        private final BlockPos cachedStart;
        private final BlockPos cachedTarget;
        private final List<BlockPos> fineNodes;
        @SuppressWarnings("unused")
        private final List<ChunkPos> coarseNodes;
        private final long createdAtGameTime;
        private volatile long lastAccessOrder;

        private Entry(BlockPos cachedStart,
                      BlockPos cachedTarget,
                      List<BlockPos> fineNodes,
                      List<ChunkPos> coarseNodes,
                      long createdAtGameTime,
                      long lastAccessOrder) {
            this.cachedStart = cachedStart;
            this.cachedTarget = cachedTarget;
            this.fineNodes = List.copyOf(fineNodes);
            this.coarseNodes = List.copyOf(coarseNodes);
            this.createdAtGameTime = createdAtGameTime;
            this.lastAccessOrder = lastAccessOrder;
        }

        private boolean matchesRequest(PathRequestSnapshot request, BlockPos target, double refineDistance) {
            double threshold = Math.max(0.0D, refineDistance) + 1.0D;
            return cachedStart.closerThan(request.start(), threshold)
                    && cachedTarget.closerThan(target, threshold)
                    && !fineNodes.isEmpty();
        }

        private PathResult toCachedResult(PathRequestSnapshot request) {
            List<BlockPos> refined = new ArrayList<>(fineNodes);
            refined.set(0, request.start());
            refined.set(refined.size() - 1, request.targets().get(0));
            return new PathResult(
                    request.entityUuid(),
                    request.requestId(),
                    request.epoch(),
                    PathResultStatus.PARTIAL,
                    refined,
                    false,
                    0.0D,
                    0,
                    0L,
                    "coarse_cache_hit"
            );
        }
    }
}
