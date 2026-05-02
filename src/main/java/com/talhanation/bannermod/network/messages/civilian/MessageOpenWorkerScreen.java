package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class MessageOpenWorkerScreen implements BannerModMessage<MessageOpenWorkerScreen> {
    private UUID workerUuid;

    public MessageOpenWorkerScreen() {
    }

    public MessageOpenWorkerScreen(UUID workerUuid) {
        this.workerUuid = workerUuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.workerUuid == null) {
            return;
        }
        if (player.serverLevel().getEntity(this.workerUuid) instanceof AbstractWorkerEntity worker
                && worker.isAlive()
                && player.distanceToSqr(worker) <= 16.0D * 16.0D) {
            worker.openDepositsGUI(player);
        }
    }

    @Override
    public MessageOpenWorkerScreen fromBytes(FriendlyByteBuf buf) {
        this.workerUuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.workerUuid);
    }
}
