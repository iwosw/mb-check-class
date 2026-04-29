package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.war.client.WarClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MessageToClientWarActionFeedback implements BannerModMessage<MessageToClientWarActionFeedback> {
    private Component feedback = Component.empty();

    public MessageToClientWarActionFeedback() {
    }

    public MessageToClientWarActionFeedback(Component feedback) {
        this.feedback = feedback == null ? Component.empty() : feedback;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        WarClientState.setLastActionFeedback(this.feedback);
    }

    @Override
    public MessageToClientWarActionFeedback fromBytes(FriendlyByteBuf buf) {
        this.feedback = Component.literal(buf.readUtf(2048));
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.feedback.getString(), 2048);
    }
}
