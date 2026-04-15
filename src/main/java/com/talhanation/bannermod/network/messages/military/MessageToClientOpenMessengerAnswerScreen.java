package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.MessengerAnswerScreen;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageToClientOpenMessengerAnswerScreen implements Message<MessageToClientOpenMessengerAnswerScreen> {

    public String message;
    public CompoundTag nbt;
    public UUID recruitUUID;

    public MessageToClientOpenMessengerAnswerScreen() {
    }

    public MessageToClientOpenMessengerAnswerScreen(MessengerEntity messenger, String message, RecruitsPlayerInfo playerInfo) {
        this.message = message;
        this.nbt = playerInfo.toNBT();
        this.recruitUUID = messenger.getUUID();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        Player player = Minecraft.getInstance().player;
        player.getCommandSenderWorld().getEntitiesOfClass(MessengerEntity.class, player.getBoundingBox()
                        .inflate(16.0D), v -> v
                        .getUUID()
                        .equals(this.recruitUUID))
                .stream()
                .filter(Entity::isAlive)
                .findAny()
                .ifPresent(recruit -> Minecraft.getInstance().setScreen(new MessengerAnswerScreen(recruit, player, message, RecruitsPlayerInfo.getFromNBT(nbt))));
    }

    @Override
    public MessageToClientOpenMessengerAnswerScreen fromBytes(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
        this.nbt = buf.readNbt();
        this.recruitUUID = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(message);
        buf.writeNbt(nbt);
        buf.writeUUID(recruitUUID);
    }
}