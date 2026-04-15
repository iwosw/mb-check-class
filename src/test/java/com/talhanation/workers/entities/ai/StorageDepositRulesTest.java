package com.talhanation.workers.entities.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageDepositRulesTest {

    @Test
    void reserveItemsStayInWorkerInventory() {
        List<StorageDepositRules.SlotState> inventory = new ArrayList<>();
        List<StorageDepositRules.SlotState> storage = new ArrayList<>();
        inventory.add(StorageDepositRules.SlotState.forTests("seed", 8, 64, true));
        inventory.add(StorageDepositRules.SlotState.forTests("wheat", 16, 64, false));
        storage.add(StorageDepositRules.SlotState.forTests(null, 0, 64, false));

        StorageDepositRules.DepositResult result = StorageDepositRules.depositAll(inventory, storage);

        assertEquals(8, inventory.get(0).count());
        assertEquals("wheat", storage.get(0).key());
        assertEquals(16, storage.get(0).count());
        assertFalse(result.hasDepositableItemsRemaining());
    }

    @Test
    void fillsMatchingChestStacksAndPreservesOverflowCount() {
        List<StorageDepositRules.SlotState> inventory = new ArrayList<>();
        List<StorageDepositRules.SlotState> storage = new ArrayList<>();
        inventory.add(StorageDepositRules.SlotState.forTests("wheat", 40, 64, false));
        storage.add(StorageDepositRules.SlotState.forTests("wheat", 60, 64, false));
        storage.add(StorageDepositRules.SlotState.forTests(null, 0, 64, false));

        StorageDepositRules.depositAll(inventory, storage);

        assertEquals(64, storage.get(0).count());
        assertEquals("wheat", storage.get(1).key());
        assertEquals(36, storage.get(1).count());
        assertEquals(0, inventory.get(0).count());
    }

    @Test
    void reportsWhenDepositableItemsRemainForAnotherChest() {
        List<StorageDepositRules.SlotState> inventory = new ArrayList<>();
        List<StorageDepositRules.SlotState> storage = new ArrayList<>();
        inventory.add(StorageDepositRules.SlotState.forTests("wheat", 10, 64, false));
        storage.add(StorageDepositRules.SlotState.forTests("carrot", 64, 64, false));

        StorageDepositRules.DepositResult result = StorageDepositRules.depositAll(inventory, storage);

        assertTrue(result.hasDepositableItemsRemaining());
        assertFalse(result.movedAnyItems());
        assertEquals(10, inventory.get(0).count());
        assertEquals(64, storage.get(0).count());
    }
}
