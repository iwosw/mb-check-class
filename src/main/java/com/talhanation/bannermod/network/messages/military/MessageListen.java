package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.RecruitCommandAuthority;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageListen implements Message<MessageListen> {

    private boolean bool;
    private UUID uuid;

    public MessageListen() {
    }

    public MessageListen(boolean bool, UUID uuid) {
        this.bool = bool;
        this.uuid = uuid;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(player, this.uuid, 100.0D);
        if (RecruitCommandAuthority.canDirectlyControl(player, recruit)) {
            recruit.setListen(bool);
        }
    }

    public MessageListen fromBytes(FriendlyByteBuf buf) {
        this.bool = buf.readBoolean();
        this.uuid = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(bool);
        buf.writeUUID(uuid);
    }
}
