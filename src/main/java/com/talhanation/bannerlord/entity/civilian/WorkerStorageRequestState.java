package com.talhanation.bannerlord.entity.civilian;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.Objects;

public class WorkerStorageRequestState {

    private PendingComplaint pendingComplaint;
    private ReservationHandle reservationHandle;
    private boolean released;

    public void recordPendingComplaint(String reasonToken, String message) {
        if (reasonToken == null || message == null) {
            return;
        }

        PendingComplaint nextComplaint = new PendingComplaint(reasonToken, message);
        if (!Objects.equals(this.pendingComplaint, nextComplaint)) {
            this.pendingComplaint = nextComplaint;
            this.released = false;
        }
    }

    public PendingComplaint releasePendingComplaint() {
        if (this.pendingComplaint == null || this.released) {
            return null;
        }

        this.released = true;
        return this.pendingComplaint;
    }

    public PendingComplaint peekPendingComplaint() {
        return this.pendingComplaint;
    }

    public void clear() {
        this.pendingComplaint = null;
        this.reservationHandle = null;
        this.released = false;
    }

    public boolean hasPendingComplaint() {
        return this.pendingComplaint != null;
    }

    public void setReservation(@Nullable UUID reservationId, @Nullable UUID endpointId, @Nullable String itemId, int count) {
        if (reservationId == null || endpointId == null || itemId == null || itemId.isBlank() || count <= 0) {
            this.reservationHandle = null;
            return;
        }
        this.reservationHandle = new ReservationHandle(reservationId, endpointId, itemId, count);
    }

    @Nullable
    public ReservationHandle getReservation() {
        return this.reservationHandle;
    }

    public void clearReservation() {
        this.reservationHandle = null;
    }

    public record PendingComplaint(String reasonToken, String message) {
    }

    public record ReservationHandle(UUID reservationId, UUID endpointId, String itemId, int count) {
    }
}
