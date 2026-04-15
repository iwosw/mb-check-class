package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.NobleTradeScreen;
import com.talhanation.bannermod.entity.military.VillagerNobleEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageToClientOpenNobleTradeScreen implements Message<MessageToClientOpenNobleTradeScreen> {

    private UUID recruit_uuid;

    public MessageToClientOpenNobleTradeScreen() {

    }

    public MessageToClientOpenNobleTradeScreen(UUID recruit_uuid) {
        this.recruit_uuid = recruit_uuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        Player player = Minecraft.getInstance().player;
        player.getCommandSenderWorld().getEntitiesOfClass(VillagerNobleEntity.class, player.getBoundingBox()
                        .inflate(16.0D), v -> v
                        .getUUID()
                        .equals(this.recruit_uuid))
                .stream()
                .filter(Entity::isAlive)
                .findAny()
                .ifPresent(nobleVillager -> Minecraft.getInstance().setScreen(new NobleTradeScreen(nobleVillager, player)));
    }

    @Override
    public MessageToClientOpenNobleTradeScreen fromBytes(FriendlyByteBuf buf) {
        this.recruit_uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(recruit_uuid);
    }
}