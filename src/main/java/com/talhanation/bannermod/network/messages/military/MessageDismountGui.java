package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageDismountGui implements BannerModMessage<MessageDismountGui> {

    private UUID uuid;
    private UUID player;

    public MessageDismountGui() {
    }

    public MessageDismountGui(UUID player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(serverPlayer, this.uuid, 16.0D);
        if (recruit != null) {
            CommandEvents.onDismountButton(player, recruit, null);
        }
    }

    public MessageDismountGui fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.player = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(player);
    }
}
