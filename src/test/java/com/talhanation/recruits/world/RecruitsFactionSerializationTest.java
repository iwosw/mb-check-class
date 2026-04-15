package com.talhanation.recruits.world;

import com.talhanation.recruits.testsupport.NbtRoundTripAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecruitsFactionSerializationTest {

    @Test
    void preservesImportantFieldsAcrossNbtRoundTrip() {
        RecruitsFaction faction = RecruitsFixtures.sampleFaction();
        RecruitsFaction restored = NbtRoundTripAssertions.assertFactionRoundTrip(faction);

        assertEquals("Test Faction", restored.getTeamDisplayName());
        assertEquals(RecruitsFixtures.LEADER_UUID, restored.getTeamLeaderUUID());
        assertEquals(2, restored.getJoinRequests().size());
        assertEquals(2, restored.getMembers().size());
    }
}
