package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.ICompanion;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.NPCArmy;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageAssignGroupToCompanion implements Message<MessageAssignGroupToCompanion> {

    private UUID ownerUUID;
    private UUID companionUUID;
    public MessageAssignGroupToCompanion(){
    }

    public MessageAssignGroupToCompanion(UUID owner, UUID companionUUID) {
        this.ownerUUID = owner;
        this.companionUUID = companionUUID;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer =  context.getSender();
        ServerLevel serverLevel =  serverPlayer.serverLevel();

        Entity entity = serverLevel.getEntity(this.companionUUID);
        if (!(entity instanceof AbstractLeaderEntity companionEntity)
                || !serverPlayer.getBoundingBox().inflate(100).intersects(companionEntity.getBoundingBox())) {
            return;
        }


        RecruitsGroup group = RecruitEvents.recruitsGroupsManager.getGroup(companionEntity.getGroup());
        if(group == null) return;

        List<AbstractRecruitEntity> recruits = RecruitIndex.instance().groupMembersInRange(
                serverLevel,
                group.getUUID(),
                serverPlayer.position(),
                100.0D
        );
        List<LivingEntity> list;
        if (recruits == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            list = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    serverPlayer.getBoundingBox().inflate(100)
            );
            list.removeIf(living -> !(living instanceof AbstractRecruitEntity recruit)
                    || (recruit.getGroup() == null || !recruit.getGroup().equals(group.getUUID()))
                    || recruit.getUUID().equals(this.companionUUID));
        } else {
            list = new ArrayList<>();
            for (AbstractRecruitEntity recruit : recruits) {
                if (!recruit.getUUID().equals(this.companionUUID)) {
                    list.add(recruit);
                }
            }
        }

        for (LivingEntity living : list) {
            if(living instanceof AbstractRecruitEntity recruit) ICompanion.assignToLeaderCompanion(companionEntity, recruit);
        }
        companionEntity.army = new NPCArmy(serverLevel, list, null);
        group.leaderUUID = companionUUID;
        companionEntity.setGroupUUID(group.getUUID());

        RecruitEvents.recruitsGroupsManager.broadCastGroupsToPlayer(serverPlayer);
    }

    public MessageAssignGroupToCompanion fromBytes(FriendlyByteBuf buf) {
        this.ownerUUID = buf.readUUID();
        this.companionUUID = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.ownerUUID);
        buf.writeUUID(this.companionUUID);
    }

}
