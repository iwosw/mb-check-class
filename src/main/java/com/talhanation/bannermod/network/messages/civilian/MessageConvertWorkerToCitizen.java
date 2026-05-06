package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerCitizenConversionService;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class MessageConvertWorkerToCitizen implements BannerModMessage<MessageConvertWorkerToCitizen> {
    private UUID workerUuid;

    public MessageConvertWorkerToCitizen() {
    }

    public MessageConvertWorkerToCitizen(UUID workerUuid) {
        this.workerUuid = workerUuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || this.workerUuid == null) {
                return;
            }
            if (!(player.serverLevel().getEntity(this.workerUuid) instanceof AbstractWorkerEntity worker)) {
                player.sendSystemMessage(Component.translatable("gui.bannermod.worker_screen.convert.denied.missing"));
                return;
            }
            String denialKey = WorkerCitizenConversionService.convertDeniedReasonKey(player, worker);
            if (denialKey != null) {
                player.sendSystemMessage(Component.translatable(denialKey));
                return;
            }
            if (WorkerCitizenConversionService.convert(player, worker)) {
                player.sendSystemMessage(Component.translatable("gui.bannermod.worker_screen.convert.success"));
            }
        });
    }

    @Override
    public MessageConvertWorkerToCitizen fromBytes(FriendlyByteBuf buf) {
        this.workerUuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.workerUuid);
    }
}
