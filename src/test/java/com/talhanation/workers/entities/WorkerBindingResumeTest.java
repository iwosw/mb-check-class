package com.talhanation.workers.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerBindingResumeTest {

    @Test
    void matchingBindingGetsLargePriorityBoost() {
        UUID bound = UUID.randomUUID();

        assertEquals(WorkerBindingResume.BOUND_AREA_PRIORITY_BOOST,
                WorkerBindingResume.priorityBoost(bound, bound));
        assertEquals(0, WorkerBindingResume.priorityBoost(bound, UUID.randomUUID()));
        assertEquals(0, WorkerBindingResume.priorityBoost(null, bound));
    }

    @Test
    void prioritizeBoundFirstMovesBoundCandidateAheadOfOthers() {
        Candidate first = new Candidate(UUID.randomUUID(), "first");
        Candidate bound = new Candidate(UUID.randomUUID(), "bound");
        Candidate third = new Candidate(UUID.randomUUID(), "third");
        List<Candidate> candidates = new ArrayList<>(List.of(first, third, bound));

        WorkerBindingResume.prioritizeBoundFirst(candidates, bound.uuid(), Candidate::uuid);

        assertEquals(bound, candidates.get(0));
        assertTrue(candidates.contains(first));
        assertTrue(candidates.contains(third));
    }

    private record Candidate(UUID uuid, String name) {
    }
}
