package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.RecruitCommandAuthority;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;
import java.util.UUID;

public class MessageSaveFormationFollowMovement implements BannerModMessage<MessageSaveFormationFollowMovement> {

    private UUID player_uuid;

    private CompoundTag groups;
    private int formation;

    public MessageSaveFormationFollowMovement(){
    }

    public MessageSaveFormationFollowMovement(UUID player_uuid, List<UUID> groups, int formation) {
        this.player_uuid = player_uuid;
        this.groups = RecruitsGroup.uuidListToNbt(groups);
        this.formation = formation;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || !sender.getUUID().equals(player_uuid)) return;
            List<UUID> ownedGroups = RecruitCommandAuthority.filterOwnedGroups(sender, RecruitsGroup.uuidListFromNbt(groups));
            CommandEvents.saveFormation(sender, formation);
            CommandEvents.saveUUIDList(sender, "ActiveGroups", ownedGroups);
        });
    }

    public MessageSaveFormationFollowMovement fromBytes(FriendlyByteBuf buf) {
        this.player_uuid = buf.readUUID();
        this.groups = buf.readNbt();
        this.formation = buf.readInt();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player_uuid);
        buf.writeNbt(this.groups);
        buf.writeInt(this.formation);
    }

}
