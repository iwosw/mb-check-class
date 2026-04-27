package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageMountEntity implements Message<MessageMountEntity> {

    private UUID uuid;
    private UUID target;
    private UUID group;

    public MessageMountEntity() {
    }

    public MessageMountEntity(UUID uuid, UUID target, UUID group) {
        this.uuid = uuid;
        this.target = target;
        this.group = group;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity mount = player.serverLevel().getEntity(target);
        if (mount == null
                || mount.distanceToSqr(player) > 100.0D * 100.0D
                || !RecruitsServerConfig.MountWhiteList.get().contains(mount.getEncodeId())) {
            return;
        }

        List<AbstractRecruitEntity> recruits = this.group == null
                ? RecruitIndex.instance().ownerInRange(player.getCommandSenderWorld(), this.uuid, player.position(), 100.0D)
                : RecruitIndex.instance().groupInRange(player.getCommandSenderWorld(), this.group, player.position(), 100.0D);
        if (recruits == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            recruits = player.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    player.getBoundingBox().inflate(100),
                    (recruit) -> recruit.isEffectedByCommand(uuid, group)
            );
        }
        CommandIntentDispatcher.dispatch(player, new CommandIntent.SiegeMachine(
                player.level().getGameTime(), CommandIntentPriority.HIGH, false, target, group, false), recruits);
    }

    public MessageMountEntity fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.target = buf.readUUID();
        this.group = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(target);
        buf.writeUUID(group);
    }
}
