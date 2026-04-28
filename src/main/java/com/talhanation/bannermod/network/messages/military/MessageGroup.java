package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.RecruitCommandAuthority;
import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
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
        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(player, this.recruitUUID, 100.0D);
        if (RecruitCommandAuthority.canDirectlyControl(player, recruit)) {
            this.setGroup(recruit, player, groupUUID);
        }
    }

    public void setGroup(AbstractRecruitEntity recruit, ServerPlayer player , UUID groupUUID){
        RecruitsGroup newGroup = RecruitCommandAuthority.ownedGroup(player, groupUUID);
        if (newGroup == null) return;

        RecruitsGroup oldGroup = RecruitEvents.recruitsGroupsManager.getGroup(recruit.getGroup());
        if(oldGroup != null && newGroup != null && oldGroup.getUUID().equals(newGroup.getUUID())) return;

        if(oldGroup != null) RecruitEvents.recruitsGroupsManager.removeMember(oldGroup.getUUID(), recruit.getUUID(), player.serverLevel());
        RecruitEvents.recruitsGroupsManager.addMember(newGroup.getUUID(), recruit.getUUID(), player.serverLevel());

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
