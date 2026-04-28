package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.map.FormationMapSnapshotService;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;

public class MessageRequestFormationMapSnapshot implements BannerModMessage<MessageRequestFormationMapSnapshot> {
    public MessageRequestFormationMapSnapshot() {
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;
        FormationMapSnapshotService.SnapshotRequestResult result = FormationMapSnapshotService.requestSnapshot(sender);
        if (result.throttled()) return;
        BannerModMain.SIMPLE_CHANNEL.send(
                BannerModPacketDistributor.PLAYER.with(() -> sender),
                new MessageToClientUpdateFormationMapSnapshot(result.contacts())
        );
    }

    @Override
    public MessageRequestFormationMapSnapshot fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
    }
}
