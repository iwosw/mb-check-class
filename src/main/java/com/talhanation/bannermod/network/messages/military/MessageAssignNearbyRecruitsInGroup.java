package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageAssignNearbyRecruitsInGroup implements Message<MessageAssignNearbyRecruitsInGroup> {

    private UUID groupUUID;

    public MessageAssignNearbyRecruitsInGroup() {
    }

    public MessageAssignNearbyRecruitsInGroup(UUID group) {
        this.groupUUID = group;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        RecruitsGroup newGroup = RecruitEvents.recruitsGroupsManager.getGroup(groupUUID);
        if(newGroup == null) return;

        player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(100),
                (recruit) -> recruit.isEffectedByCommand(player.getUUID())
        ).forEach((recruit) -> this.setGroup(recruit, newGroup));

        RecruitEvents.recruitsGroupsManager.addOrUpdateGroup(player.serverLevel(), player, newGroup);

        RecruitEvents.recruitsGroupsManager.broadCastGroupsToPlayer(player);
    }

    public void setGroup(AbstractRecruitEntity recruit, RecruitsGroup group){
        if(recruit.getGroupUUID().isPresent() && recruit.getGroupUUID().get().equals(group)){
            return;
        }

        group.addMember(recruit.getUUID());
        RecruitsGroup oldGroup = RecruitEvents.recruitsGroupsManager.getGroup(recruit.getGroup());
        if(oldGroup != null) oldGroup.removeMember(recruit.getUUID());

        recruit.setGroupUUID(groupUUID);
    }

    public MessageAssignNearbyRecruitsInGroup fromBytes(FriendlyByteBuf buf) {
        this.groupUUID = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(groupUUID);
    }
}