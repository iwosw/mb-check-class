package com.talhanation.bannermod.settlement;

public enum BannerModSettlementResidentAssignmentState {
    NOT_APPLICABLE,
    UNASSIGNED,
    ASSIGNED_LOCAL_BUILDING,
    ASSIGNED_MISSING_BUILDING;

    public static BannerModSettlementResidentAssignmentState fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return NOT_APPLICABLE;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return NOT_APPLICABLE;
        }
    }
}
