package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.citizen.CitizenRoleContext;
import com.talhanation.bannermod.shared.authority.BannerModAuthorityRules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

final class WorkerRecoveryService {
    private final AbstractWorkerEntity worker;
    private final WorkerControlAccess controlAccess;

    WorkerRecoveryService(AbstractWorkerEntity worker, WorkerControlAccess controlAccess) {
        this.worker = worker;
        this.controlAccess = controlAccess;
    }

    boolean recoverControl(Player requester) {
        if (this.worker.level().isClientSide()) {
            return false;
        }

        if (!this.worker.isAlive()) {
            if (requester != null) {
                requester.sendSystemMessage(Component.literal(this.worker.getName().getString() + ": I can't recover right now."));
            }
            return false;
        }

        if (!this.canRequesterRecoverControl(requester)) {
            return false;
        }

        this.resetWorkerControlState();

        if (requester != null) {
            requester.sendSystemMessage(Component.literal(this.worker.getName().getString() + ": control recovered."));
        }

        this.worker.getCitizenRoleController().onRecoveredControl(new CitizenRoleContext(
                this.worker.getCitizenRole(),
                this.worker.getCitizenCore(),
                this.worker,
                requester,
                this.controlAccess.getBoundWorkAreaUUID()
        ));

        return true;
    }

    private boolean canRequesterRecoverControl(Player requester) {
        if (requester == null) {
            return true;
        }

        BannerModAuthorityRules.Decision decision = BannerModAuthorityRules.recoverControlDecision(
                true,
                BannerModAuthorityRules.resolveRelationship(
                        requester.getUUID().equals(this.worker.getOwnerUUID()),
                        false,
                        requester.hasPermissions(2)
                )
        );

        if (BannerModAuthorityRules.isAllowed(decision)) {
            return true;
        }

        requester.sendSystemMessage(Component.literal("You do not control " + this.worker.getName().getString() + "."));
        return false;
    }

    private void resetWorkerControlState() {
        this.controlAccess.resetRecoveredControlState();
    }
}
