package com.talhanation.recruits.world;

import com.talhanation.recruits.testsupport.NbtRoundTripAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitsClaimSerializationTest {

    @Test
    void preservesImportantFieldsAcrossNbtRoundTrip() {
        RecruitsClaim claim = RecruitsFixtures.sampleClaim();
        RecruitsClaim restored = NbtRoundTripAssertions.assertClaimRoundTrip(claim);

        assertEquals("test-faction", restored.getOwnerFactionStringID());
        assertEquals(2, restored.getClaimedChunks().size());
        assertEquals(37, restored.getHealth());
        assertEquals(1.25F, restored.getSiegeSpeedPercent());
        assertTrue(restored.isAdmin);
        assertTrue(restored.isUnderSiege);
    }
}
