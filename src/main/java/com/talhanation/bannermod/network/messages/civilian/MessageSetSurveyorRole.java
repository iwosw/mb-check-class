package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MessageSetSurveyorRole implements BannerModMessage<MessageSetSurveyorRole> {
    public int handIndex;
    public int roleOrdinal;

    public MessageSetSurveyorRole() {
    }

    public MessageSetSurveyorRole(InteractionHand hand, ZoneRole role) {
        this.handIndex = hand == InteractionHand.OFF_HAND ? 1 : 0;
        this.roleOrdinal = role.ordinal();
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
        ZoneRole[] roles = ZoneRole.values();
        if (roleOrdinal < 0 || roleOrdinal >= roles.length) {
            return;
        }
        ItemStack stack = player.getItemInHand(handIndex == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof SettlementSurveyorToolItem)) {
            return;
        }
        SettlementSurveyorToolItem.setSelectedRole(stack, roles[roleOrdinal]);
    }

    @Override
    public MessageSetSurveyorRole fromBytes(FriendlyByteBuf buf) {
        this.handIndex = buf.readVarInt();
        this.roleOrdinal = buf.readVarInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(handIndex);
        buf.writeVarInt(roleOrdinal);
    }
}
