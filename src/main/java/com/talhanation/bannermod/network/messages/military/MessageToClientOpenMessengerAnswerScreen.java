package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.MessengerAnswerScreen;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MessageToClientOpenMessengerAnswerScreen implements Message<MessageToClientOpenMessengerAnswerScreen> {

    public String message;
    public CompoundTag nbt;
    public int entityId;

    public MessageToClientOpenMessengerAnswerScreen() {
    }

    public MessageToClientOpenMessengerAnswerScreen(MessengerEntity messenger, String message, RecruitsPlayerInfo playerInfo) {
        this.message = message;
        this.nbt = playerInfo.toNBT();
        this.entityId = messenger.getId();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        Player player = Minecraft.getInstance().player;
        if (player != null
                && player.level().getEntity(this.entityId) instanceof MessengerEntity messenger
                && messenger.isAlive()
                && player.getBoundingBox().inflate(16.0D).intersects(messenger.getBoundingBox())) {
            Minecraft.getInstance().setScreen(new MessengerAnswerScreen(messenger, player, message, RecruitsPlayerInfo.getFromNBT(nbt)));
        }
    }

    @Override
    public MessageToClientOpenMessengerAnswerScreen fromBytes(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.nbt = buf.readNbt();
        this.entityId = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(message);
        buf.writeNbt(nbt);
        buf.writeInt(entityId);
    }
}
