package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.war.registry.GovernmentForm;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimPacketAuthorityTest {
    @Test
    void rejectsNonOwnerWhenNotAdminOrPoliticalAuthority() {
        RecruitsClaim claim = claimOwnedBy(UUID.randomUUID());

        assertFalse(ClaimPacketAuthority.canEditClaim(UUID.randomUUID(), false, claim, null));
    }

    @Test
    void allowsDirectClaimOwner() {
        UUID owner = UUID.randomUUID();
        RecruitsClaim claim = claimOwnedBy(owner);

        assertTrue(ClaimPacketAuthority.canEditClaim(owner, false, claim, null));
    }

    @Test
    void allowsAdminWithoutOwnership() {
        RecruitsClaim claim = claimOwnedBy(UUID.randomUUID());

        assertTrue(ClaimPacketAuthority.canEditClaim(UUID.randomUUID(), true, claim, null));
    }

    @Test
    void allowsPoliticalLeaderAndCoLeader() {
        UUID leader = UUID.randomUUID();
        UUID coLeader = UUID.randomUUID();
        UUID politicalId = UUID.randomUUID();
        RecruitsClaim claim = new RecruitsClaim("state claim", politicalId);
        PoliticalEntityRecord owner = new PoliticalEntityRecord(
                politicalId,
                "State",
                PoliticalEntityStatus.STATE,
                leader,
                List.of(coLeader),
                BlockPos.ZERO,
                "",
                "",
                "",
                "",
                0L
        );

        assertTrue(ClaimPacketAuthority.canEditClaim(leader, false, claim, owner));
        assertTrue(ClaimPacketAuthority.canEditClaim(coLeader, false, claim, owner.withGovernmentForm(GovernmentForm.REPUBLIC)));
    }

    @Test
    void rejectsMalformedInputs() {
        assertFalse(ClaimPacketAuthority.canEditClaim(null, false, claimOwnedBy(UUID.randomUUID()), null));
        assertFalse(ClaimPacketAuthority.canEditClaim(UUID.randomUUID(), false, null, null));
    }

    private static RecruitsClaim claimOwnedBy(UUID owner) {
        RecruitsClaim claim = new RecruitsClaim("claim", null);
        claim.setPlayer(new RecruitsPlayerInfo(owner, "owner"));
        return claim;
    }
}
