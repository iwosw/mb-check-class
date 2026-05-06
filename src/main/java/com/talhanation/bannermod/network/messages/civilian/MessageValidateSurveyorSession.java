package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MessageValidateSurveyorSession implements BannerModMessage<MessageValidateSurveyorSession> {
    public int handIndex;

    public MessageValidateSurveyorSession() {
    }

    public MessageValidateSurveyorSession(InteractionHand hand) {
        this.handIndex = hand == InteractionHand.OFF_HAND ? 1 : 0;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getItemInHand(handIndex == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof SettlementSurveyorToolItem)) {
                return;
            }
            SettlementSurveyorToolItem.validateCurrentSession(player, stack);
        });
    }

    @Override
    public MessageValidateSurveyorSession fromBytes(FriendlyByteBuf buf) {
        this.handIndex = buf.readVarInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(handIndex);
    }
}
