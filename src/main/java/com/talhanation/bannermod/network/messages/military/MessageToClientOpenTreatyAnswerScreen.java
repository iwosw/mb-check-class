package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.MessengerTreatyAnswerScreen;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MessageToClientOpenTreatyAnswerScreen implements Message<MessageToClientOpenTreatyAnswerScreen> {

    public int durationHours;
    public CompoundTag nbt;
    public int entityId;

    public MessageToClientOpenTreatyAnswerScreen() {
    }

    public MessageToClientOpenTreatyAnswerScreen(MessengerEntity messenger, int durationHours, RecruitsPlayerInfo playerInfo) {
        this.durationHours = durationHours;
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
            Minecraft.getInstance().setScreen(new MessengerTreatyAnswerScreen(messenger, player, durationHours, RecruitsPlayerInfo.getFromNBT(nbt)));
        }
    }

    @Override
    public MessageToClientOpenTreatyAnswerScreen fromBytes(FriendlyByteBuf buf) {
        this.durationHours = buf.readInt();
        this.nbt = buf.readNbt();
        this.entityId = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(durationHours);
        buf.writeNbt(nbt);
        buf.writeInt(entityId);
    }
}
