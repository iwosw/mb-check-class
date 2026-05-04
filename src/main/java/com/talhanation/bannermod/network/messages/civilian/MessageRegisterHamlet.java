package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.society.NpcHamletAccess;
import com.talhanation.bannermod.society.NpcHamletRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class MessageRegisterHamlet implements BannerModMessage<MessageRegisterHamlet> {
    private UUID hamletId;

    public MessageRegisterHamlet() {
    }

    public MessageRegisterHamlet(UUID hamletId) {
        this.hamletId = hamletId;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        ServerPlayer player = context.getSender();
        ServerLevel level = MessageRequestHamletSnapshot.serverLevel(player);
        if (player == null || level == null || this.hamletId == null) {
            return;
        }
        NpcHamletRecord hamlet = NpcHamletAccess.hamletFor(level, this.hamletId).orElse(null);
        if (hamlet == null) {
            MessageRequestHamletSnapshot.sendSystemMessage(player, Component.translatable("gui.bannermod.society.hamlet.command.not_found"));
            MessageRequestHamletSnapshot.sendSnapshot(player, MessageRequestHamletSnapshot.buildSnapshot(player));
            return;
        }
        RecruitsClaim claim = MessageRequestHamletSnapshot.claimById(hamlet.claimUuid());
        PoliticalEntityRecord owner = MessageRequestHamletSnapshot.ownerRecord(level, claim);
        if (!PoliticalEntityAuthority.canAct(player, owner)) {
            MessageRequestHamletSnapshot.sendSystemMessage(player, PoliticalEntityAuthority.denialReason(player.getUUID(), player.hasPermissions(2), owner));
            MessageRequestHamletSnapshot.sendSnapshot(player, MessageRequestHamletSnapshot.buildSnapshot(player));
            return;
        }
        NpcHamletRecord updated = NpcHamletAccess.register(level, this.hamletId, level.getGameTime());
        MessageRequestHamletSnapshot.sendSystemMessage(player, Component.translatable(
                "gui.bannermod.society.hamlet.command.registered",
                NpcHamletAccess.displayName(updated)
        ));
        MessageRequestHamletSnapshot.sendSnapshot(player, MessageRequestHamletSnapshot.buildSnapshot(player));
    }

    @Override
    public MessageRegisterHamlet fromBytes(FriendlyByteBuf buf) {
        this.hamletId = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.hamletId);
    }
}
