package com.talhanation.bannermod.entity.civilian;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerControlStatusTest {

    @Test
    void storesLatestReasonMessage() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.BLOCKED, "farmer_missing_seeds", "Need wheat seeds"));
        assertEquals(WorkerControlStatus.Kind.BLOCKED, status.kind());
        assertEquals("farmer_missing_seeds", status.reasonToken());
        assertEquals("Need wheat seeds", status.reasonMessage());
    }

    @Test
    void unchangedReasonAndMessageDoNotRetrigger() {
        WorkerControlStatus status = new WorkerControlStatus();

        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "farmer_no_area", "Waiting for a crop area"));
        assertFalse(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "farmer_no_area", "Waiting for a crop area"));
        assertTrue(status.shouldNotify(WorkerControlStatus.Kind.IDLE, "farmer_no_area", "Waiting for seeds"));
    }

    @Test
    void clearDropsMessageState() {
        WorkerControlStatus status = new WorkerControlStatus();

        status.shouldNotify(WorkerControlStatus.Kind.IDLE, "farmer_no_area", "Waiting for a crop area");
        status.clear();

        assertNull(status.kind());
        assertNull(status.reasonToken());
        assertNull(status.reasonMessage());
    }
}
