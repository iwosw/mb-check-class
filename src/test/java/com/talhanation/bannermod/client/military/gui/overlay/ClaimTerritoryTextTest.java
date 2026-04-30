package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.war.registry.GovernmentForm;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClaimTerritoryTextTest {
    @Test
    void wildernessUsesFallbackLabel() {
        assertEquals("Wilderness", ClaimTerritoryText.territoryName(null, null, "Wilderness"));
    }

    @Test
    void territoryNamePrefersPoliticalEntityName() {
        RecruitsClaim claim = new RecruitsClaim("Northmarch Outpost", UUID.randomUUID());
        claim.setPlayer(new RecruitsPlayerInfo(UUID.randomUUID(), "Captain Rowan"));
        PoliticalEntityRecord entity = new PoliticalEntityRecord(
                claim.getOwnerPoliticalEntityId(),
                "Northmarch",
                PoliticalEntityStatus.STATE,
                UUID.randomUUID(),
                List.of(),
                null,
                "#335577",
                "",
                "",
                "",
                0L,
                GovernmentForm.MONARCHY
        );

        assertEquals("Northmarch", ClaimTerritoryText.territoryName(claim, entity, "Wilderness"));
    }

    @Test
    void territoryNameFallsBackToPlayerNameWhenStateMissing() {
        RecruitsClaim claim = new RecruitsClaim("Northmarch Outpost", UUID.randomUUID());
        claim.setPlayer(new RecruitsPlayerInfo(UUID.randomUUID(), "Captain Rowan"));

        assertEquals("Captain Rowan", ClaimTerritoryText.territoryName(claim, null, "Wilderness"));
    }

    @Test
    void territoryNameFallsBackToClaimNameWhenOwnerNamesMissing() {
        RecruitsClaim claim = new RecruitsClaim("Northmarch Outpost", UUID.randomUUID());

        assertEquals("Northmarch Outpost", ClaimTerritoryText.territoryName(claim, null, "Wilderness"));
    }
}
