package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;

import javax.annotation.Nullable;

public enum ClaimAuthorityStatus {
    FRIENDLY("gui.bannermod.claim_overlay.authority.friendly", 0xFF77DD77, 0x226633, 0x80102010),
    HOSTILE("gui.bannermod.claim_overlay.authority.hostile", 0xFFFF7777, 0x993333, 0x80141010),
    UNCLAIMED("gui.bannermod.claim_overlay.authority.unclaimed", 0xFFBBBBBB, 0x777777, 0x70101010);

    private final String labelKey;
    private final int textColor;
    private final int chromeColor;
    private final int backgroundColor;

    ClaimAuthorityStatus(String labelKey, int textColor, int chromeColor, int backgroundColor) {
        this.labelKey = labelKey;
        this.textColor = textColor;
        this.chromeColor = chromeColor;
        this.backgroundColor = backgroundColor;
    }

    public String labelKey() {
        return labelKey;
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
        if (claim == null || claim.getOwnerPoliticalEntityId() == null) {
            return UNCLAIMED;
        }
        return claim.getOwnerPoliticalEntityId().toString().equals(playerTeamName) ? FRIENDLY : HOSTILE;
    }
}
