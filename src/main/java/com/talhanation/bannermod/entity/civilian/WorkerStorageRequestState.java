package com.talhanation.bannermod.entity.civilian;

import java.util.Objects;

public class WorkerStorageRequestState {

    private PendingComplaint pendingComplaint;
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

    public void clear() {
        this.pendingComplaint = null;
        this.released = false;
    }

    public boolean hasPendingComplaint() {
        return this.pendingComplaint != null;
    }

    public record PendingComplaint(String reasonToken, String message) {
    }
}
