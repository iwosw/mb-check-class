package com.talhanation.bannermod.citizen;

public interface CitizenRoleController {

    CitizenRole role();

    default void onCitizenReady(CitizenRoleContext context) {
    }

    default void onRecoveredControl(CitizenRoleContext context) {
    }

    default void onBoundWorkAreaRemembered(CitizenRoleContext context) {
    }

    static CitizenRoleController noop(CitizenRole role) {
        return () -> role;
    }
}
