package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.growth.PendingProject;

import java.util.Optional;
import java.util.UUID;

/**
 * Bridge between a {@link BannerModSettlementProjectScheduler} queue and the existing
 * player-authored BuildArea subsystem.
 *
 * <p>Slice C is deliberately read-only with respect to
 * {@code com.talhanation.bannermod.entity.civilian.workarea.BuildArea}. The real
 * resolver implementation is deferred: callers either inject a production resolver
 * (added in a follow-up slice) or fall back to {@link NoopBuildAreaResolver}, which
 * always returns empty and leaves the project at the head of the queue.
 */
public final class BannerModBuildAreaProjectBridge {

    public BannerModBuildAreaProjectBridge() {
    }

    /**
     * Strategy for picking a {@link com.talhanation.bannermod.entity.civilian.workarea.BuildArea}
     * to host a given {@link PendingProject}. Purely a lookup — the resolver must not
     * start the build or mutate BuildArea state.
     */
    public interface BuildAreaResolver {
        Optional<BuildAreaBinding> resolveCandidate(UUID claimUuid, PendingProject project);
    }

    /**
     * Snapshot of a BuildArea candidate selected by a {@link BuildAreaResolver}.
     *
     * @param buildAreaUuid       the entity UUID of the resolved BuildArea.
     * @param hasTemplateLoaded   whether the structure NBT is already populated. If false, the
     *                            assignment starts in {@link AssignmentPhase#MATERIALS_PENDING}.
     * @param estimatedTickCost   best-effort remaining tick cost for the build, used for back-pressure.
     */
    public record BuildAreaBinding(UUID buildAreaUuid, boolean hasTemplateLoaded, int estimatedTickCost) {
        public BuildAreaBinding {
            if (buildAreaUuid == null) {
                throw new IllegalArgumentException("buildAreaUuid must not be null");
            }
            if (estimatedTickCost < 0) {
                estimatedTickCost = 0;
            }
        }
    }

    /**
     * Resolver that never binds a project. Production callers should replace this with a real
     * implementation that walks the active {@code BuildArea} entities in the claim.
     *
     * <pre>
     * // FIXME slice-C-follow-up: provide a production BuildAreaResolver backed by
     * //   com.talhanation.bannermod.entity.civilian.workarea.BuildArea lookups.
     * //   The existing BuildArea API exposes getUUID(), hasStructureTemplate(),
     * //   hasPendingBuildWork(), and setStartBuild(boolean) — enough to implement
     * //   resolution once the settlement <-> claim plumbing (slice D) is committed.
     * </pre>
     */
    public static final class NoopBuildAreaResolver implements BuildAreaResolver {
        @Override
        public Optional<BuildAreaBinding> resolveCandidate(UUID claimUuid, PendingProject project) {
            return Optional.empty();
        }
    }

    /**
     * Pop the next {@link PendingProject} for {@code claimUuid}, try to resolve a BuildArea
     * for it, and return a fresh {@link ProjectAssignment} bound to that BuildArea.
     *
     * <p>If the resolver cannot bind a BuildArea the project is pushed back onto the front
     * of the scheduler queue and {@link Optional#empty()} is returned so the caller can
     * retry next tick.
     */
    public Optional<ProjectAssignment> attemptAssignment(
            BannerModSettlementProjectScheduler scheduler,
            UUID claimUuid,
            long gameTime,
            BuildAreaResolver resolver
    ) {
        if (scheduler == null || claimUuid == null || resolver == null) {
            return Optional.empty();
        }
        Optional<PendingProject> head = scheduler.pollNext(claimUuid);
        if (head.isEmpty()) {
            return Optional.empty();
        }
        PendingProject project = head.get();
        Optional<BuildAreaBinding> binding;
        try {
            binding = resolver.resolveCandidate(claimUuid, project);
        } catch (RuntimeException resolverError) {
            // Resolver failures must not drop the project. Re-queue and bubble nothing.
            scheduler.requeueFront(claimUuid, project);
            throw resolverError;
        }
        if (binding.isEmpty()) {
            scheduler.requeueFront(claimUuid, project);
            return Optional.empty();
        }
        BuildAreaBinding resolved = binding.get();
        AssignmentPhase phase = resolved.hasTemplateLoaded()
                ? AssignmentPhase.SEARCHING_BUILDER
                : AssignmentPhase.MATERIALS_PENDING;
        return Optional.of(new ProjectAssignment(
                project.projectId(),
                claimUuid,
                resolved.buildAreaUuid(),
                project,
                gameTime,
                phase
        ));
    }
}
