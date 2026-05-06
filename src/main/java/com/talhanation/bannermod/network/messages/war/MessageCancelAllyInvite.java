package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.runtime.WarAllyService;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

/**
 * Client → server: side leader cancels an outstanding invitation they issued.
 * Mirrors {@code /bannermod war ally cancel}.
 */
public class MessageCancelAllyInvite implements BannerModMessage<MessageCancelAllyInvite> {
    private UUID inviteId;

    public MessageCancelAllyInvite() {
    }

    public MessageCancelAllyInvite(UUID inviteId) {
        this.inviteId = inviteId;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || this.inviteId == null) return;
            ServerLevel level = player.serverLevel().getServer().overworld();
            if (level == null) return;
            WarAllyService.InviteResult result = WarAllyService.cancel(level, player, this.inviteId);
            if (result.ok()) {
                sendFeedback(player, Component.translatable("gui.bannermod.war.feedback.ally_invite_cancelled"));
            } else {
                sendFeedback(player, Component.translatable("gui.bannermod.war.denial.cancel_invite", result.outcome().component()));
            }
        });
    }

    private static void sendFeedback(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player),
                new MessageToClientWarActionFeedback(message));
    }

    @Override
    public MessageCancelAllyInvite fromBytes(FriendlyByteBuf buf) {
        this.inviteId = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.inviteId);
    }
}
