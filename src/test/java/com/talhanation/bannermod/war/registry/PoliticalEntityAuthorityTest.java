package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticalEntityAuthorityTest {

    private static final UUID LEADER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CO_LEADER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static PoliticalEntityRecord record(UUID leader) {
        return new PoliticalEntityRecord(
                UUID.fromString("00000000-0000-0000-0000-0000000000aa"),
                "Test",
                PoliticalEntityStatus.SETTLEMENT,
                leader,
                List.of(),
                new BlockPos(0, 64, 0),
                "",
                "",
                "",
                "",
                0L
        );
    }

    private static PoliticalEntityRecord record(UUID leader, GovernmentForm form) {
        return new PoliticalEntityRecord(
                UUID.fromString("00000000-0000-0000-0000-0000000000aa"),
                "Test",
                PoliticalEntityStatus.SETTLEMENT,
                leader,
                List.of(CO_LEADER),
                new BlockPos(0, 64, 0),
                "",
                "",
                "",
                "",
                0L,
                form
        );
    }

    @Test
    void leaderIsAuthorized() {
        assertTrue(PoliticalEntityAuthority.isLeaderOrOp(LEADER, false, record(LEADER)));
    }

    @Test
    void nonLeaderWithoutOpIsRejected() {
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(OTHER, false, record(LEADER)));
    }

    @Test
    void opPrivilegeOverridesLeadership() {
        assertTrue(PoliticalEntityAuthority.isLeaderOrOp(OTHER, true, record(LEADER)));
    }

    @Test
    void nullActorRejectedWithoutOp() {
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(null, false, record(LEADER)));
    }

    @Test
    void nullRecordIsAlwaysRejected() {
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(LEADER, true, null));
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(LEADER, false, null));
    }

    @Test
    void sharedActionPolicyAllowsRepublicCoLeadersOnly() {
        assertTrue(PoliticalEntityAuthority.canAct(CO_LEADER, false, record(LEADER, GovernmentForm.REPUBLIC)));
        assertFalse(PoliticalEntityAuthority.canAct(CO_LEADER, false, record(LEADER, GovernmentForm.MONARCHY)));
    }

    @Test
    void strictLeaderPolicyNeverAllowsCoLeaders() {
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(CO_LEADER, false, record(LEADER, GovernmentForm.REPUBLIC)));
        assertFalse(PoliticalEntityAuthority.isLeaderOrOp(CO_LEADER, false, record(LEADER, GovernmentForm.MONARCHY)));
    }

    @Test
    void denialReasonDistinguishesLeadershipAndGovernmentForm() {
        assertEquals(PoliticalEntityAuthority.DENIAL_CO_LEADER_MONARCHY_KEY,
                PoliticalEntityAuthority.denialReasonKey(CO_LEADER, false, record(LEADER, GovernmentForm.MONARCHY)));
        assertEquals(PoliticalEntityAuthority.DENIAL_OUTSIDER_REPUBLIC_KEY,
                PoliticalEntityAuthority.denialReasonKey(OTHER, false, record(LEADER, GovernmentForm.REPUBLIC)));
        assertEquals(PoliticalEntityAuthority.DENIAL_LEADER_ONLY_KEY,
                PoliticalEntityAuthority.denialReasonKey(OTHER, false, record(LEADER)));
        assertEquals("gui.bannermod.war.denial.allowed",
                PoliticalEntityAuthority.denialReasonKey(LEADER, false, record(LEADER)));
    }
}
