package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AssassinLeaderEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageAssassinCount implements Message<MessageAssassinCount> {

    private int count;
    private UUID uuid;

    public MessageAssassinCount(){
    }

    public MessageAssassinCount(int count, UUID uuid) {
        this.count = count;
        this.uuid = uuid;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (entity instanceof AssassinLeaderEntity leader
                && player.getBoundingBox().inflate(16.0D).intersects(leader.getBoundingBox())) {
            leader.setCount(this.count);
        }
    }
    public MessageAssassinCount fromBytes(FriendlyByteBuf buf) {
        this.count = buf.readInt();
        this.uuid = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(count);
        buf.writeUUID(uuid);
    }

}
