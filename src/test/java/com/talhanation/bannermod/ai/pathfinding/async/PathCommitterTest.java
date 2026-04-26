package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathCommitterTest {

    @Test
    void committerDiscardsStaleAndMissingAndCancelledResults() {
        UUID activeEntity = UUID.randomUUID();
        UUID staleEntity = UUID.randomUUID();
        UUID missingEntity = UUID.randomUUID();
        AtomicInteger appliedCount = new AtomicInteger();

        PathCommitter.CommitTargetResolver resolver = entityUuid -> {
            if (entityUuid.equals(activeEntity)) {
                return new StubTarget(5L, true, appliedCount);
            }
            if (entityUuid.equals(staleEntity)) {
                return new StubTarget(7L, true, appliedCount);
            }
            if (entityUuid.equals(missingEntity)) {
                return new StubTarget(2L, false, appliedCount);
            }
            return null;
        };

        List<PathResult> results = List.of(
                result(activeEntity, 11L, 5L, PathResultStatus.SUCCESS),
                result(staleEntity, 12L, 4L, PathResultStatus.SUCCESS),
                result(missingEntity, 13L, 2L, PathResultStatus.SUCCESS),
                result(UUID.randomUUID(), 14L, 1L, PathResultStatus.CANCELLED)
        );

        PathCommitter.CommitSummary summary = new PathCommitter().commit(results, resolver, 10);

        assertEquals(1, summary.committed());
        assertEquals(1, summary.staleResults());
        assertEquals(1, summary.missingEntityDiscards());
        assertEquals(1, summary.cancelledDiscards());
        assertEquals(1, appliedCount.get());
    }

    private static PathResult result(UUID entityUuid, long requestId, long epoch, PathResultStatus status) {
        return new PathResult(
                entityUuid,
                requestId,
                epoch,
                status,
                List.of(new BlockPos(0, 64, 0), new BlockPos(1, 64, 1)),
                status == PathResultStatus.SUCCESS,
                2.0D,
                10,
                100L,
                status.name()
        );
    }

    private record StubTarget(long currentEpoch, boolean aliveAndLoaded, AtomicInteger appliedCounter) implements PathCommitter.CommitTarget {

        @Override
        public boolean isAliveAndLoaded() {
            return aliveAndLoaded;
        }

        @Override
        public void apply(PathResult result) {
            appliedCounter.incrementAndGet();
        }
    }
}
