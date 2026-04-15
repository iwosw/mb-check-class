package com.talhanation.bannermod.entity.civilian;

import java.util.UUID;

public final class MerchantAccessControl {

    private MerchantAccessControl() {
    }

    public static boolean canManage(UUID ownerUuid, UUID playerUuid, boolean hasPermission) {
        if (hasPermission) {
            return true;
        }
        if (ownerUuid == null || playerUuid == null) {
            return false;
        }
        return ownerUuid.equals(playerUuid);
    }
}
