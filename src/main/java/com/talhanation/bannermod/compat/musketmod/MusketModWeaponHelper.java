package com.talhanation.bannermod.compat.musketmod;

import net.minecraft.world.item.ItemStack;

/**
 * Utility helper for MusketMod-related checks.
 */
public final class MusketModWeaponHelper {

    private MusketModWeaponHelper() {
        // utility class – no instances
    }

    /**
     * Returns true if the given ItemStack is a recognised MusketMod weapon or cartridge.
     */
    public static boolean isMusketModWeapon(ItemStack stack) {
        return stack.getDescriptionId().equals("item.musketmod.musket")
                || stack.getDescriptionId().equals("item.musketmod.musket_with_bayonet")
                || stack.getDescriptionId().equals("item.musketmod.musket_with_scope")
                || stack.getDescriptionId().equals("item.musketmod.blunderbuss")
                || stack.getDescriptionId().equals("item.musketmod.cartridge")
                || stack.getDescriptionId().equals("item.musketmod.pistol");
    }
}