package com.talhanation.bannermod.settlement.prefab;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-process catalogue of all {@link BuildingPrefab}s available for the
 * "place building" GUI. Populated once during mod setup via
 * {@link BuildingPrefabCatalog#registerDefaults(BuildingPrefabRegistry)}.
 */
public final class BuildingPrefabRegistry {
    private static final BuildingPrefabRegistry INSTANCE = new BuildingPrefabRegistry();

    private final Map<ResourceLocation, BuildingPrefab> prefabs = new LinkedHashMap<>();
    private boolean defaultsLoaded;

    private BuildingPrefabRegistry() {
    }

    public static BuildingPrefabRegistry instance() {
        return INSTANCE;
    }

    public synchronized void register(BuildingPrefab prefab) {
        Objects.requireNonNull(prefab, "prefab");
        Objects.requireNonNull(prefab.id(), "prefab.id()");
        prefabs.put(prefab.id(), prefab);
    }

    public synchronized Optional<BuildingPrefab> lookup(ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(prefabs.get(id));
    }

    public synchronized List<BuildingPrefab> all() {
        return List.copyOf(prefabs.values());
    }

    public synchronized Map<ResourceLocation, BuildingPrefab> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(prefabs));
    }

    public synchronized int size() {
        return prefabs.size();
    }

    public synchronized void ensureDefaultsLoaded() {
        if (defaultsLoaded) {
            return;
        }
        defaultsLoaded = true;
        BuildingPrefabCatalog.registerDefaults(this);
    }

    /** Visible to tests only. */
    public synchronized void clearForTest() {
        prefabs.clear();
        defaultsLoaded = false;
    }
}
