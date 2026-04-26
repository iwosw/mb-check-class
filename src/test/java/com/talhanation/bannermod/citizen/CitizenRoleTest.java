package com.talhanation.bannermod.citizen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CitizenRoleTest {

    @Test
    void legacyRecruitAliasMapsToControlledRecruit() {
        assertEquals(CitizenRole.CONTROLLED_RECRUIT, CitizenRole.fromLegacy(CitizenRole.RECRUIT));
    }

    @Test
    void legacyWorkerAliasMapsToControlledWorker() {
        assertEquals(CitizenRole.CONTROLLED_WORKER, CitizenRole.fromLegacy(CitizenRole.WORKER));
    }

    @Test
    void nonLegacyRoleReturnsAsIs() {
        assertEquals(CitizenRole.MILITIA, CitizenRole.fromLegacy(CitizenRole.MILITIA));
    }
}
