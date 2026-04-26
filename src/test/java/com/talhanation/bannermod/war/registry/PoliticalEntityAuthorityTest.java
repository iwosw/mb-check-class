package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticalEntityAuthorityTest {

    private static final UUID LEADER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

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
}
