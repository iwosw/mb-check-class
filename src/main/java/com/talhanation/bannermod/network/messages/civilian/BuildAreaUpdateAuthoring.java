package com.talhanation.bannermod.network.messages.civilian;

public final class BuildAreaUpdateAuthoring {

    private BuildAreaUpdateAuthoring() {
    }

    public static WorkAreaAuthoringRules.Decision authorize(boolean areaExists, WorkAreaAuthoringRules.AccessLevel accessLevel) {
        return WorkAreaAuthoringRules.modifyDecision(areaExists, accessLevel);
    }
}
