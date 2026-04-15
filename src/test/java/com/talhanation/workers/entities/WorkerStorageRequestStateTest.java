package com.talhanation.workers.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerStorageRequestStateTest {

    @Test
    void recordsPendingComplaintWithoutReleasingItImmediately() {
        WorkerStorageRequestState state = new WorkerStorageRequestState();

        state.recordPendingComplaint("farmer_missing_hoe", "Farmer: I need a hoe to keep working.");

        assertTrue(state.hasPendingComplaint());
        assertNotNull(state.releasePendingComplaint());
    }

    @Test
    void releasesStoredComplaintOnlyOnce() {
        WorkerStorageRequestState state = new WorkerStorageRequestState();

        state.recordPendingComplaint("farmer_missing_seeds", "Farmer: I need seeds for this field.");

        WorkerStorageRequestState.PendingComplaint complaint = state.releasePendingComplaint();
        assertNotNull(complaint);
        assertEquals("farmer_missing_seeds", complaint.reasonToken());
        assertEquals("Farmer: I need seeds for this field.", complaint.message());
        assertNull(state.releasePendingComplaint());
    }

    @Test
    void clearRemovesPendingComplaint() {
        WorkerStorageRequestState state = new WorkerStorageRequestState();

        state.recordPendingComplaint("builder_missing_materials", "Builder: I need more building materials.");
        state.clear();

        assertFalse(state.hasPendingComplaint());
        assertNull(state.releasePendingComplaint());
    }
}
