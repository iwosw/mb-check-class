package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageGroup implements Message<MessageGroup> {

    private UUID groupUUID;
    private UUID recruitUUID;

    public MessageGroup() {
    }

    public MessageGroup(UUID groupUUID, UUID recruitUUID) {
        this.groupUUID = groupUUID;
        this.recruitUUID = recruitUUID;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity entity = player.serverLevel().getEntity(this.recruitUUID);
        if (entity instanceof AbstractRecruitEntity recruit && player.getBoundingBox().inflate(100).intersects(recruit.getBoundingBox())) {
            this.setGroup(recruit, player, groupUUID);
        }
    }

    public void setGroup(AbstractRecruitEntity recruit, ServerPlayer player , UUID groupUUID){
        RecruitsGroup oldGroup = RecruitEvents.recruitsGroupsManager.getGroup(recruit.getGroup());
        RecruitsGroup newGroup = RecruitEvents.recruitsGroupsManager.getGroup(groupUUID);
        if(oldGroup != null && newGroup != null && oldGroup.getUUID().equals(newGroup.getUUID())) return;

        if(oldGroup != null) RecruitEvents.recruitsGroupsManager.removeMember(oldGroup.getUUID(), recruit.getUUID(), player.serverLevel());
        if(newGroup != null) RecruitEvents.recruitsGroupsManager.addMember(newGroup.getUUID(), recruit.getUUID(), player.serverLevel());

        recruit.setGroupUUID(newGroup.getUUID());
    }

    public MessageGroup fromBytes(FriendlyByteBuf buf) {
        this.groupUUID = buf.readUUID();
        this.recruitUUID = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(groupUUID);
        buf.writeUUID(recruitUUID);
    }
}
