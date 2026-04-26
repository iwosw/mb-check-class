package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.UUID;

public record BuildingValidationRequest(
        UUID settlementId,
        BuildingType type,
        BlockPos anchorPos,
        List<ZoneSelection> zones,
        boolean enforceOverlapChecks
) {
    public BuildingValidationRequest {
        settlementId = settlementId == null ? new UUID(0L, 0L) : settlementId;
        type = type == null ? BuildingType.HOUSE : type;
        anchorPos = anchorPos == null ? BlockPos.ZERO : anchorPos;
        zones = List.copyOf(zones == null ? List.of() : zones);
    }

    public BuildingValidationRequest(UUID settlementId,
                                     BuildingType type,
                                     BlockPos anchorPos,
                                     List<ZoneSelection> zones) {
        this(settlementId, type, anchorPos, zones, true);
    }
}
