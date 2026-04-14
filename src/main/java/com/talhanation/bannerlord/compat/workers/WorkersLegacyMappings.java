package com.talhanation.bannerlord.compat.workers;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

public class WorkersLegacyMappings {

    @SubscribeEvent
    public void onMissingMappings(MissingMappingsEvent event) {
        remap(event, ForgeRegistries.Keys.ENTITY_TYPES, ForgeRegistries.ENTITY_TYPES);
        remap(event, ForgeRegistries.Keys.ITEMS, ForgeRegistries.ITEMS);
        remap(event, ForgeRegistries.Keys.BLOCKS, ForgeRegistries.BLOCKS);
        remap(event, ForgeRegistries.Keys.POI_TYPES, ForgeRegistries.POI_TYPES);
        remap(event, ForgeRegistries.Keys.VILLAGER_PROFESSIONS, ForgeRegistries.VILLAGER_PROFESSIONS);
    }

    private static <T> void remap(MissingMappingsEvent event,
                                  ResourceKey<? extends Registry<T>> registryKey,
                                  IForgeRegistry<T> registry) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(registryKey, WorkersRuntime.LEGACY_MOD_ID)) {
            ResourceLocation targetId = WorkersRuntime.migrateLegacyId(mapping.getKey());
            T target = registry.getValue(targetId);
            if (target != null) {
                mapping.remap(target);
                WorkersRuntime.logger().info("Remapped legacy {} id {} -> {}", registry.getRegistryName(), mapping.getKey(), targetId);
            }
        }
    }
}
