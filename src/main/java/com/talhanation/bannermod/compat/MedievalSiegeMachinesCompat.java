package com.talhanation.bannermod.compat;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraftforge.fml.ModList;

/**
 * Centralized optional-compat facts for Magistu's Medieval Siege Machines.
 */
public final class MedievalSiegeMachinesCompat {
    public static final String MOD_ID = "siegemachines";
    public static final String LEGACY_MOD_ID = "siegeweapons";

    private MedievalSiegeMachinesCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded(LEGACY_MOD_ID);
    }

    public static void logDetectedState() {
        if (isLoaded()) {
            BannerModMain.LOGGER.info("Medieval Siege Machines compatibility detected; siege engineer promotion is enabled.");
        }
    }
}
