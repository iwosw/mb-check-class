package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaimAuthorityStatusTest {

    @Test
    void nullClaimIsUnclaimed() {
        assertEquals(ClaimAuthorityStatus.UNCLAIMED, ClaimAuthorityStatus.classify(null, null));
    }

    @Test
    void claimWithoutOwnerIsUnclaimed() {
        assertEquals(ClaimAuthorityStatus.UNCLAIMED, ClaimAuthorityStatus.classify(null, new RecruitsClaim("Camp", null)));
    }

    @Test
    void matchingTeamIsFriendly() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000011");
        assertEquals(ClaimAuthorityStatus.FRIENDLY, ClaimAuthorityStatus.classify(owner.toString(), new RecruitsClaim("Keep", owner)));
    }

    @Test
    void nonMatchingTeamIsHostile() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000011");
        assertEquals(ClaimAuthorityStatus.HOSTILE, ClaimAuthorityStatus.classify("other-team", new RecruitsClaim("Keep", owner)));
    }

    @Test
    void boundaryFeedbackKeysStayMappedByTerritory() {
        assertEquals("actionbar.bannermod.claim_boundary.friendly", ClaimAuthorityStatus.FRIENDLY.boundaryMessageKey());
        assertEquals("actionbar.bannermod.claim_boundary.hostile", ClaimAuthorityStatus.HOSTILE.boundaryMessageKey());
        assertEquals("actionbar.bannermod.claim_boundary.unclaimed", ClaimAuthorityStatus.UNCLAIMED.boundaryMessageKey());
    }
}
