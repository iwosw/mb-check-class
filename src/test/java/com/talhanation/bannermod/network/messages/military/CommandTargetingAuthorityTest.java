package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandHierarchy;
import com.talhanation.bannermod.army.command.CommandRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTargetingAuthorityTest {
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000901");
    private static final UUID TEAMMATE = UUID.fromString("00000000-0000-0000-0000-000000000902");
    private static final UUID OUTSIDER = UUID.fromString("00000000-0000-0000-0000-000000000903");
    private static final UUID RECRUIT = UUID.fromString("00000000-0000-0000-0000-000000000904");
    private static final UUID GROUP = UUID.fromString("00000000-0000-0000-0000-000000000905");

    @Test
    void canonicalRolesCoverOwnerTeammateAdminAndUnsupportedNationAuthority() {
        assertEquals(CommandRole.OWNER,
                CommandHierarchy.roleFor(OWNER, null, false, OWNER, null, true));
        assertEquals(CommandRole.TEAMMATE,
                CommandHierarchy.roleFor(TEAMMATE, "red", false, OWNER, "red", true));
        assertEquals(CommandRole.ADMIN,
                CommandHierarchy.roleFor(OUTSIDER, null, true, OWNER, null, true));

        // Nation-level recruit command authority is explicitly unsupported unless represented by owner/team/admin.
        assertEquals(CommandRole.NONE,
                CommandHierarchy.roleFor(OUTSIDER, null, false, OWNER, null, true));
    }

    @Test
    void groupTargetingUsesSameCanonicalRoles() {
        List<CommandTargeting.RecruitSnapshot> recruits = List.of(snapshot("red"));

        assertTrue(CommandTargeting.forGroupCommand(OWNER, null, false, GROUP, recruits).isSuccess());
        assertEquals(1, CommandTargeting.forGroupCommand(OWNER, null, false, GROUP, recruits).recruits().size());
        assertEquals(1, CommandTargeting.forGroupCommand(TEAMMATE, "red", false, GROUP, recruits).recruits().size());
        assertEquals(1, CommandTargeting.forGroupCommand(OUTSIDER, null, true, GROUP, recruits).recruits().size());
        assertEquals(0, CommandTargeting.forGroupCommand(OUTSIDER, null, false, GROUP, recruits).recruits().size());
    }

    @Test
    void singleRecruitTargetingUsesSameCanonicalRoles() {
        List<CommandTargeting.RecruitSnapshot> recruits = List.of(snapshot("red"));

        assertTrue(CommandTargeting.forSingleRecruit(OWNER, null, false, RECRUIT, recruits).isSuccess());
        assertTrue(CommandTargeting.forSingleRecruit(TEAMMATE, "red", false, RECRUIT, recruits).isSuccess());
        assertTrue(CommandTargeting.forSingleRecruit(OUTSIDER, null, true, RECRUIT, recruits).isSuccess());
        assertEquals(CommandTargeting.Failure.NOT_AUTHORIZED,
                CommandTargeting.forSingleRecruit(OUTSIDER, null, false, RECRUIT, recruits).failure());
    }

    private static CommandTargeting.RecruitSnapshot snapshot(String teamId) {
        return new CommandTargeting.RecruitSnapshot(
                RECRUIT,
                OWNER,
                GROUP,
                teamId,
                true,
                true,
                true,
                4.0D
        );
    }
}
