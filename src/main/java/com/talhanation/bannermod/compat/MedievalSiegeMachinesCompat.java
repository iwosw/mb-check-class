package com.talhanation.bannermod.compat;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralized optional-compat facts for Magistu's Medieval Siege Machines.
 */
public final class MedievalSiegeMachinesCompat {
    public static final String MOD_ID = "siegemachines";
    public static final String LEGACY_MOD_ID = "siegeweapons";
    private static final List<String> KNOWN_MACHINE_PATHS = List.of(
            "catapult",
            "ballista",
            "trebuchet",
            "mortar",
            "cannon"
    );

    private MedievalSiegeMachinesCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded(LEGACY_MOD_ID);
    }

    public static Map<ResourceLocation, Set<Operation>> detectedMachines() {
        Map<ResourceLocation, Set<Operation>> machines = new LinkedHashMap<>();
        detectNamespace(MOD_ID, machines);
        detectNamespace(LEGACY_MOD_ID, machines);
        return Map.copyOf(machines);
    }

    private static void detectNamespace(String namespace, Map<ResourceLocation, Set<Operation>> machines) {
        for (String path : KNOWN_MACHINE_PATHS) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            if (type != null) {
                machines.put(id, EnumSet.of(Operation.CREW_SEAT, Operation.AIM, Operation.LOAD, Operation.FIRE));
            }
        }
    }

    public static void logDetectedState() {
        if (isLoaded()) {
            BannerModMain.LOGGER.info("Medieval Siege Machines compatibility detected; supported machines={}", detectedMachines());
        }
    }

    public enum Operation {
        CREW_SEAT,
        AIM,
        LOAD,
        FIRE,
        MOVE
    }
}
