package com.talhanation.workers.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerControlStatusTest {

    @Test
    void emitsFirstBlockedTransition() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "missing_storage"));
    }

    @Test
    void suppressesRepeatedBlockedReasonUntilCleared() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "missing_storage"));
        assertFalse(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "missing_storage"));
    }

    @Test
    void emitsWhenKindOrReasonChanges() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "missing_storage"));
        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "waiting_for_area"));
        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "missing_tool"));
    }

    @Test
    void clearResetsNotificationState() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "waiting_for_area"));
        assertFalse(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "waiting_for_area"));

        status.clear();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "waiting_for_area"));
    }
}
