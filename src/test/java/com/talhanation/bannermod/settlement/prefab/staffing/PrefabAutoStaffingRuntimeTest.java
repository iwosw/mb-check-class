package com.talhanation.bannermod.settlement.prefab.staffing;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefabProfession;
import com.talhanation.bannermod.settlement.prefab.impl.FarmPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.LumberCampPrefab;
import com.talhanation.bannermod.settlement.prefab.impl.MinePrefab;
import com.talhanation.bannermod.settlement.building.BuildingType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests for {@link PrefabAutoStaffingRuntime} — validate the profession→entity-type
 * mapping table and worker/recruit classification without bootstrapping Minecraft.
 *
 * <p>Note: the actual {@code EntityType} lookups happen through Forge's {@code RegistryObject.get()}
 * which requires a running Forge registry — these tests don't assert concrete EntityType values.
 * They only assert the structural guarantees of the mapping: every enum value is handled, NONE
 * returns null, and worker/recruit classification splits the enum cleanly.</p>
 */
class PrefabAutoStaffingRuntimeTest {

    @Test
    void noneProfessionReturnsNullEntityType() {
        assertNull(PrefabAutoStaffingRuntime.entityTypeFor(BuildingPrefabProfession.NONE),
                "NONE profession must map to null entity type (caller short-circuits on null)");
    }

    @Test
    void nullProfessionReturnsNullEntityType() {
        assertNull(PrefabAutoStaffingRuntime.entityTypeFor(null),
                "null profession must be safely handled and return null");
    }

    @Test
    void isWorkerProfessionHandlesEveryEnumValue() {
        // Every enum value must be classified deterministically (true or false) — no enum
        // value may fall through or throw. This guards against new enum additions not being
        // handled by the classifier switch.
        for (BuildingPrefabProfession profession : BuildingPrefabProfession.values()) {
            boolean result = PrefabAutoStaffingRuntime.isWorkerProfession(profession);
            if (profession == BuildingPrefabProfession.NONE) {
                assertFalse(result, "NONE must not be classified as a worker profession");
            }
        }
    }

    @Test
    void workerAndRecruitPartitionCoversEveryNonNoneProfession() {
        // Every non-NONE profession must be either a worker or a recruit, never both,
        // never neither. This guarantees the spawn pipeline handles the whole enum.
        for (BuildingPrefabProfession profession : BuildingPrefabProfession.values()) {
            if (profession == BuildingPrefabProfession.NONE) {
                continue;
            }
            boolean worker = PrefabAutoStaffingRuntime.isWorkerProfession(profession);
            boolean recruit = isRecruitProfessionName(profession);
            assertTrue(worker ^ recruit,
                    "Profession " + profession + " must be classified as exactly one of worker/recruit");
        }
    }

    private static boolean isRecruitProfessionName(BuildingPrefabProfession p) {
        return p.name().startsWith("RECRUIT_");
    }

    @Test
    void workerProfessionsAreClassifiedAsWorkers() {
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.FARMER));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.LUMBERJACK));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.MINER));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.BUILDER));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.MERCHANT));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.FISHERMAN));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.ANIMAL_FARMER));
        assertTrue(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.SHEPHERD));
    }

    @Test
    void recruitProfessionsAreNotClassifiedAsWorkers() {
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.RECRUIT_SWORDSMAN));
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.RECRUIT_ARCHER));
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.RECRUIT_PIKEMAN));
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.RECRUIT_CROSSBOW));
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.RECRUIT_CAVALRY));
    }

    @Test
    void noneAndNullAreNotWorkerProfessions() {
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(BuildingPrefabProfession.NONE));
        assertFalse(PrefabAutoStaffingRuntime.isWorkerProfession(null));
    }

    @Test
    void onBuildAreaCompletedWithNullArgsDoesNotExplode() {
        // Both null-level and null-buildArea must be tolerated by the guard clause.
        PrefabAutoStaffingRuntime.onBuildAreaCompleted(null, null);
    }

    @Test
    void manualValidatedWorkplacesDeclareVacancyProfessions() {
        assertTrue(PrefabAutoStaffingRuntime.professionForManualBuilding(BuildingType.FARM) == BuildingPrefabProfession.FARMER);
        assertTrue(PrefabAutoStaffingRuntime.professionForManualBuilding(BuildingType.MINE) == BuildingPrefabProfession.MINER);
        assertTrue(PrefabAutoStaffingRuntime.professionForManualBuilding(BuildingType.LUMBER_CAMP) == BuildingPrefabProfession.LUMBERJACK);
        assertTrue(PrefabAutoStaffingRuntime.professionForManualBuilding(BuildingType.ARCHITECT_WORKSHOP) == BuildingPrefabProfession.BUILDER);
        assertTrue(PrefabAutoStaffingRuntime.professionForManualBuilding(BuildingType.STORAGE) == BuildingPrefabProfession.NONE);
    }

    @Test
    void manualValidatedWorkplaceSlotsMatchEquivalentPrefabs() {
        assertTrue(PrefabAutoStaffingRuntime.vacancySlotsForManualBuilding(BuildingType.FARM)
                == PrefabAutoStaffingRuntime.vacancySlotsFor(FarmPrefab.ID, BuildingPrefabProfession.FARMER));
        assertTrue(PrefabAutoStaffingRuntime.vacancySlotsForManualBuilding(BuildingType.MINE)
                == PrefabAutoStaffingRuntime.vacancySlotsFor(MinePrefab.ID, BuildingPrefabProfession.MINER));
        assertTrue(PrefabAutoStaffingRuntime.vacancySlotsForManualBuilding(BuildingType.LUMBER_CAMP)
                == PrefabAutoStaffingRuntime.vacancySlotsFor(LumberCampPrefab.ID, BuildingPrefabProfession.LUMBERJACK));
    }
}
