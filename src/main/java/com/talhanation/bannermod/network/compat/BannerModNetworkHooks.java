package com.talhanation.bannermod.network.compat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;

public final class BannerModNetworkHooks {
    private BannerModNetworkHooks() {
    }

    public static void openScreen(ServerPlayer player, MenuProvider provider) {
        player.openMenu(provider);
    }

    public static Packet<ClientGamePacketListener> getEntitySpawningPacket(Entity entity) {
        return entity.getAddEntityPacket(null);
    }
}
