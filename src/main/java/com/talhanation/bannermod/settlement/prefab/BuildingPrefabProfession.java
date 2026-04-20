package com.talhanation.bannermod.settlement.prefab;

/**
 * Which BannerMod worker profession a prefab is built for. Used by the auto-staffing hook
 * to pick the right entity type to spawn when a BuildArea of this prefab completes.
 *
 * <p>This intentionally mirrors
 * {@code com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules.WorkerProfession}
 * but stays decoupled from it so prefabs can declare their staffing need without a hard
 * dependency on the settlement-civilian package.</p>
 */
public enum BuildingPrefabProfession {
    NONE,
    FARMER,
    LUMBERJACK,
    MINER,
    BUILDER,
    MERCHANT,
    FISHERMAN,
    ANIMAL_FARMER,
    SHEPHERD,
    RECRUIT_SWORDSMAN,
    RECRUIT_ARCHER,
    RECRUIT_PIKEMAN,
    RECRUIT_CROSSBOW,
    RECRUIT_CAVALRY
}
