package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;

import javax.annotation.Nullable;

final class ClaimTerritoryText {
    private ClaimTerritoryText() {
    }

    static String territoryName(@Nullable RecruitsClaim claim,
                                @Nullable PoliticalEntityRecord ownerEntity,
                                String wildernessLabel) {
        if (claim == null) {
            return wildernessLabel;
        }
        String ownerName = ownerName(claim, ownerEntity);
        return ownerName.isBlank() ? claim.getName() : ownerName;
    }

    static String ownerName(@Nullable RecruitsClaim claim, @Nullable PoliticalEntityRecord ownerEntity) {
        if (ownerEntity != null && !ownerEntity.name().isBlank()) {
            return ownerEntity.name();
        }
        if (claim == null) {
            return "";
        }
        RecruitsPlayerInfo playerInfo = claim.getPlayerInfo();
        if (playerInfo != null && playerInfo.getName() != null && !playerInfo.getName().isBlank()) {
            return playerInfo.getName().trim();
        }
        return claim.getName() == null ? "" : claim.getName().trim();
    }
}
