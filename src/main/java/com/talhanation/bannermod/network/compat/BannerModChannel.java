package com.talhanation.bannermod.network.compat;

import de.maxhenkel.corelib.net.Message;

public final class BannerModChannel {
    public void sendToServer(Message<?> message) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(message);
    }

    public void send(BannerModPacketDistributor.Target target, Message<?> message) {
        target.send(message);
    }
}
