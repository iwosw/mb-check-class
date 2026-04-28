package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageRest implements BannerModMessage<MessageRest> {

    private UUID player;
    private UUID group;
    private boolean should;

    public MessageRest(){
    }

    public MessageRest(UUID player, UUID group, boolean should) {
        this.player = player;
        this.group = group;
        this.should = should;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> list = this.group == null
                ? RecruitIndex.instance().ownerInRange(serverPlayer.getCommandSenderWorld(), this.player, serverPlayer.position(), 100.0D)
                : RecruitIndex.instance().groupInRange(serverPlayer.getCommandSenderWorld(), this.group, serverPlayer.position(), 100.0D);
        if (list == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            list = serverPlayer.getCommandSenderWorld().getEntitiesOfClass(AbstractRecruitEntity.class, serverPlayer.getBoundingBox().inflate(100));
        }
        for (AbstractRecruitEntity recruits : list) {
                CommandEvents.onRestCommand(serverPlayer, this.player, recruits, group, should);
        }
    }
    public MessageRest fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.group = buf.readUUID();
        this.should = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.group);
        buf.writeBoolean(this.should);
    }

}
