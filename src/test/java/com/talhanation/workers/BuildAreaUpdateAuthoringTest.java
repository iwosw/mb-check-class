package com.talhanation.workers;

import com.talhanation.bannermod.network.messages.civilian.BuildAreaUpdateAuthoring;
import com.talhanation.bannermod.network.messages.civilian.WorkAreaAuthoringRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildAreaUpdateAuthoringTest {

    @Test
    void ownerSameTeamAndAdminAreAllowed() {
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                BuildAreaUpdateAuthoring.authorize(true, WorkAreaAuthoringRules.AccessLevel.OWNER));
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                BuildAreaUpdateAuthoring.authorize(true, WorkAreaAuthoringRules.AccessLevel.SAME_TEAM));
        assertEquals(WorkAreaAuthoringRules.Decision.ALLOW,
                BuildAreaUpdateAuthoring.authorize(true, WorkAreaAuthoringRules.AccessLevel.ADMIN));
    }

    @Test
    void forbiddenAccessReturnsForbidden() {
        assertEquals(WorkAreaAuthoringRules.Decision.FORBIDDEN,
                BuildAreaUpdateAuthoring.authorize(true, WorkAreaAuthoringRules.AccessLevel.FORBIDDEN));
    }

    @Test
    void missingAreaReturnsAreaNotFound() {
        assertEquals(WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND,
                BuildAreaUpdateAuthoring.authorize(false, WorkAreaAuthoringRules.AccessLevel.OWNER));
    }
}
