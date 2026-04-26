package com.talhanation.bannermod.citizen;

import com.talhanation.bannermod.entity.citizen.AbstractCitizenEntity;

public interface CitizenRoleController {

    CitizenRole role();

    default void onCitizenReady(CitizenRoleContext context) {
    }

    default void onRecoveredControl(CitizenRoleContext context) {
    }

    default void onBoundWorkAreaRemembered(CitizenRoleContext context) {
    }

    default void onServerAiStep(AbstractCitizenEntity citizen) {
    }

    default void onServerTick(AbstractCitizenEntity citizen) {
    }

    static CitizenRoleController noop(CitizenRole role) {
        return () -> role;
    }
}
