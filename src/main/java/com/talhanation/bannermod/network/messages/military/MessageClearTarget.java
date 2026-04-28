package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageClearTarget implements BannerModMessage<MessageClearTarget> {
    private UUID uuid;
    private UUID group;

    public MessageClearTarget(){
    }

    public MessageClearTarget(UUID uuid, UUID group) {
        this.uuid = uuid;
        this.group = group;

    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> list = this.group == null
                ? RecruitIndex.instance().ownerInRange(player.getCommandSenderWorld(), this.uuid, player.position(), 100.0D)
                : RecruitIndex.instance().groupInRange(player.getCommandSenderWorld(), this.group, player.position(), 100.0D);
        if (list == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            list = player.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    player.getBoundingBox().inflate(100));
        }
        for (AbstractRecruitEntity recruits : list) {
            CommandEvents.onClearTargetButton(uuid, recruits, group);
        }
    }
    public MessageClearTarget fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.group = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(group);
    }

}
