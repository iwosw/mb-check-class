package com.talhanation.bannermod.shared.military;

import com.talhanation.bannermod.compat.MusketModCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class BannerModRecruitFirearmStatus {
    private BannerModRecruitFirearmStatus() {
    }

    public enum FirearmState {
        NONE,
        SUPPORTED,
        MISSING_AMMO,
        UNSUPPORTED
    }

    public record FirearmInspection(FirearmState state, int ammoCount) {
        public boolean visible() {
            return state != FirearmState.NONE;
        }

        public boolean hasAmmo() {
            return ammoCount > 0;
        }
    }

    public static FirearmInspection inspect(ItemStack mainHand, Container inventory) {
        if (!MusketModCompat.isMusketModItem(mainHand)) {
            return firearmInspection(false, false, 0);
        }
        if (!MusketModCompat.isSupportedRecruitFirearm(mainHand)) {
            return firearmInspection(true, false, 0);
        }

        ResourceLocation ammoId = MusketModCompat.ammoContract(mainHand).orElse(null);
        int ammoCount = ammoId == null ? 0 : countAmmo(inventory, ammoId);
        return firearmInspection(true, true, ammoCount);
    }

    public static FirearmInspection firearmInspection(boolean musketModItem, boolean supportedRecruitFirearm, int ammoCount) {
        if (!musketModItem) {
            return new FirearmInspection(FirearmState.NONE, 0);
        }
        if (!supportedRecruitFirearm) {
            return new FirearmInspection(FirearmState.UNSUPPORTED, 0);
        }
        if (ammoCount <= 0) {
            return new FirearmInspection(FirearmState.MISSING_AMMO, 0);
        }
        return new FirearmInspection(FirearmState.SUPPORTED, ammoCount);
    }

    private static int countAmmo(Container inventory, ResourceLocation ammoId) {
        int ammoCount = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (MusketModCompat.isAmmo(stack, ammoId)) {
                ammoCount += stack.getCount();
            }
        }
        return ammoCount;
    }
}
