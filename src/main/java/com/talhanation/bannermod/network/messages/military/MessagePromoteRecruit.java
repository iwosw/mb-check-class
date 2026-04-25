package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessagePromoteRecruit implements Message<MessagePromoteRecruit> {

    private UUID recruit;
    private int profession;
    private String name;
    public MessagePromoteRecruit(){
    }

    public MessagePromoteRecruit(UUID recruit, int profession, String name) {
        this.recruit = recruit;
        this.profession = profession;
        this.name = name;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        if (context.getSender() == null) {
            return;
        }
        Entity entity = context.getSender().serverLevel().getEntity(this.recruit);
        if (entity instanceof AbstractRecruitEntity recruit && recruit.distanceToSqr(context.getSender()) <= 16D * 16D) {
            RecruitEvents.promoteRecruit(recruit, profession, name, context.getSender());
        }

    }
    public MessagePromoteRecruit fromBytes(FriendlyByteBuf buf) {
        this.recruit = buf.readUUID();
        this.profession = buf.readInt();
        this.name = buf.readUtf();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recruit);
        buf.writeInt(profession);
        buf.writeUtf(name);
    }

}
