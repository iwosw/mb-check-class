package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageClearTargetGui implements Message<MessageClearTargetGui> {
    private UUID recruit;
    private UUID player;

    public MessageClearTargetGui() {
    }

    public MessageClearTargetGui(UUID player, UUID recruit) {
        this.player = player;
        this.recruit = recruit;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity entity = player.serverLevel().getEntity(this.recruit);
        if (entity instanceof AbstractRecruitEntity recruit
                && player.getBoundingBox().inflate(16.0D).intersects(recruit.getBoundingBox())) {
            CommandEvents.onClearTargetButton(this.player, recruit, null);
        }
    }

    public MessageClearTargetGui fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.recruit);
    }
}
