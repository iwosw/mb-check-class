package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;

public class MessageApplyNoGroup implements Message<MessageApplyNoGroup> {

    private UUID owner;
    private UUID groupID;

    public MessageApplyNoGroup(){
    }

    public MessageApplyNoGroup(UUID owner, UUID groupID) {
        this.owner = owner;
        this.groupID = groupID;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> recruitList = new ArrayList<>();

        ServerLevel serverLevel = (ServerLevel) player.getCommandSenderWorld();

        List<AbstractRecruitEntity> indexed = RecruitIndex.instance().groupMembers(serverLevel, groupID);
        if (indexed != null) {
            recruitList.addAll(indexed);
        } else {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            for(Entity entity : serverLevel.getEntities().getAll()){
                if(entity instanceof AbstractRecruitEntity recruit && recruit.getGroup() != null && recruit.getGroup().equals(groupID))
                    recruitList.add(recruit);
            }
        }

        for(AbstractRecruitEntity recruit : recruitList){
            recruit.setGroupUUID(null);
        }
    }
    public MessageApplyNoGroup fromBytes(FriendlyByteBuf buf) {
        this.owner = buf.readUUID();
        this.groupID = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.owner);
        buf.writeUUID(this.groupID);
    }

}
