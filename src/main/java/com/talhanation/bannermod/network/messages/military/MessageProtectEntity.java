package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageProtectEntity implements Message<MessageProtectEntity> {

    private UUID uuid;
    private UUID target;
    private UUID group;

    public MessageProtectEntity(){

    }

    public MessageProtectEntity(UUID uuid, UUID target, UUID group) {
        this.uuid = uuid;
        this.target = target;
        this.group = group;

    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> recruits = this.group == null
                ? RecruitIndex.instance().ownerInRange(player.getCommandSenderWorld(), this.uuid, player.position(), 100.0D)
                : RecruitIndex.instance().groupInRange(player.getCommandSenderWorld(), this.group, player.position(), 100.0D);
        if (recruits == null) {
            RuntimeProfilingCounters.increment("recruit.index.fallback_scans");
            recruits = player.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    player.getBoundingBox().inflate(100)
            );
        }
        recruits.forEach((recruit) -> CommandEvents.onProtectButton(uuid, recruit, target, group));
    }
    public MessageProtectEntity fromBytes(FriendlyByteBuf buf) {
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
