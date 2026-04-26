package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.List;

public record ValidatedBuildingSnapshot(
        BlockPos anchorPos,
        AABB bounds,
        List<ZoneSelection> zones
) {
    public ValidatedBuildingSnapshot {
        anchorPos = anchorPos == null ? BlockPos.ZERO : anchorPos;
        bounds = bounds == null ? new AABB(anchorPos) : bounds;
        zones = List.copyOf(zones == null ? List.of() : zones);
    }
}
