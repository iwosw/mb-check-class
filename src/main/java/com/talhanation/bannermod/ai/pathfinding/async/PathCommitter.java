package com.talhanation.bannermod.ai.pathfinding.async;

import com.talhanation.bannermod.util.RuntimeProfilingCounters;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PathCommitter {
    private static final String METRICS_PREFIX = "pathfinding.true_async.commit";

    public CommitSummary commit(List<PathResult> results, CommitTargetResolver resolver, int maxResults) {
        return commit(results, resolver, (entityUuid, target) -> { }, maxResults);
    }

    public CommitSummary commit(List<PathResult> results, CommitTargetResolver resolver, CommitTargetRemoval removal, int maxResults) {
        Objects.requireNonNull(results, "results");
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(removal, "removal");
        int capped = Math.max(0, maxResults);
        int committed = 0;
        int stale = 0;
        int discardedMissingEntity = 0;
        int discardedCancelled = 0;

        for (int i = 0; i < results.size() && i < capped; i++) {
            PathResult result = results.get(i);
            if (result == null) {
                continue;
            }
            if (result.status() == PathResultStatus.CANCELLED || result.status() == PathResultStatus.DEADLINE_EXCEEDED) {
                discardedCancelled++;
                RuntimeProfilingCounters.increment(METRICS_PREFIX + ".discard.cancelled_or_deadline");
                continue;
            }
            CommitTarget target = resolver.resolve(result.entityUuid());
            if (target == null || !target.isAliveAndLoaded()) {
                discardedMissingEntity++;
                RuntimeProfilingCounters.increment(METRICS_PREFIX + ".discard.entity_gone");
                continue;
            }
            if (result.epoch() != target.currentEpoch()) {
                stale++;
                RuntimeProfilingCounters.increment(METRICS_PREFIX + ".discard.stale_epoch");
                continue;
            }
            target.apply(result);
            removal.remove(result.entityUuid(), target);
            committed++;
            RuntimeProfilingCounters.increment(METRICS_PREFIX + ".applied");
        }
        return new CommitSummary(committed, stale, discardedMissingEntity, discardedCancelled);
    }

    public record CommitSummary(int committed, int staleResults, int missingEntityDiscards, int cancelledDiscards) {
    }

    public interface CommitTargetResolver {
        CommitTarget resolve(UUID entityUuid);
    }

    public interface CommitTargetRemoval {
        void remove(UUID entityUuid, CommitTarget target);
    }

    public interface CommitTarget {
        long currentEpoch();
        boolean isAliveAndLoaded();
        void apply(PathResult result);
    }
}
