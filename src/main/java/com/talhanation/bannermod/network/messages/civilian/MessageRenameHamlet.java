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

public class MessageRenameHamlet implements BannerModMessage<MessageRenameHamlet> {
    private UUID hamletId;
    private String newName;

    public MessageRenameHamlet() {
    }

    public MessageRenameHamlet(UUID hamletId, String newName) {
        this.hamletId = hamletId;
        this.newName = newName == null ? "" : newName;
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
        try {
            NpcHamletRecord updated = NpcHamletAccess.rename(level, this.hamletId, this.newName, level.getGameTime());
            MessageRequestHamletSnapshot.sendSystemMessage(player, Component.translatable(
                    "gui.bannermod.society.hamlet.command.renamed",
                    NpcHamletAccess.displayName(updated)
            ));
        } catch (IllegalArgumentException ex) {
            MessageRequestHamletSnapshot.sendSystemMessage(player, Component.translatable("gui.bannermod.society.hamlet.command." + renameReasonKey(ex)));
        }
        MessageRequestHamletSnapshot.sendSnapshot(player, MessageRequestHamletSnapshot.buildSnapshot(player));
    }

    private static String renameReasonKey(IllegalArgumentException ex) {
        String message = ex == null ? null : ex.getMessage();
        return switch (message == null ? "invalid_name" : message) {
            case "name_too_short" -> "name_too_short";
            case "name_too_long" -> "name_too_long";
            case "duplicate_name" -> "duplicate_name";
            default -> "invalid_name";
        };
    }

    @Override
    public MessageRenameHamlet fromBytes(FriendlyByteBuf buf) {
        this.hamletId = buf.readUUID();
        this.newName = buf.readUtf(64);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.hamletId);
        buf.writeUtf(this.newName == null ? "" : this.newName, 64);
    }
}
