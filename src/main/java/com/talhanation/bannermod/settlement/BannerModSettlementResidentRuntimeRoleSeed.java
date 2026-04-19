package com.talhanation.bannermod.settlement;

public enum BannerModSettlementResidentRuntimeRoleSeed {
    VILLAGE_LIFE,
    GOVERNANCE,
    LOCAL_LABOR,
    FLOATING_LABOR,
    ORPHANED_LABOR_ASSIGNMENT;

    public static BannerModSettlementResidentRuntimeRoleSeed fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return VILLAGE_LIFE;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return VILLAGE_LIFE;
        }
    }

    public static BannerModSettlementResidentRuntimeRoleSeed defaultFor(BannerModSettlementResidentRole role,
                                                                        BannerModSettlementResidentScheduleSeed scheduleSeed,
                                                                        BannerModSettlementResidentMode residentMode,
                                                                        BannerModSettlementResidentAssignmentState assignmentState) {
        return switch (role) {
            case GOVERNOR_RECRUIT -> GOVERNANCE;
            case VILLAGER -> VILLAGE_LIFE;
            case CONTROLLED_WORKER -> defaultWorkerSeed(scheduleSeed, residentMode, assignmentState);
        };
    }

    private static BannerModSettlementResidentRuntimeRoleSeed defaultWorkerSeed(BannerModSettlementResidentScheduleSeed scheduleSeed,
                                                                                BannerModSettlementResidentMode residentMode,
                                                                                BannerModSettlementResidentAssignmentState assignmentState) {
        if (assignmentState == BannerModSettlementResidentAssignmentState.ASSIGNED_MISSING_BUILDING) {
            return ORPHANED_LABOR_ASSIGNMENT;
        }
        if (assignmentState == BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING
                || scheduleSeed == BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK) {
            return LOCAL_LABOR;
        }
        if (residentMode == BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER) {
            return FLOATING_LABOR;
        }
        return FLOATING_LABOR;
    }
}
