package com.talhanation.workers.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantAccessControlTest {

    @Test
    void ownerCanManageMerchant() {
        UUID owner = UUID.randomUUID();

        assertTrue(MerchantAccessControl.canManage(owner, owner, false));
    }

    @Test
    void operatorsCanManageWithoutOwnership() {
        assertTrue(MerchantAccessControl.canManage(null, UUID.randomUUID(), true));
    }

    @Test
    void unrelatedPlayersCannotManageMerchant() {
        assertFalse(MerchantAccessControl.canManage(UUID.randomUUID(), UUID.randomUUID(), false));
    }
}
