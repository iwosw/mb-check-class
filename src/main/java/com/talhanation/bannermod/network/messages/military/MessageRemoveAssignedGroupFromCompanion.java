package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.ICompanion;
import com.talhanation.bannermod.util.RecruitCommanderUtil;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class MessageRemoveAssignedGroupFromCompanion implements BannerModMessage<MessageRemoveAssignedGroupFromCompanion> {

    private UUID owner;
    private UUID companion;

    public MessageRemoveAssignedGroupFromCompanion() {
    }

    public MessageRemoveAssignedGroupFromCompanion(UUID owner, UUID companion) {
        this.owner = owner;
        this.companion = companion;
    }

    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = context.getSender();
        Entity entity = serverPlayer.serverLevel().getEntity(this.companion);
        if (entity instanceof AbstractLeaderEntity companionEntity
                && serverPlayer.getBoundingBox().inflate(100D).intersects(companionEntity.getBoundingBox())) {

            RecruitsGroup group = RecruitEvents.recruitsGroupsManager.getGroup(companionEntity.getGroup());
            if(group == null) return;
            group.leaderUUID = null;
            companionEntity.setGroupUUID(group.getUUID());


            if(companionEntity.getArmySize() > 0){
                RecruitCommanderUtil.setRecruitsListen(companionEntity.army.getAllRecruitUnits(), true);
                RecruitCommanderUtil.setRecruitsFollow(companionEntity.army.getAllRecruitUnits(), null);
                RecruitCommanderUtil.setRecruitsHoldPos(companionEntity.army.getAllRecruitUnits());
                RecruitCommanderUtil.setRecruitsMoveSpeed(companionEntity.army.getAllRecruitUnits(), 1F);
            }

            companionEntity.army = null;
            RecruitEvents.recruitsGroupsManager.broadCastGroupsToPlayer(serverPlayer);

            BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(context::getSender), new MessageToClientUpdateLeaderScreen(companionEntity.WAYPOINTS, companionEntity.WAYPOINT_ITEMS, companionEntity.getArmySize()));
        }
    }

    public MessageRemoveAssignedGroupFromCompanion fromBytes(FriendlyByteBuf buf) {
        this.owner = buf.readUUID();
        this.companion = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.owner);
        buf.writeUUID(this.companion);
    }
}
