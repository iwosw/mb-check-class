package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageRotateWorkArea implements Message<MessageRotateWorkArea> {

    public UUID uuid;
    public boolean clockwise;

    public MessageRotateWorkArea() {}

    public MessageRotateWorkArea(UUID uuid, boolean clockwise) {
        this.uuid = uuid;
        this.clockwise = clockwise;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (!(entity instanceof AbstractWorkAreaEntity workArea)) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.AREA_NOT_FOUND);
            return;
        }

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.modifyDecision(true, workArea.getAuthoringAccess(player));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        this.rotate(player, workArea);
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
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.OVERLAPPING);
        }
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
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
