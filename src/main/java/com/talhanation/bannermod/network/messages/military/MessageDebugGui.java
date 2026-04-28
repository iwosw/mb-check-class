package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.DebugEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageDebugGui implements Message<MessageDebugGui> {

    private int id;
    private UUID uuid;
    private String name;

    public MessageDebugGui() {
    }

    public MessageDebugGui(int id, UUID uuid, String name) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(player, this.uuid, 16.0D);
        if (recruit != null) {
            DebugEvents.handleMessage(id, recruit, context.getSender());
            recruit.setCustomName(Component.literal(name));
        }
    }

    public MessageDebugGui fromBytes(FriendlyByteBuf buf) {
        this.id = buf.readInt();
        this.uuid = buf.readUUID();
        this.name = buf.readUtf();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeUUID(uuid);
        buf.writeUtf(name);
    }
}
