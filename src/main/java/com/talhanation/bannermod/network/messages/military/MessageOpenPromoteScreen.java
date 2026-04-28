package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.events.RecruitEvents;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageOpenPromoteScreen implements Message<MessageOpenPromoteScreen> {

    private UUID player;
    private UUID recruit;

    public MessageOpenPromoteScreen() {
        this.player = new UUID(0, 0);
    }

    public MessageOpenPromoteScreen(Player player, UUID recruit) {
        this.player = player.getUUID();
        this.recruit = recruit;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        if (!player.getUUID().equals(this.player)) {
            return;
        }

        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitWithinDistance(player, this.recruit, 16.0D * 16.0D);
        if (recruit != null) {
            RecruitEvents.openPromoteScreen(player, recruit);
        }
    }

    @Override
    public MessageOpenPromoteScreen fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeUUID(recruit);
    }
}
