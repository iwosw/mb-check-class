package com.talhanation.recruits;

import com.talhanation.bannerlord.bootstrap.BannerlordMain;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.Logger;

public class Main {
    public static final String MOD_ID = BannerlordMain.MOD_ID;
    public static SimpleChannel SIMPLE_CHANNEL;
    public static final Logger LOGGER = BannerlordMain.LOGGER;
    public static boolean isMusketModLoaded;
    public static boolean isSmallShipsLoaded;
    public static boolean isSmallShipsCompatible;
    public static boolean isSiegeWeaponsLoaded;
    public static boolean isEpicKnightsLoaded;
    public static boolean isCorpseLoaded;
    public static boolean isRPGZLoaded;

    private Main() {
    }

    public static void syncFromBannerlord() {
        SIMPLE_CHANNEL = BannerlordMain.SIMPLE_CHANNEL;
        isMusketModLoaded = BannerlordMain.isMusketModLoaded;
        isSmallShipsLoaded = BannerlordMain.isSmallShipsLoaded;
        isSmallShipsCompatible = BannerlordMain.isSmallShipsCompatible;
        isSiegeWeaponsLoaded = BannerlordMain.isSiegeWeaponsLoaded;
        isEpicKnightsLoaded = BannerlordMain.isEpicKnightsLoaded;
        isCorpseLoaded = BannerlordMain.isCorpseLoaded;
        isRPGZLoaded = BannerlordMain.isRPGZLoaded;
    }
}
