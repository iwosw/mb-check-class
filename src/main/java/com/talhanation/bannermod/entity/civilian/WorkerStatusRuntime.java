package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

final class WorkerStatusRuntime {
    private final AbstractWorkerEntity worker;
    private final WorkerControlStatus workStatus = new WorkerControlStatus();

    WorkerStatusRuntime(AbstractWorkerEntity worker) {
        this.worker = worker;
    }

    void reportReason(WorkerControlStatus.Kind kind, String reasonToken, @Nullable Component message) {
        if (this.worker.level().isClientSide() || message == null || this.worker.getOwner() == null) {
            return;
        }

        if (this.workStatus.shouldNotify(kind, reasonToken)) {
            this.worker.getOwner().sendSystemMessage(message);
        }
    }

    void clearWorkStatus() {
        this.workStatus.clear();
        this.worker.clearPendingStorageComplaint();
    }

    WorkerControlStatus workStatus() {
        return this.workStatus;
    }

    void resetRecoveredControlState() {
        AbstractWorkAreaEntity workArea = this.worker.getCurrentWorkArea();
        if (workArea != null) {
            workArea.setBeingWorkedOn(false);
        }

        this.worker.getNavigation().stop();
        this.worker.clearMovePos();
        this.worker.clearHoldPos();
        this.worker.setTarget(null);
        this.worker.setFollowState(0);
        this.worker.neededItems.clear();
        this.worker.forcedDeposit = false;
        this.worker.lastStorage = null;
        this.worker.clearActiveCourierTask();
        this.clearWorkStatus();
        this.worker.clearCurrentWorkAreaForRecovery();
    }
}
