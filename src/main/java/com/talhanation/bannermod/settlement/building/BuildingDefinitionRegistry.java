package com.talhanation.bannermod.settlement.building;

import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class BuildingDefinitionRegistry {
    private final Map<BuildingType, BuildingDefinition> definitions = new EnumMap<>(BuildingType.class);

    public BuildingDefinitionRegistry() {
        registerDefaults();
    }

    public Optional<BuildingDefinition> get(BuildingType type) {
        return Optional.ofNullable(this.definitions.get(type));
    }

    public void register(BuildingDefinition definition) {
        if (definition == null) {
            return;
        }
        this.definitions.put(definition.type(), definition);
    }

    private void registerDefaults() {
        register(new BuildingDefinition(
                BuildingType.STARTER_FORT,
                Set.<Block>of(),
                Set.of(ZoneRole.AUTHORITY_POINT, ZoneRole.INTERIOR),
                ValidationProfile.FORT
        ));
        register(new BuildingDefinition(
                BuildingType.HOUSE,
                Set.<Block>of(),
                Set.of(ZoneRole.INTERIOR, ZoneRole.SLEEPING),
                ValidationProfile.HOUSE
        ));
        register(new BuildingDefinition(
                BuildingType.FARM,
                Set.<Block>of(),
                Set.of(ZoneRole.WORK_ZONE),
                ValidationProfile.FARM
        ));
        register(new BuildingDefinition(
                BuildingType.MINE,
                Set.<Block>of(),
                Set.of(ZoneRole.WORK_ZONE),
                ValidationProfile.MINE
        ));
        register(new BuildingDefinition(
                BuildingType.LUMBER_CAMP,
                Set.<Block>of(),
                Set.of(ZoneRole.WORK_ZONE),
                ValidationProfile.LUMBER
        ));
        register(new BuildingDefinition(
                BuildingType.SMITHY,
                Set.<Block>of(),
                Set.of(ZoneRole.INTERIOR, ZoneRole.WORK_ZONE),
                ValidationProfile.SMITHY
        ));
        register(new BuildingDefinition(
                BuildingType.STORAGE,
                Set.<Block>of(),
                Set.of(ZoneRole.STORAGE),
                ValidationProfile.STORAGE
        ));
        register(new BuildingDefinition(
                BuildingType.ARCHITECT_WORKSHOP,
                Set.<Block>of(),
                Set.of(ZoneRole.INTERIOR, ZoneRole.WORK_ZONE),
                ValidationProfile.ARCHITECT
        ));
        register(new BuildingDefinition(
                BuildingType.BARRACKS,
                Set.<Block>of(),
                Set.of(ZoneRole.INTERIOR, ZoneRole.SLEEPING, ZoneRole.STORAGE),
                ValidationProfile.HOUSE
        ));
    }
}
