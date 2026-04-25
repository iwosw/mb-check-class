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

public class MessageDismountGui implements Message<MessageDismountGui> {

    private UUID uuid;
    private UUID player;

    public MessageDismountGui() {
    }

    public MessageDismountGui(UUID player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());
        Entity entity = serverPlayer.serverLevel().getEntity(this.uuid);
        if (entity instanceof AbstractRecruitEntity recruit
                && serverPlayer.getBoundingBox().inflate(16.0D).intersects(recruit.getBoundingBox())) {
            CommandEvents.onDismountButton(player, recruit, null);
        }
    }

    public MessageDismountGui fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.player = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(player);
    }
}
