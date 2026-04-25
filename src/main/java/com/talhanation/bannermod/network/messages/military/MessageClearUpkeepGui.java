package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageClearUpkeepGui implements Message<MessageClearUpkeepGui> {

    private UUID uuid;

    public MessageClearUpkeepGui(){
    }

    public MessageClearUpkeepGui(UUID uuid) {
        this.uuid = uuid;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity entity = player.serverLevel().getEntity(this.uuid);
        if (entity instanceof AbstractRecruitEntity recruit
                && player.getBoundingBox().inflate(16.0D).intersects(recruit.getBoundingBox())) {
            recruit.clearUpkeepPos();
            recruit.clearUpkeepEntity();
        }
    }

    public MessageClearUpkeepGui fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
    }

}
