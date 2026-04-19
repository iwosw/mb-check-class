package com.talhanation.bannermod.citizen;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenProfessionRegistryTest {

    @Test
    void defaultsRegistryHasNoopControllerForEveryProfession() {
        CitizenProfessionRegistry registry = CitizenProfessionRegistry.defaults();

        assertEquals(CitizenProfession.values().length, registry.size());
        for (CitizenProfession profession : CitizenProfession.values()) {
            CitizenProfessionController controller = registry.lookup(profession);
            assertNotNull(controller, "default controller for " + profession);
            assertEquals(profession, controller.profession());
            assertEquals(profession.coarseRole(), controller.role());
            assertEquals(27, controller.preferredInventorySize());
        }
    }

    @Test
    void registerReplacesNoopWithConcreteController() {
        CitizenProfessionRegistry registry = CitizenProfessionRegistry.defaults();
        CitizenProfessionController noopBefore = registry.lookup(CitizenProfession.FARMER);

        ProbeFarmerController probe = new ProbeFarmerController();
        registry.register(probe);

        CitizenProfessionController after = registry.lookup(CitizenProfession.FARMER);
        assertSame(probe, after);
        assertNotSame(noopBefore, after);
    }

    @Test
    void lookupByLegacyEntityIdResolvesProfessionController() {
        CitizenProfessionRegistry registry = CitizenProfessionRegistry.defaults();
        ProbeFarmerController probe = new ProbeFarmerController();
        registry.register(probe);

        Optional<CitizenProfessionController> byLegacy = registry.lookupByLegacyEntityId("workers:farmer");

        assertTrue(byLegacy.isPresent());
        assertSame(probe, byLegacy.get());
    }

    @Test
    void unknownLegacyEntityIdReturnsEmpty() {
        CitizenProfessionRegistry registry = CitizenProfessionRegistry.defaults();

        assertTrue(registry.lookupByLegacyEntityId(null).isEmpty());
        assertTrue(registry.lookupByLegacyEntityId("").isEmpty());
        assertTrue(registry.lookupByLegacyEntityId("minecolonies:citizen").isEmpty());
    }

    @Test
    void professionReverseLookupFromLegacyEntityIdCoversAllNonNoneValues() {
        for (CitizenProfession profession : CitizenProfession.values()) {
            if (profession == CitizenProfession.NONE) {
                continue;
            }
            String legacyId = profession.legacyEntityId();
            assertNotNull(legacyId, "every non-NONE profession should carry a legacy entity id for save migration: " + profession);
            assertEquals(profession, CitizenProfession.fromLegacyEntityId(legacyId));
        }
    }

    @Test
    void legacyRoleAliasesMapToNewRoleVocabulary() {
        assertEquals(CitizenRole.CONTROLLED_RECRUIT, CitizenRole.fromLegacy(CitizenRole.RECRUIT));
        assertEquals(CitizenRole.CONTROLLED_WORKER, CitizenRole.fromLegacy(CitizenRole.WORKER));
        assertEquals(CitizenRole.CIVILIAN_RESIDENT, CitizenRole.fromLegacy(null));
        assertEquals(CitizenRole.NOBLE, CitizenRole.fromLegacy(CitizenRole.NOBLE));
    }

    @Test
    void professionTagNameFallsBackToNoneOnDirtySave() {
        assertEquals(CitizenProfession.FARMER, CitizenProfession.fromTagName("FARMER"));
        assertEquals(CitizenProfession.NONE, CitizenProfession.fromTagName(null));
        assertEquals(CitizenProfession.NONE, CitizenProfession.fromTagName(""));
        assertEquals(CitizenProfession.NONE, CitizenProfession.fromTagName("DEFINITELY_NOT_A_PROFESSION"));
    }

    @Test
    void noopControllerReturnsCorrectProfessionAndCoarseRole() {
        for (CitizenProfession profession : CitizenProfession.values()) {
            CitizenProfessionController controller = CitizenProfessionController.noop(profession);
            assertEquals(profession, controller.profession());
            assertEquals(profession.coarseRole(), controller.role());
        }
    }

    private static final class ProbeFarmerController implements CitizenProfessionController {
        @Override
        public CitizenProfession profession() {
            return CitizenProfession.FARMER;
        }
    }
}
