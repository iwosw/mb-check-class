package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ValidatedBuildingRecord;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface BuildingValidator {
    BuildingValidationResult validate(ServerLevel level, Player player, BuildingValidationRequest request);

    BuildingValidationResult revalidate(ServerLevel level, ValidatedBuildingRecord building);
}
