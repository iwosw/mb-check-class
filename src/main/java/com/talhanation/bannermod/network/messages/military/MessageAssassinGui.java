package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.AssassinLeaderEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageAssassinGui implements Message<MessageAssassinGui> {

    private UUID uuid;
    private UUID recruit;


    public MessageAssassinGui() {
        this.uuid = new UUID(0, 0);
    }

    public MessageAssassinGui(Player player, UUID recruit) {
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

        Entity entity = player.serverLevel().getEntity(this.recruit);
        if (entity instanceof AssassinLeaderEntity assassin
                && assassin.isAlive()
                && player.getBoundingBox().inflate(16.0D).intersects(assassin.getBoundingBox())) {
            assassin.openGUI(player);
        }
    }

    @Override
    public MessageAssassinGui fromBytes(FriendlyByteBuf buf) {
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
