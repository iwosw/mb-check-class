package com.talhanation.bannermod.network;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.catalog.CivilianPacketCatalog;
import com.talhanation.bannermod.network.catalog.MilitaryPacketCatalog;
import com.talhanation.bannermod.network.catalog.PacketCatalog;
import com.talhanation.bannermod.network.catalog.WarPacketCatalog;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.talhanation.bannermod.network.compat.BannerModChannel;

/**
 * Owns the single shared BannerModChannel for the merged bannermod runtime.
 *
 * Military packets are registered at indices [0..MILITARY_MESSAGES.length) and
 * civilian packets at [MILITARY_MESSAGES.length..MILITARY_MESSAGES.length+CIVILIAN_MESSAGES.length).
 *
 * Existing ordering within each family is preserved from the legacy messages[] arrays
 * in recruits.Main.setup() and workers.WorkersMain.setup() respectively; newly restored
 * packets are appended to avoid renumbering existing military packets.
 *
 * workerPacketOffset() == MILITARY_MESSAGES.length == 107.
 */
public class BannerModNetworkBootstrap {

    public static final Class<?>[] MILITARY_MESSAGES = MilitaryPacketCatalog.MESSAGES;
    public static final Class<?>[] CIVILIAN_MESSAGES = CivilianPacketCatalog.MESSAGES;
    public static final Class<?>[] WAR_MESSAGES = WarPacketCatalog.MESSAGES;

    private static final PacketCatalog[] PACKET_CATALOGS = {
        MilitaryPacketCatalog.CATALOG,
        CivilianPacketCatalog.CATALOG,
        WarPacketCatalog.CATALOG,
    };

    private BannerModNetworkBootstrap() {
    }

    /**
     * Returns the offset at which civilian (worker) packets begin in the shared channel.
     * Equal to MILITARY_MESSAGES.length (107).
     * Matches the merged runtime's current worker packet offset.
     */
    public static int workerPacketOffset() {
        return MILITARY_MESSAGES.length;
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BannerModMain.MOD_ID);
        for (PacketCatalog catalog : PACKET_CATALOGS) {
            catalog.register(registrar);
        }
    }

    public static BannerModChannel createSharedChannel() {
        BannerModChannel channel = new BannerModChannel();
        com.talhanation.bannermod.bootstrap.WorkersRuntime.bindChannel(channel);
        return channel;
    }
}
