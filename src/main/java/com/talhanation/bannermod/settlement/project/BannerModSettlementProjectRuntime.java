package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.growth.PendingProject;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Minimal facade that binds one {@link BannerModSettlementProjectScheduler} and
 * one {@link BannerModBuildAreaProjectBridge} per {@link ServerLevel}.
 *
 * <p>Settlement-service code (arriving in slice D) feeds freshly scored
 * {@link PendingProject growth queues} in and receives {@link ProjectAssignment}s
 * back. Nothing is persisted; everything lives only for the lifetime of the server
 * process. A real resolver is not bundled here — callers supply one, falling back
 * to {@link BannerModBuildAreaProjectBridge.NoopBuildAreaResolver} when the
 * production resolver is not yet available.
 */
public final class BannerModSettlementProjectRuntime {

    private static final WeakHashMap<ServerLevel, BannerModSettlementProjectRuntime> PER_LEVEL = new WeakHashMap<>();

    private final BannerModSettlementProjectScheduler scheduler;
    private final BannerModBuildAreaProjectBridge bridge;

    private BannerModSettlementProjectRuntime(BannerModSettlementProjectScheduler scheduler,
                                              BannerModBuildAreaProjectBridge bridge) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
    }

    /** Lazy per-level singleton for production use. */
    public static synchronized BannerModSettlementProjectRuntime forServer(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return PER_LEVEL.computeIfAbsent(level, lvl -> new BannerModSettlementProjectRuntime(
                BannerModSettlementProjectScheduler.forServer(lvl),
                new BannerModBuildAreaProjectBridge()
        ));
    }

    /** Package-private factory for tests and detached callers. */
    static BannerModSettlementProjectRuntime detached() {
        return new BannerModSettlementProjectRuntime(
                BannerModSettlementProjectScheduler.detached(),
                new BannerModBuildAreaProjectBridge()
        );
    }

    public BannerModSettlementProjectScheduler scheduler() {
        return scheduler;
    }

    public BannerModBuildAreaProjectBridge bridge() {
        return bridge;
    }

    /**
     * Feed a newly scored growth queue into the scheduler and attempt to bind the head project
     * to a BuildArea. Returns the resulting {@link ProjectAssignment} when binding succeeds.
     *
     * <p>Entries already queued under {@code claimUuid} are preserved; duplicate project IDs
     * are ignored. Overflow beyond {@link BannerModSettlementProjectScheduler#PER_CLAIM_QUEUE_CAP}
     * drops silently.
     */
    public Optional<ProjectAssignment> tickClaim(
            @Nullable ServerLevel ignoredLevel,
            UUID claimUuid,
            List<PendingProject> growthQueue,
            BannerModBuildAreaProjectBridge.BuildAreaResolver resolver,
            long gameTime
    ) {
        if (claimUuid == null) {
            return Optional.empty();
        }
        List<PendingProject> safeQueue = growthQueue == null ? List.of() : growthQueue;
        for (PendingProject candidate : safeQueue) {
            if (candidate != null) {
                scheduler.submit(claimUuid, candidate);
            }
        }
        BannerModBuildAreaProjectBridge.BuildAreaResolver safeResolver = resolver == null
                ? new BannerModBuildAreaProjectBridge.NoopBuildAreaResolver()
                : resolver;
        return bridge.attemptAssignment(scheduler, claimUuid, gameTime, safeResolver);
    }

    /**
     * Static convenience overload matching the signature promised to downstream callers.
     * Resolves or creates the runtime for {@code level} and delegates to the instance method
     * with a {@link BannerModBuildAreaProjectBridge.NoopBuildAreaResolver}.
     */
    public static Optional<ProjectAssignment> tickClaim(ServerLevel level, UUID claimUuid, List<PendingProject> growthQueue) {
        if (level == null || claimUuid == null) {
            return Optional.empty();
        }
        BannerModSettlementProjectRuntime runtime = forServer(level);
        long gameTime = level.getGameTime();
        return runtime.tickClaim(level, claimUuid, growthQueue,
                new BannerModBuildAreaProjectBridge.NoopBuildAreaResolver(), gameTime);
    }

    /** Defensive copy of the scheduler's current queue for {@code claimUuid}. */
    public List<PendingProject> snapshot(UUID claimUuid) {
        return new ArrayList<>(scheduler.snapshot(claimUuid));
    }
}
