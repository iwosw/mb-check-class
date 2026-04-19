package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.growth.PendingProject;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModBuildAreaProjectBridgeTest {

    @Test
    void noopResolverRequeuesProjectAtFrontAndReturnsEmpty() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        BannerModBuildAreaProjectBridge bridge = new BannerModBuildAreaProjectBridge();
        UUID claim = UUID.randomUUID();

        PendingProject first = ProjectTestFactory.general(80, 5);
        PendingProject second = ProjectTestFactory.general(60, 5);
        scheduler.submit(claim, first);
        scheduler.submit(claim, second);

        Optional<ProjectAssignment> result = bridge.attemptAssignment(
                scheduler, claim, 1234L,
                new BannerModBuildAreaProjectBridge.NoopBuildAreaResolver()
        );

        assertTrue(result.isEmpty(), "noop resolver must produce no assignment");
        assertEquals(2, scheduler.pendingCount(claim),
                "project must be re-queued when resolver cannot bind it");
        assertSame(first, scheduler.peek(claim).orElseThrow(),
                "original head project must still be at the front");
    }

    @Test
    void stubResolverWithLoadedTemplateProducesSearchingBuilderAssignment() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        BannerModBuildAreaProjectBridge bridge = new BannerModBuildAreaProjectBridge();
        UUID claim = UUID.randomUUID();
        UUID buildArea = UUID.randomUUID();

        PendingProject project = ProjectTestFactory.general(100, 12);
        scheduler.submit(claim, project);

        BannerModBuildAreaProjectBridge.BuildAreaResolver resolver =
                (c, p) -> Optional.of(new BannerModBuildAreaProjectBridge.BuildAreaBinding(buildArea, true, 12));

        Optional<ProjectAssignment> maybeAssignment = bridge.attemptAssignment(
                scheduler, claim, 42L, resolver);

        assertTrue(maybeAssignment.isPresent(), "stub resolver should bind the project");
        ProjectAssignment assignment = maybeAssignment.get();
        assertEquals(project.projectId(), assignment.projectId());
        assertEquals(claim, assignment.claimUuid());
        assertEquals(buildArea, assignment.buildAreaUuid());
        assertSame(project, assignment.project());
        assertEquals(42L, assignment.assignedAtGameTime());
        assertEquals(AssignmentPhase.SEARCHING_BUILDER, assignment.phase());
        assertEquals(0, scheduler.pendingCount(claim),
                "successfully assigned project must leave the queue");
    }

    @Test
    void stubResolverWithUnloadedTemplateProducesMaterialsPendingPhase() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        BannerModBuildAreaProjectBridge bridge = new BannerModBuildAreaProjectBridge();
        UUID claim = UUID.randomUUID();
        UUID buildArea = UUID.randomUUID();

        PendingProject project = ProjectTestFactory.general(50, 3);
        scheduler.submit(claim, project);

        BannerModBuildAreaProjectBridge.BuildAreaResolver resolver =
                (c, p) -> Optional.of(new BannerModBuildAreaProjectBridge.BuildAreaBinding(buildArea, false, 3));

        ProjectAssignment assignment = bridge.attemptAssignment(scheduler, claim, 7L, resolver).orElseThrow();
        assertEquals(AssignmentPhase.MATERIALS_PENDING, assignment.phase());
    }

    @Test
    void emptyQueueYieldsEmptyOptional() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        BannerModBuildAreaProjectBridge bridge = new BannerModBuildAreaProjectBridge();
        UUID claim = UUID.randomUUID();

        Optional<ProjectAssignment> assignment = bridge.attemptAssignment(
                scheduler, claim, 0L,
                new BannerModBuildAreaProjectBridge.NoopBuildAreaResolver()
        );
        assertTrue(assignment.isEmpty());
    }

    @Test
    void resolverExceptionRequeuesAndPropagates() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        BannerModBuildAreaProjectBridge bridge = new BannerModBuildAreaProjectBridge();
        UUID claim = UUID.randomUUID();
        PendingProject project = ProjectTestFactory.general(25, 2);
        scheduler.submit(claim, project);

        BannerModBuildAreaProjectBridge.BuildAreaResolver resolver = (c, p) -> {
            throw new IllegalStateException("resolver boom");
        };

        IllegalStateException boom = assertThrows(IllegalStateException.class,
                () -> bridge.attemptAssignment(scheduler, claim, 5L, resolver));
        assertEquals("resolver boom", boom.getMessage());
        assertEquals(1, scheduler.pendingCount(claim),
                "project must be re-queued when resolver throws");
        assertSame(project, scheduler.peek(claim).orElseThrow());
    }

    @Test
    void runtimeTickClaimFeedsQueueAndReturnsAssignmentWithResolver() {
        BannerModSettlementProjectRuntime runtime = BannerModSettlementProjectRuntime.detached();
        UUID claim = UUID.randomUUID();
        UUID buildArea = UUID.randomUUID();
        PendingProject project = ProjectTestFactory.general(77, 4);

        BannerModBuildAreaProjectBridge.BuildAreaResolver resolver =
                (c, p) -> Optional.of(new BannerModBuildAreaProjectBridge.BuildAreaBinding(buildArea, true, 4));

        Optional<ProjectAssignment> first = runtime.tickClaim(
                null, claim, java.util.List.of(project), resolver, 99L);
        assertTrue(first.isPresent());
        assertEquals(buildArea, first.get().buildAreaUuid());
        assertEquals(AssignmentPhase.SEARCHING_BUILDER, first.get().phase());
        assertEquals(0, runtime.scheduler().pendingCount(claim));

        // Feeding the same project again must be an idempotent no-op in an empty queue
        // followed by re-submission; the scheduler dedupes by projectId only while queued.
        Optional<ProjectAssignment> second = runtime.tickClaim(
                null, claim, java.util.List.of(project), resolver, 100L);
        assertTrue(second.isPresent());
        assertEquals(100L, second.get().assignedAtGameTime());
    }
}
