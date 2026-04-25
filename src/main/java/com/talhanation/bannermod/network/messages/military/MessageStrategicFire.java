package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageStrategicFire implements Message<MessageStrategicFire> {

    private UUID player;
    private UUID group;
    private boolean should;

    public MessageStrategicFire() {
    }

    public MessageStrategicFire(UUID player, UUID group, boolean should) {
        this.player = player;
        this.group = group;
        this.should = should;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        AABB commandBox = serverPlayer.getBoundingBox().inflate(100);
        List<AbstractRecruitEntity> actors = RecruitIndex.instance().groupInRange(
                serverPlayer.getCommandSenderWorld(),
                this.group,
                serverPlayer.position(),
                200.0D
        );
        if (actors == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            actors = serverPlayer.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    commandBox
            );
        } else {
            actors.removeIf(recruit -> !recruit.getBoundingBox().intersects(commandBox));
        }
        if (actors.isEmpty()) {
            return;
        }
        long gameTime = serverPlayer.getCommandSenderWorld().getGameTime();
        CommandIntent intent = new CommandIntent.StrategicFire(
                gameTime,
                CommandIntentPriority.NORMAL,
                false,
                this.group,
                this.should
        );
        CommandIntentDispatcher.dispatch(serverPlayer, intent, actors);
    }

    public MessageStrategicFire fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.group = buf.readUUID();
        this.should = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.group);
        buf.writeBoolean(this.should);
    }
}
