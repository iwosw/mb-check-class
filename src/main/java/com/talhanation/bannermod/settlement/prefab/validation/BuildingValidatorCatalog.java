package com.talhanation.bannermod.settlement.prefab.validation;

import com.talhanation.bannermod.settlement.prefab.validation.impl.AnimalPenValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.BarracksValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.FarmValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.FishingDockValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.HouseValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.LumberCampValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.MarketStallValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.MineValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.PastureValidator;
import com.talhanation.bannermod.settlement.prefab.validation.impl.StorageValidator;

/**
 * Registers every bundled {@link BuildingValidator} against a
 * {@link BuildingValidatorRegistry}. Ordering is insertion order; the registry keeps
 * only one entry per prefab id (last wins).
 */
public final class BuildingValidatorCatalog {
    private BuildingValidatorCatalog() {
    }

    public static void registerDefaults(BuildingValidatorRegistry registry) {
        registry.register(new FarmValidator());
        registry.register(new LumberCampValidator());
        registry.register(new MineValidator());
        registry.register(new PastureValidator());
        registry.register(new AnimalPenValidator());
        registry.register(new FishingDockValidator());
        registry.register(new MarketStallValidator());
        registry.register(new StorageValidator());
        registry.register(new HouseValidator());
        registry.register(new BarracksValidator());
    }
}
