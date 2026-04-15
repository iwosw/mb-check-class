package com.talhanation.recruits.network;

import com.talhanation.recruits.testsupport.CommandTargetingFixtures;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTargetingTest {

    @Test
    void selectsNearbyOwnedRecruitsInRequestedGroup() {
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                CommandTargetingFixtures.OWNER_UUID,
                null,
                CommandTargetingFixtures.GROUP_ALPHA_UUID,
                CommandTargetingFixtures.sampleNearbyRecruits()
        );

        assertTrue(selection.isSuccess());
        assertEquals(CommandTargeting.Failure.NONE, selection.failure());
        assertEquals(List.of(CommandTargetingFixtures.ALPHA_NEAR_UUID), selection.recruits().stream().map(CommandTargeting.RecruitSnapshot::recruitUuid).toList());
    }

    @Test
    void excludesOutOfRadiusForeignOwnedAndNonListeningRecruits() {
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                CommandTargetingFixtures.OWNER_UUID,
                null,
                CommandTargetingFixtures.GROUP_ALPHA_UUID,
                CommandTargetingFixtures.sampleNearbyRecruits()
        );

        assertFalse(selection.recruits().stream().anyMatch(recruit -> recruit.recruitUuid().equals(CommandTargetingFixtures.ALPHA_FAR_UUID)));
        assertFalse(selection.recruits().stream().anyMatch(recruit -> recruit.recruitUuid().equals(CommandTargetingFixtures.FOREIGN_NEAR_UUID)));
        assertFalse(selection.recruits().stream().anyMatch(recruit -> recruit.recruitUuid().equals(CommandTargetingFixtures.QUIET_NEAR_UUID)));
    }

    @Test
    void invalidSingleRecruitLookupsReturnSafeEmptySelections() {
        CommandTargeting.SingleRecruitSelection missingRecruit = CommandTargeting.forSingleRecruit(
                CommandTargetingFixtures.OWNER_UUID,
                null,
                CommandTargetingFixtures.sampleNearbyRecruits()
        );
        CommandTargeting.SingleRecruitSelection foreignRecruit = CommandTargeting.forSingleRecruit(
                CommandTargetingFixtures.OWNER_UUID,
                CommandTargetingFixtures.FOREIGN_NEAR_UUID,
                CommandTargetingFixtures.sampleNearbyRecruits()
        );

        assertFalse(missingRecruit.isSuccess());
        assertTrue(missingRecruit.recruit().isEmpty());
        assertEquals(CommandTargeting.Failure.INVALID_RECRUIT, missingRecruit.failure());
        assertFalse(foreignRecruit.isSuccess());
        assertTrue(foreignRecruit.recruit().isEmpty());
        assertEquals(CommandTargeting.Failure.NOT_OWNED_BY_SENDER, foreignRecruit.failure());
    }

    @Test
    void sameTeamPlayersCanSelectNearbyOwnedRecruitsWithoutBecomingOwners() {
        CommandTargeting.GroupCommandSelection selection = CommandTargeting.forGroupCommand(
                CommandTargetingFixtures.ALLIED_UUID,
                CommandTargetingFixtures.TEAM_ALPHA,
                CommandTargetingFixtures.GROUP_ALPHA_UUID,
                CommandTargetingFixtures.sampleNearbyRecruits()
        );

        assertTrue(selection.isSuccess());
        assertEquals(List.of(CommandTargetingFixtures.ALPHA_NEAR_UUID), selection.recruits().stream().map(CommandTargeting.RecruitSnapshot::recruitUuid).toList());
    }
}
