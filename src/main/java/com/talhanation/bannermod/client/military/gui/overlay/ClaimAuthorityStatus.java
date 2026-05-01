package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;

import javax.annotation.Nullable;
import java.util.UUID;

public enum ClaimAuthorityStatus {
    FRIENDLY("gui.bannermod.claim_overlay.authority.friendly", "gui.bannermod.claim_overlay.compact.friendly", "actionbar.bannermod.claim_boundary.friendly", 0xFF77DD77, 0x226633, 0x80102010),
    HOSTILE("gui.bannermod.claim_overlay.authority.hostile", "gui.bannermod.claim_overlay.compact.hostile", "actionbar.bannermod.claim_boundary.hostile", 0xFFFF7777, 0x993333, 0x80141010),
    UNCLAIMED("gui.bannermod.claim_overlay.authority.unclaimed", "gui.bannermod.claim_overlay.compact.unclaimed", "actionbar.bannermod.claim_boundary.unclaimed", 0xFFBBBBBB, 0x777777, 0x70101010);

    private final String labelKey;
    private final String compactLabelKey;
    private final String boundaryMessageKey;
    private final int textColor;
    private final int chromeColor;
    private final int backgroundColor;

    ClaimAuthorityStatus(String labelKey, String compactLabelKey, String boundaryMessageKey, int textColor, int chromeColor, int backgroundColor) {
        this.labelKey = labelKey;
        this.compactLabelKey = compactLabelKey;
        this.boundaryMessageKey = boundaryMessageKey;
        this.textColor = textColor;
        this.chromeColor = chromeColor;
        this.backgroundColor = backgroundColor;
    }

    public String labelKey() {
        return labelKey;
    }

    public String compactLabelKey() {
        return compactLabelKey;
    }

    public String boundaryMessageKey() {
        return boundaryMessageKey;
    }

    public int textColor() {
        return textColor;
    }

    public int chromeColor() {
        return chromeColor;
    }

    public int backgroundColor() {
        return backgroundColor;
    }

    public static ClaimAuthorityStatus classify(@Nullable String playerTeamName, @Nullable RecruitsClaim claim) {
        return classify(null, playerTeamName, claim, null);
    }

    public static ClaimAuthorityStatus classify(@Nullable UUID playerUuid, @Nullable String playerTeamName, @Nullable RecruitsClaim claim) {
        return classify(playerUuid, playerTeamName, claim, null);
    }

    public static ClaimAuthorityStatus classify(@Nullable UUID playerUuid,
                                                @Nullable String playerTeamName,
                                                @Nullable RecruitsClaim claim,
                                                @Nullable PoliticalEntityRecord ownerEntity) {
        if (claim == null || claim.getOwnerPoliticalEntityId() == null) {
            return UNCLAIMED;
        }
        if (claim.isTrustedPlayer(playerUuid)) {
            return FRIENDLY;
        }
        if (ownerEntity != null && playerUuid != null) {
            if (playerUuid.equals(ownerEntity.leaderUuid()) || ownerEntity.coLeaderUuids().contains(playerUuid)) {
                return FRIENDLY;
            }
        }
        return claim.getOwnerPoliticalEntityId().toString().equals(playerTeamName) ? FRIENDLY : HOSTILE;
    }
}
