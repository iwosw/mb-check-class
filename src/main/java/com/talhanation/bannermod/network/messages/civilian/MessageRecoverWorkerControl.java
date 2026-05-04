package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageRecoverWorkerControl implements BannerModMessage<MessageRecoverWorkerControl> {

    public List<UUID> workerUuids = new ArrayList<>();

    public MessageRecoverWorkerControl() {
    }

    public MessageRecoverWorkerControl(List<UUID> workerUuids) {
        this.workerUuids = new ArrayList<>(workerUuids);
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (workerUuids.isEmpty()) {
                player.sendSystemMessage(Component.literal("No workers selected for recovery."));
                return;
            }

            int recovered = 0;

            for (UUID workerUuid : workerUuids) {
                Entity entity = player.serverLevel().getEntity(workerUuid);
                AbstractWorkerEntity worker = entity instanceof AbstractWorkerEntity abstractWorkerEntity ? abstractWorkerEntity : null;

                if (worker == null) {
                    continue;
                }

                if (!player.getUUID().equals(worker.getOwnerUUID()) && !player.hasPermissions(2)) {
                    continue;
                }

                if (worker.recoverControl(player)) {
                    recovered++;
                }
            }

            if (recovered == 0) {
                player.sendSystemMessage(Component.literal("No controlled workers could be recovered."));
            }
        });
    }

    @Override
    public MessageRecoverWorkerControl fromBytes(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.workerUuids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.workerUuids.add(buf.readUUID());
        }
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(workerUuids.size());
        for (UUID workerUuid : workerUuids) {
            buf.writeUUID(workerUuid);
        }
    }
}
