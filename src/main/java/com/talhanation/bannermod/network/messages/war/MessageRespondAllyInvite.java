package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.war.runtime.WarAllyService;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

/**
 * Client → server: invitee leader accepts (or declines) an outstanding ally
 * invitation. Mirrors {@code /bannermod war ally accept|decline}.
 */
public class MessageRespondAllyInvite implements BannerModMessage<MessageRespondAllyInvite> {
    private UUID inviteId;
    private boolean accept;

    public MessageRespondAllyInvite() {
    }

    public MessageRespondAllyInvite(UUID inviteId, boolean accept) {
        this.inviteId = inviteId;
        this.accept = accept;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null || this.inviteId == null) return;
        ServerLevel level = player.serverLevel().getServer().overworld();
        if (level == null) return;
        WarAllyService.InviteResult result = accept
                ? WarAllyService.accept(level, player, this.inviteId)
                : WarAllyService.decline(level, player, this.inviteId);
        if (result.ok()) {
            player.sendSystemMessage(Component.literal(accept ? "Joined as ally." : "Invite declined."));
        } else {
            player.sendSystemMessage(Component.literal((accept ? "Accept" : "Decline")
                    + " denied: " + result.outcome().token()));
        }
    }

    @Override
    public MessageRespondAllyInvite fromBytes(FriendlyByteBuf buf) {
        this.inviteId = buf.readUUID();
        this.accept = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.inviteId);
        buf.writeBoolean(this.accept);
    }
}
