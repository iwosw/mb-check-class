package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.map.FormationMapSnapshotService;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class MessageRequestFormationMapSnapshot implements Message<MessageRequestFormationMapSnapshot> {
    public MessageRequestFormationMapSnapshot() {
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null) return;
        FormationMapSnapshotService.SnapshotRequestResult result = FormationMapSnapshotService.requestSnapshot(sender);
        if (result.throttled()) return;
        BannerModMain.SIMPLE_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> sender),
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
