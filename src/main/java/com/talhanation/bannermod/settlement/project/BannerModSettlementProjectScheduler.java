package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.growth.PendingProject;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-side in-memory queue of {@link PendingProject} entries, keyed by claim UUID.
 *
 * <p>Slice C-only concerns: bounded ingestion from the growth evaluator, bookkeeping,
 * and hand-off to {@link BannerModBuildAreaProjectBridge}. No persistence yet.
 *
 * <p>Thread model: expected to be touched from the server thread only. No internal
 * synchronization is provided.
 *
 * <pre>
 * // TODO persistence: scheduler contents should survive server restarts.
 * //                   Candidate home: SavedData alongside BannerModSettlementManager.
 * </pre>
 */
public final class BannerModSettlementProjectScheduler {

    /** Upper bound on queued projects per claim; extra {@link #submit} calls silently drop. */
    public static final int PER_CLAIM_QUEUE_CAP = 16;

    @Nullable
    private final ServerLevel level;

    private final Map<UUID, Deque<PendingProject>> queues = new HashMap<>();

    /** Optional record of the last cancellation per project, handy for diagnostics. */
    private final Map<UUID, ProjectCancellationReason> cancellationLog = new HashMap<>();

    private BannerModSettlementProjectScheduler(@Nullable ServerLevel level) {
        this.level = level;
    }

    /** Production entrypoint. One scheduler per {@link ServerLevel}. */
    public static BannerModSettlementProjectScheduler forServer(ServerLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        return new BannerModSettlementProjectScheduler(level);
    }

    /** Package-private factory for unit tests that cannot instantiate a {@link ServerLevel}. */
    static BannerModSettlementProjectScheduler detached() {
        return new BannerModSettlementProjectScheduler(null);
    }

    /**
     * Append a project to the tail of {@code claimUuid}'s queue. Rejected if the queue is
     * at {@link #PER_CLAIM_QUEUE_CAP} or the project is already queued.
     */
    public void submit(UUID claimUuid, PendingProject project) {
        if (claimUuid == null || project == null) {
            return;
        }
        Deque<PendingProject> queue = queues.computeIfAbsent(claimUuid, k -> new ArrayDeque<>());
        if (queue.size() >= PER_CLAIM_QUEUE_CAP) {
            return;
        }
        for (PendingProject existing : queue) {
            if (existing.projectId().equals(project.projectId())) {
                return;
            }
        }
        queue.addLast(project);
    }

    /** Non-destructive look at the head of {@code claimUuid}'s queue. */
    public Optional<PendingProject> peek(UUID claimUuid) {
        Deque<PendingProject> queue = queues.get(claimUuid);
        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(queue.peekFirst());
    }

    /** Remove and return the head of {@code claimUuid}'s queue. */
    public Optional<PendingProject> pollNext(UUID claimUuid) {
        Deque<PendingProject> queue = queues.get(claimUuid);
        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(queue.pollFirst());
    }

    /**
     * Push {@code project} back onto the head of {@code claimUuid}'s queue.
     * Used by the bridge when a resolver cannot yet bind the project to a BuildArea.
     * Respects {@link #PER_CLAIM_QUEUE_CAP}.
     */
    public void requeueFront(UUID claimUuid, PendingProject project) {
        if (claimUuid == null || project == null) {
            return;
        }
        Deque<PendingProject> queue = queues.computeIfAbsent(claimUuid, k -> new ArrayDeque<>());
        if (queue.size() >= PER_CLAIM_QUEUE_CAP) {
            return;
        }
        queue.addFirst(project);
    }

    public int pendingCount(UUID claimUuid) {
        Deque<PendingProject> queue = queues.get(claimUuid);
        return queue == null ? 0 : queue.size();
    }

    /** Stable, defensive copy of the queue contents for UI/diagnostics. */
    public List<PendingProject> snapshot(UUID claimUuid) {
        Deque<PendingProject> queue = queues.get(claimUuid);
        if (queue == null || queue.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(queue);
    }

    /**
     * Remove the project with {@code projectId} from whichever claim holds it.
     * Records the {@code reason} in the cancellation log. No-op if the project is not queued.
     */
    public void cancel(UUID projectId, ProjectCancellationReason reason) {
        if (projectId == null) {
            return;
        }
        ProjectCancellationReason effectiveReason = reason == null ? ProjectCancellationReason.MANUAL : reason;
        for (Deque<PendingProject> queue : queues.values()) {
            Iterator<PendingProject> it = queue.iterator();
            while (it.hasNext()) {
                PendingProject project = it.next();
                if (project.projectId().equals(projectId)) {
                    it.remove();
                    cancellationLog.put(projectId, effectiveReason);
                    return;
                }
            }
        }
        // Not queued but still worth remembering — a later slice may cancel an in-progress
        // assignment whose PendingProject has already been polled.
        cancellationLog.put(projectId, effectiveReason);
    }

    /** Diagnostic accessor; returns null if the project was never cancelled via this scheduler. */
    @Nullable
    public ProjectCancellationReason lastCancellationReason(UUID projectId) {
        return cancellationLog.get(projectId);
    }

    /** Drop all queues and cancellation entries; intended for world unload / reset. */
    public void reset() {
        queues.clear();
        cancellationLog.clear();
    }

    /** Package-private accessor for the bridge and tests. Returns {@code null} in detached mode. */
    @Nullable
    ServerLevel level() {
        return level;
    }
}
