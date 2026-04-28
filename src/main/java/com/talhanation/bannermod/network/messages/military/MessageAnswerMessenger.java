package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.entity.military.MessengerEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageAnswerMessenger implements Message<MessageAnswerMessenger> {

    private UUID recruit;
    public MessageAnswerMessenger() {
    }
    public MessageAnswerMessenger(UUID recruit) {
        this.recruit = recruit;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Entity entity = player.serverLevel().getEntity(this.recruit);
        if (entity instanceof MessengerEntity messenger && messenger.distanceToSqr(player) <= 16D * 16D) {
            messenger.teleportWaitTimer = 100;
            player.sendSystemMessage(messenger.MESSENGER_INFO_ON_MY_WAY());
            messenger.giveDeliverItem(player);

            messenger.setMessengerState(MessengerEntity.MessengerState.TELEPORT_BACK);
        }

    }
    public MessageAnswerMessenger fromBytes(FriendlyByteBuf buf) {
        this.recruit = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recruit);
    }
}
