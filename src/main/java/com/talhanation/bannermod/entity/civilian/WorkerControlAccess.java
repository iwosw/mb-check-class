package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.citizen.CitizenRoleContext;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.settlement.BannerModSettlementManager;
import com.talhanation.bannermod.settlement.BannerModSettlementService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

final class WorkerControlAccess {
    private final AbstractWorkerEntity worker;
    private final WorkerStatusRuntime statusRuntime;
    private final WorkerRecoveryService recoveryService;
    private UUID boundWorkArea;

    WorkerControlAccess(AbstractWorkerEntity worker) {
        this.worker = worker;
        this.statusRuntime = new WorkerStatusRuntime(worker);
        this.recoveryService = new WorkerRecoveryService(worker, this);
    }

    @Nullable
    UUID getBoundWorkAreaUUID() {
        return this.boundWorkArea;
    }

    void setBoundWorkAreaBinding(@Nullable UUID boundWorkAreaUuid) {
        UUID previous = this.boundWorkArea;
        this.boundWorkArea = boundWorkAreaUuid;
        if ((previous == null && boundWorkAreaUuid == null) || (previous != null && previous.equals(boundWorkAreaUuid))) {
            return;
        }
        refreshSettlementSnapshot();
    }

    void rememberCurrentWorkAreaBinding() {
        AbstractWorkAreaEntity workArea = this.worker.getCurrentWorkArea();
        if (workArea == null) {
            return;
        }

        this.boundWorkArea = workArea.getUUID();
        this.worker.getCitizenRoleController().onBoundWorkAreaRemembered(new CitizenRoleContext(
                this.worker.getCitizenRole(),
                this.worker.getCitizenCore(),
                this.worker,
                null,
                this.boundWorkArea
        ));
        refreshSettlementSnapshot();
    }

    void reportBlockedReason(String reasonToken, @Nullable Component message) {
        this.statusRuntime.reportReason(WorkerControlStatus.Kind.BLOCKED, reasonToken, message);
    }

    void reportIdleReason(String reasonToken, @Nullable Component message) {
        this.statusRuntime.reportReason(WorkerControlStatus.Kind.IDLE, reasonToken, message);
    }

    void clearWorkStatus() {
        this.statusRuntime.clearWorkStatus();
    }

    void resetRecoveredControlState() {
        this.statusRuntime.resetRecoveredControlState();
    }

    boolean recoverControl(Player requester) {
        return this.recoveryService.recoverControl(requester);
    }

    private void refreshSettlementSnapshot() {
        if (!(this.worker.level() instanceof ServerLevel serverLevel) || ClaimEvents.recruitsClaimManager == null) {
            return;
        }
        BannerModSettlementService.refreshClaimAt(
                serverLevel,
                ClaimEvents.recruitsClaimManager,
                BannerModSettlementManager.get(serverLevel),
                BannerModGovernorManager.get(serverLevel),
                this.worker.blockPosition()
        );
    }
}
