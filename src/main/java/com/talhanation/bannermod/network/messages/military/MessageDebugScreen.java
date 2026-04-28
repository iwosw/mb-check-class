package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageDebugScreen implements Message<MessageDebugScreen> {
    private UUID uuid;
    private UUID recruit;


    public MessageDebugScreen() {
        this.uuid = new UUID(0, 0);
    }

    public MessageDebugScreen(Player player, UUID recruit) {
        this.uuid = player.getUUID();
        this.recruit = recruit;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        if (!player.getUUID().equals(uuid)) {
            return;
        }

        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(player, this.recruit, 16.0D);
        if (recruit != null) {
            recruit.openDebugScreen(player);
        }
    }

    @Override
    public MessageDebugScreen fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(recruit);
    }
}
