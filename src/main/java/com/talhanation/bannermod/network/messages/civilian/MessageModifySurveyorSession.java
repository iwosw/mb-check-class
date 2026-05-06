package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MessageModifySurveyorSession implements BannerModMessage<MessageModifySurveyorSession> {
    public int handIndex;
    public int actionOrdinal;

    public MessageModifySurveyorSession() {
    }

    public MessageModifySurveyorSession(InteractionHand hand, Action action) {
        this.handIndex = hand == InteractionHand.OFF_HAND ? 1 : 0;
        this.actionOrdinal = action.ordinal();
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
            Action[] actions = Action.values();
            if (actionOrdinal < 0 || actionOrdinal >= actions.length) {
                return;
            }
            ItemStack stack = player.getItemInHand(handIndex == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof SettlementSurveyorToolItem)) {
                return;
            }
            switch (actions[actionOrdinal]) {
                case CANCEL_PENDING_CORNER -> SettlementSurveyorToolItem.cancelPendingCorner(player, stack);
                case CLEAR_CURRENT_ROLE -> SettlementSurveyorToolItem.clearSelectedRoleZone(player, stack);
                case RESET_ALL_MARKS -> SettlementSurveyorToolItem.resetAllMarks(player, stack);
                case TOGGLE_GUIDE_PREVIEW -> SettlementSurveyorToolItem.toggleGuidePreview(player, stack);
                case SUGGEST_DRAFT_ZONES -> SettlementSurveyorToolItem.suggestDraftZones(player, stack);
            }
        });
    }

    @Override
    public MessageModifySurveyorSession fromBytes(FriendlyByteBuf buf) {
        this.handIndex = buf.readVarInt();
        this.actionOrdinal = buf.readVarInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(handIndex);
        buf.writeVarInt(actionOrdinal);
    }

    public enum Action {
        CANCEL_PENDING_CORNER,
        CLEAR_CURRENT_ROLE,
        RESET_ALL_MARKS,
        TOGGLE_GUIDE_PREVIEW,
        SUGGEST_DRAFT_ZONES
    }
}
