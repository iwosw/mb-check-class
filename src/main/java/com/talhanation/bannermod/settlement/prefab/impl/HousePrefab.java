package com.talhanation.bannermod.settlement.prefab.impl;

import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.persistence.civilian.StructureTemplateLoader;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;

/**
 * Earth-covered starter house. No embedded work-area — this is a home. Auto-staffing treats
 * {@link BuildingPrefabProfession#NONE} as "no worker needed".
 */
public final class HousePrefab implements BuildingPrefab {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("bannermod", "house");
    private static final String TEMPLATE_RESOURCE = "assets/bannermod/structures/zemlyanka.nbt";
    private static final CompoundTag TEMPLATE = loadTemplate();

    private static final BuildingPrefabDescriptor DESCRIPTOR = new BuildingPrefabDescriptor(
            ID, "bannermod.prefab.house.name", "bannermod.prefab.house.description",
            TEMPLATE.getInt("width"), TEMPLATE.getInt("height"), TEMPLATE.getInt("depth"),
            BuildingPrefabProfession.NONE, "minecraft:oak_door");

    @Override public BuildingPrefabDescriptor descriptor() { return DESCRIPTOR; }

    @Override
    public CompoundTag buildStructureNBT(Direction facing) {
        return TEMPLATE.copy();
    }

    private static CompoundTag loadTemplate() {
        try (InputStream input = HousePrefab.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
            CompoundTag template = StructureTemplateLoader.loadTemplate(input, "zemlyanka.nbt", WorkersRuntime::migrateStructureNbt);
            if (template == null || !template.contains("blocks")) {
                throw new IllegalStateException("Failed to load house template from " + TEMPLATE_RESOURCE);
            }
            return template;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load house template from " + TEMPLATE_RESOURCE, e);
        }
    }
}
