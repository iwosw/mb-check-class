package com.talhanation.bannermod.settlement.prefab;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory map of BuildArea UUIDs → prefab id, used by the auto-staffing hook to find
 * out which prefab a completed BuildArea originated from (so it knows which worker
 * profession to spawn).
 *
 * <p>Entries live only for as long as the BuildArea is not yet completed; once the
 * staffing hook has consumed the entry it is removed.</p>
 */
public final class PrefabBuildAreaTracker {
    private static final Map<UUID, ResourceLocation> PREFAB_BY_BUILDAREA = new ConcurrentHashMap<>();

    private PrefabBuildAreaTracker() {
    }

    public static void markPrefabBuildArea(UUID buildAreaUuid, ResourceLocation prefabId) {
        if (buildAreaUuid == null || prefabId == null) {
            return;
        }
        PREFAB_BY_BUILDAREA.put(buildAreaUuid, prefabId);
    }

    public static Optional<ResourceLocation> prefabFor(UUID buildAreaUuid) {
        if (buildAreaUuid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PREFAB_BY_BUILDAREA.get(buildAreaUuid));
    }

    public static Optional<ResourceLocation> consume(UUID buildAreaUuid) {
        if (buildAreaUuid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PREFAB_BY_BUILDAREA.remove(buildAreaUuid));
    }

    /** Visible for tests. */
    public static Map<UUID, ResourceLocation> snapshot() {
        return Collections.unmodifiableMap(PREFAB_BY_BUILDAREA);
    }

    /** Visible for tests. */
    public static void clearForTest() {
        PREFAB_BY_BUILDAREA.clear();
    }
}
