package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.settlement.validation.SurveyorMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MessageSetSurveyorMode implements BannerModMessage<MessageSetSurveyorMode> {
    public int handIndex;
    public int modeOrdinal;

    public MessageSetSurveyorMode() {
    }

    public MessageSetSurveyorMode(InteractionHand hand, SurveyorMode mode) {
        this.handIndex = hand == InteractionHand.OFF_HAND ? 1 : 0;
        this.modeOrdinal = mode.ordinal();
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        SurveyorMode[] modes = SurveyorMode.values();
        if (modeOrdinal < 0 || modeOrdinal >= modes.length) {
            return;
        }
        ItemStack stack = player.getItemInHand(handIndex == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof SettlementSurveyorToolItem)) {
            return;
        }
        SettlementSurveyorToolItem.setMode(player, stack, modes[modeOrdinal]);
    }

    @Override
    public MessageSetSurveyorMode fromBytes(FriendlyByteBuf buf) {
        this.handIndex = buf.readVarInt();
        this.modeOrdinal = buf.readVarInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(handIndex);
        buf.writeVarInt(modeOrdinal);
    }
}
