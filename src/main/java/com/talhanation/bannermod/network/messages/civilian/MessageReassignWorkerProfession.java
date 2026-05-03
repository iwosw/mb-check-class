package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.citizen.CitizenProfession;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerCitizenConversionService;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Server-bound: client request to swap a worker's profession in place. The
 * server reuses {@link WorkerCitizenConversionService#convertDeniedReasonKey}
 * for auth so the same controller checks (creative-OP bypass, distance,
 * owner OR same political entity) gate this path as the convert path.
 */
public class MessageReassignWorkerProfession implements BannerModMessage<MessageReassignWorkerProfession> {
    private UUID workerUuid;
    private String targetProfessionTag;

    public MessageReassignWorkerProfession() {
    }

    public MessageReassignWorkerProfession(UUID workerUuid, String targetProfessionTag) {
        this.workerUuid = workerUuid;
        this.targetProfessionTag = targetProfessionTag;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.workerUuid == null || this.targetProfessionTag == null) {
            return;
        }
        if (!(player.serverLevel().getEntity(this.workerUuid) instanceof AbstractWorkerEntity worker)) {
            player.sendSystemMessage(Component.translatable("chat.bannermod.workerui.reassign.denied.missing"));
            return;
        }
        CitizenProfession target = CitizenProfession.fromTagName(this.targetProfessionTag);
        String denialKey = WorkerCitizenConversionService.reassignProfession(player, worker, target);
        if (denialKey != null) {
            player.sendSystemMessage(Component.translatable(denialKey));
            return;
        }
        player.sendSystemMessage(Component.translatable(
                "chat.bannermod.workerui.reassign.success",
                Component.translatable("gui.bannermod.worker_screen.reassign.option." + target.name().toLowerCase())
        ));
    }

    @Override
    public MessageReassignWorkerProfession fromBytes(FriendlyByteBuf buf) {
        this.workerUuid = buf.readUUID();
        this.targetProfessionTag = buf.readUtf();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.workerUuid);
        buf.writeUtf(this.targetProfessionTag == null ? "" : this.targetProfessionTag);
    }
}
