package com.talhanation.bannermod.settlement.prefab;

import com.talhanation.bannermod.settlement.prefab.impl.AnimalPenPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.BarracksPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.FarmPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.FishingDockPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.GatehousePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.HousePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.LumberCampPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MarketStallPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MinePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.PasturePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.StoragePrefab;
import com.talhanation.bannermod.settlement.prefab.impl.TownHallPrefab;

/**
 * Central list of built-in prefabs. Agents are free to add new entries here without
 * touching {@link BuildingPrefabRegistry} wiring.
 *
 * <p>Entries that are not yet implemented should be added as classes under
 * {@code settlement.prefab.impl} and listed here. The registration call is idempotent.</p>
 */
public final class BuildingPrefabCatalog {
    private BuildingPrefabCatalog() {
    }

    public static void registerDefaults(BuildingPrefabRegistry registry) {
        registry.register(new FarmPrefab());
        registry.register(new LumberCampPrefab());
        registry.register(new MinePrefab());
        registry.register(new PasturePrefab());
        registry.register(new AnimalPenPrefab());
        registry.register(new FishingDockPrefab());
        registry.register(new MarketStallPrefab());
        registry.register(new StoragePrefab());
        registry.register(new HousePrefab());
        registry.register(new BarracksPrefab());
        registry.register(new GatehousePrefab());
        registry.register(new TownHallPrefab());
    }
}
