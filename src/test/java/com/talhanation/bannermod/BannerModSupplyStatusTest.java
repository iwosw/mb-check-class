package com.talhanation.bannermod;

import com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus;
import com.talhanation.bannermod.entity.civilian.WorkerStorageRequestState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSupplyStatusTest {

    @Test
    void buildProjectStatusDistinguishesTemplateReadinessMaterialPressureAndCompletion() {
        BannerModSupplyStatus.BuildProjectStatus noTemplate = BannerModSupplyStatus.buildProjectStatus(false, false, 0, 0);
        BannerModSupplyStatus.BuildProjectStatus ready = BannerModSupplyStatus.buildProjectStatus(true, true, 0, 0);
        BannerModSupplyStatus.BuildProjectStatus needsMaterials = BannerModSupplyStatus.buildProjectStatus(true, true, 2, 20);
        BannerModSupplyStatus.BuildProjectStatus complete = BannerModSupplyStatus.buildProjectStatus(true, false, 1, 12);

        assertEquals(BannerModSupplyStatus.BuildState.NO_TEMPLATE, noTemplate.state());
        assertEquals(BannerModSupplyStatus.BuildState.READY, ready.state());
        assertEquals(BannerModSupplyStatus.BuildState.NEEDS_MATERIALS, needsMaterials.state());
        assertEquals(2, needsMaterials.materialTypes());
        assertEquals(20, needsMaterials.materialCount());
        assertEquals(BannerModSupplyStatus.BuildState.COMPLETE, complete.state());
    }

    @Test
    void workerSupplyStatusReflectsPendingStorageComplaint() {
        WorkerStorageRequestState state = new WorkerStorageRequestState();

        BannerModSupplyStatus.WorkerSupplyStatus clear = BannerModSupplyStatus.workerSupplyStatus(state.releasePendingComplaint());
        assertFalse(clear.blocked());

        state.recordPendingComplaint("storage_missing_items", "Storage does not have planks.");
        BannerModSupplyStatus.WorkerSupplyStatus blocked = BannerModSupplyStatus.workerSupplyStatus(state.releasePendingComplaint());

        assertTrue(blocked.blocked());
        assertEquals("storage_missing_items", blocked.reasonToken());
        assertEquals("Storage does not have planks.", blocked.message());
    }

    @Test
    void recruitSupplyStatusKeepsMilitaryUpkeepPressureInSharedSupplyVocabulary() {
        BannerModSupplyStatus.RecruitSupplyStatus ready = BannerModSupplyStatus.recruitSupplyStatus(true, true, true, true, true, false, 100.0F);
        BannerModSupplyStatus.RecruitSupplyStatus missingFood = BannerModSupplyStatus.recruitSupplyStatus(true, true, false, false, true, false, 12.0F);
        BannerModSupplyStatus.RecruitSupplyStatus inventoryPaymentFallback = BannerModSupplyStatus.recruitSupplyStatus(true, false, true, false, false, true, 100.0F);
        BannerModSupplyStatus.RecruitSupplyStatus missingBoth = BannerModSupplyStatus.recruitSupplyStatus(true, true, true, false, false, false, 0.0F);

        assertEquals(BannerModSupplyStatus.RecruitSupplyState.READY, ready.state());
        assertFalse(ready.blocked());
        assertEquals(BannerModSupplyStatus.ArmyUpkeepState.STABLE, ready.accounting().state());

        assertEquals(BannerModSupplyStatus.RecruitSupplyState.NEEDS_FOOD, missingFood.state());
        assertTrue(missingFood.blocked());
        assertTrue(missingFood.needsFood());
        assertEquals("recruit_upkeep_missing_food", missingFood.reasonToken());
        assertEquals(BannerModSupplyStatus.ArmyUpkeepState.STARVING, missingFood.accounting().state());
        assertEquals(1, missingFood.accounting().starvingLevel());

        assertEquals(BannerModSupplyStatus.RecruitSupplyState.READY, inventoryPaymentFallback.state());
        assertFalse(inventoryPaymentFallback.blocked());
        assertEquals(BannerModSupplyStatus.ArmyUpkeepState.STABLE, inventoryPaymentFallback.accounting().state());

        assertEquals(BannerModSupplyStatus.RecruitSupplyState.NEEDS_FOOD_AND_PAYMENT, missingBoth.state());
        assertTrue(missingBoth.blocked());
        assertTrue(missingBoth.needsFood());
        assertTrue(missingBoth.needsPayment());
        assertEquals("recruit_upkeep_missing_food_and_payment", missingBoth.reasonToken());
        assertEquals(BannerModSupplyStatus.ArmyUpkeepState.UNPAID_AND_STARVING, missingBoth.accounting().state());
        assertEquals(1, missingBoth.accounting().unpaidLevel());
        assertEquals(3, missingBoth.accounting().starvingLevel());
        assertEquals("army_upkeep_unpaid_and_starving", missingBoth.accounting().reasonToken());
    }
}
