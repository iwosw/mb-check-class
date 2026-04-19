package com.talhanation.bannermod.settlement.project;

import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.growth.ProjectBlocker;
import com.talhanation.bannermod.settlement.growth.ProjectKind;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementProjectSchedulerTest {

    @Test
    void submitThenPollRoundTripsPreservesProject() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        PendingProject project = ProjectTestFactory.general(100, 10);

        scheduler.submit(claim, project);

        assertEquals(1, scheduler.pendingCount(claim));
        Optional<PendingProject> peeked = scheduler.peek(claim);
        assertTrue(peeked.isPresent());
        assertSame(project, peeked.get());

        Optional<PendingProject> polled = scheduler.pollNext(claim);
        assertTrue(polled.isPresent());
        assertSame(project, polled.get());
        assertEquals(0, scheduler.pendingCount(claim));
        assertTrue(scheduler.pollNext(claim).isEmpty());
    }

    @Test
    void overflowBeyondCapSilentlyDropsExcess() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        int over = BannerModSettlementProjectScheduler.PER_CLAIM_QUEUE_CAP + 5;

        PendingProject[] submitted = new PendingProject[over];
        for (int i = 0; i < over; i++) {
            submitted[i] = ProjectTestFactory.general(i, 1);
            scheduler.submit(claim, submitted[i]);
        }

        assertEquals(BannerModSettlementProjectScheduler.PER_CLAIM_QUEUE_CAP,
                scheduler.pendingCount(claim),
                "queue must clamp to per-claim cap");

        // The first PER_CLAIM_QUEUE_CAP entries are the ones that stuck.
        List<PendingProject> kept = scheduler.snapshot(claim);
        for (int i = 0; i < BannerModSettlementProjectScheduler.PER_CLAIM_QUEUE_CAP; i++) {
            assertSame(submitted[i], kept.get(i),
                    "overflow must drop late submissions, not early ones");
        }
    }

    @Test
    void cancelByProjectIdRemovesFromQueue() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        PendingProject head = ProjectTestFactory.general(50, 5);
        PendingProject mid = ProjectTestFactory.general(40, 5);
        PendingProject tail = ProjectTestFactory.general(30, 5);

        scheduler.submit(claim, head);
        scheduler.submit(claim, mid);
        scheduler.submit(claim, tail);

        scheduler.cancel(mid.projectId(), ProjectCancellationReason.SUPERSEDED);

        List<PendingProject> remaining = scheduler.snapshot(claim);
        assertEquals(2, remaining.size());
        assertSame(head, remaining.get(0));
        assertSame(tail, remaining.get(1));
        assertEquals(ProjectCancellationReason.SUPERSEDED,
                scheduler.lastCancellationReason(mid.projectId()));
    }

    @Test
    void perClaimQueuesStayIsolated() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claimX = UUID.randomUUID();
        UUID claimY = UUID.randomUUID();

        PendingProject projectX = ProjectTestFactory.general(80, 5);
        PendingProject projectY = ProjectTestFactory.general(90, 5);

        scheduler.submit(claimX, projectX);
        scheduler.submit(claimY, projectY);

        assertEquals(1, scheduler.pendingCount(claimX));
        assertEquals(1, scheduler.pendingCount(claimY));

        scheduler.pollNext(claimX);
        assertEquals(0, scheduler.pendingCount(claimX));
        assertEquals(1, scheduler.pendingCount(claimY),
                "polling claim X must not drain claim Y");
        assertSame(projectY, scheduler.peek(claimY).orElseThrow());
    }

    @Test
    void snapshotReturnsStableDefensiveCopy() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        PendingProject first = ProjectTestFactory.general(10, 5);
        PendingProject second = ProjectTestFactory.general(20, 5);

        scheduler.submit(claim, first);
        scheduler.submit(claim, second);

        List<PendingProject> before = scheduler.snapshot(claim);
        assertEquals(List.of(first, second), before);

        // Mutations to the scheduler after the snapshot must not change the snapshot.
        scheduler.pollNext(claim);
        assertEquals(2, before.size(), "snapshot must be independent of later polls");
        assertSame(first, before.get(0));

        // And two successive snapshots must be independent lists with equal content.
        scheduler.submit(claim, first);
        List<PendingProject> after = scheduler.snapshot(claim);
        List<PendingProject> afterAgain = scheduler.snapshot(claim);
        assertEquals(after, afterAgain);
        assertNotSame(after, afterAgain);
    }

    @Test
    void duplicateSubmitsAreDroppedByProjectId() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        PendingProject project = new PendingProject(
                projectId,
                ProjectKind.NEW_BUILDING,
                null,
                BannerModSettlementBuildingCategory.GENERAL,
                BannerModSettlementBuildingProfileSeed.GENERAL,
                100,
                0L,
                5,
                ProjectBlocker.NONE
        );
        scheduler.submit(claim, project);
        scheduler.submit(claim, project);
        assertEquals(1, scheduler.pendingCount(claim),
                "identical projectId must not be double-queued");
    }

    @Test
    void resetDropsEverything() {
        BannerModSettlementProjectScheduler scheduler = BannerModSettlementProjectScheduler.detached();
        UUID claim = UUID.randomUUID();
        scheduler.submit(claim, ProjectTestFactory.general(10, 5));
        scheduler.cancel(UUID.randomUUID(), ProjectCancellationReason.MANUAL);

        scheduler.reset();

        assertEquals(0, scheduler.pendingCount(claim));
        assertTrue(scheduler.snapshot(claim).isEmpty());
    }
}
