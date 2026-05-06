package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

public class MessageRotateWorkArea implements BannerModMessage<MessageRotateWorkArea> {

    public UUID uuid;
    public boolean clockwise;

    public MessageRotateWorkArea() {}

    public MessageRotateWorkArea(UUID uuid, boolean clockwise) {
        this.uuid = uuid;
        this.clockwise = clockwise;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            AbstractWorkAreaEntity workArea = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, AbstractWorkAreaEntity.class);
            if (workArea == null) {
                return;
            }

            this.rotate(player, workArea);
        });
    }

    public void rotate(ServerPlayer player, AbstractWorkAreaEntity workArea) {
        Direction current = workArea.getFacing();
        Direction next = WorkAreaRotation.rotate(current, clockwise);

        // Test the rotated area before committing
        workArea.setFacing(next);
        workArea.createArea();

        if (AbstractWorkAreaEntity.isAreaOverlapping(workArea.level(), workArea, workArea.getArea())) {
            // Revert to original facing
            workArea.setFacing(current);
            workArea.createArea();
            WorkAreaMessageSupport.sendDecision(player, WorkAreaAuthoringRules.Decision.OVERLAPPING);
        }
    }

    @Override
    public MessageRotateWorkArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.clockwise = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeBoolean(clockwise);
    }
}
