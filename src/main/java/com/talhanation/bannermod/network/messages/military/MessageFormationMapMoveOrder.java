package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandHierarchy;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.army.command.MovementCommandState;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageFormationMapMoveOrder implements BannerModMessage<MessageFormationMapMoveOrder> {
    private UUID contactId;
    private UUID groupId;
    private BlockPos target;

    public MessageFormationMapMoveOrder() {
    }

    public MessageFormationMapMoveOrder(UUID contactId, UUID groupId, BlockPos target) {
        this.contactId = contactId;
        this.groupId = groupId;
        this.target = target;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            dispatchToServer(sender, contactId, groupId, target);
        });
    }

    public static void dispatchToServer(ServerPlayer sender, UUID contactId, UUID groupId, BlockPos target) {
        if (sender == null || target == null || contactId == null) return;
        ServerLevel level = sender.serverLevel();
        BlockPos moveTarget = new BlockPos(
                target.getX(),
                level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ()),
                target.getZ()
        );

        List<AbstractRecruitEntity> recruits = new ArrayList<>(resolveTargets(sender, contactId, groupId));
        recruits.removeIf(recruit -> !CommandHierarchy.canCommand(sender, recruit));
        if (recruits.isEmpty()) return;

        int formation = CommandEvents.getSavedFormation(sender);
        CommandIntent intent = new CommandIntent.Movement(
                level.getGameTime(),
                CommandIntentPriority.NORMAL,
                false,
                MovementCommandState.MOVE_TO_POSITION,
                formation,
                false,
                Vec3.atCenterOf(moveTarget)
        );
        CommandIntentDispatcher.dispatch(sender, intent, recruits);
    }

    private static List<AbstractRecruitEntity> resolveTargets(ServerPlayer sender, UUID contactId, UUID groupId) {
        if (groupId != null) {
            return RecruitCommandTargetResolver.resolveGroupTargets(sender, sender.getUUID(), groupId, "formation-map-move");
        }
        AbstractRecruitEntity recruit = RecruitIndex.instance().get(sender.serverLevel(), contactId);
        if (recruit == null || !CommandHierarchy.canCommand(sender, recruit)) {
            return List.of();
        }
        return List.of(recruit);
    }

    @Override
    public MessageFormationMapMoveOrder fromBytes(FriendlyByteBuf buf) {
        this.contactId = buf.readUUID();
        this.groupId = buf.readBoolean() ? buf.readUUID() : null;
        this.target = buf.readBlockPos();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(contactId);
        buf.writeBoolean(groupId != null);
        if (groupId != null) buf.writeUUID(groupId);
        buf.writeBlockPos(target);
    }
}
