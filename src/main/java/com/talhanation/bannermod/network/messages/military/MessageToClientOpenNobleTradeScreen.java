package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.NobleTradeScreen;
import com.talhanation.bannermod.entity.military.VillagerNobleEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

public class MessageToClientOpenNobleTradeScreen implements BannerModMessage<MessageToClientOpenNobleTradeScreen> {

    private int entityId;

    public MessageToClientOpenNobleTradeScreen() {

    }

    public MessageToClientOpenNobleTradeScreen(VillagerNobleEntity noble) {
        this.entityId = noble.getId();
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        Player player = Minecraft.getInstance().player;
        if (player != null
                && player.level().getEntity(this.entityId) instanceof VillagerNobleEntity nobleVillager
                && nobleVillager.isAlive()
                && player.getBoundingBox().inflate(16.0D).intersects(nobleVillager.getBoundingBox())) {
            Minecraft.getInstance().setScreen(new NobleTradeScreen(nobleVillager, player));
        }
    }

    @Override
    public MessageToClientOpenNobleTradeScreen fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }
}
