package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.BuildingValidationState;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRecord;

public final class BuildingInvalidationPolicy {
    private BuildingInvalidationPolicy() {
    }

    public static BuildingValidationState stateForFailedRevalidation(ValidatedBuildingRecord record,
                                                                     BuildingInvalidationReason reason,
                                                                     long nowGameTime) {
        // invalidSinceGameTime() == 0L is the sentinel for "this record is VALID — no invalid
        // window in effect", but a record's state is the authoritative answer. Using state lets
        // 0L be a real game time when the record is already non-VALID (e.g. when nowGameTime is
        // small and prior code stamped invalidSinceGameTime at the moment of first failure).
        long failedSince = record.state() == BuildingValidationState.VALID
                ? nowGameTime
                : record.invalidSinceGameTime();
        long elapsed = Math.max(0L, nowGameTime - failedSince);
        long graceTicks = graceTicks(record.type(), reason);

        if (elapsed >= graceTicks) {
            return BuildingValidationState.INVALID;
        }
        if (record.type() == BuildingType.STARTER_FORT) {
            return BuildingValidationState.SUSPENDED;
        }
        return BuildingValidationState.DEGRADED;
    }

    private static long graceTicks(BuildingType type, BuildingInvalidationReason reason) {
        if (type == BuildingType.STARTER_FORT) {
            return switch (reason) {
                case EXPLOSION -> WorkersServerConfig.settlementFortExplosionGraceTicks();
                case BLOCK_BROKEN, FLUID_CHANGED, BLOCK_PLACED -> WorkersServerConfig.settlementFortGraceTicks();
                default -> WorkersServerConfig.settlementFortGraceTicks();
            };
        }
        if (type == BuildingType.HOUSE) {
            return WorkersServerConfig.settlementHouseGraceTicks();
        }
        if (type == BuildingType.FARM || type == BuildingType.SMITHY || type == BuildingType.STORAGE) {
            return WorkersServerConfig.settlementWorkplaceGraceTicks();
        }
        return WorkersServerConfig.settlementWorkplaceGraceTicks();
    }
}
