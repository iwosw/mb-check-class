package com.talhanation.bannermod.settlement.building;

import net.minecraft.world.level.block.Block;

import java.util.Set;

public record BuildingDefinition(
        BuildingType type,
        Set<Block> allowedAnchors,
        Set<ZoneRole> requiredZones,
        ValidationProfile profile
) {
    public BuildingDefinition {
        type = type == null ? BuildingType.HOUSE : type;
        allowedAnchors = Set.copyOf(allowedAnchors == null ? Set.of() : allowedAnchors);
        requiredZones = Set.copyOf(requiredZones == null ? Set.of() : requiredZones);
        profile = profile == null ? ValidationProfile.HOUSE : profile;
    }
}
