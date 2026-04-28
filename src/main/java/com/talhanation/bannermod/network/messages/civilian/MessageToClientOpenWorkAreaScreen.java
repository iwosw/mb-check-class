package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

public class MessageToClientOpenWorkAreaScreen implements BannerModMessage<MessageToClientOpenWorkAreaScreen> {

    private int entityId;
    private UUID uuid;

    public MessageToClientOpenWorkAreaScreen() {

    }

    public MessageToClientOpenWorkAreaScreen(int entityId, UUID uuid) {
        this.entityId = entityId;
        this.uuid = uuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) {
            return;
        }

        AbstractWorkAreaEntity areaEntity = null;
        if (player.level().getEntity(this.entityId) instanceof AbstractWorkAreaEntity entity && entity.getUUID().equals(this.uuid)) {
            areaEntity = entity;
        }

        if (areaEntity == null) {
            player.sendSystemMessage(Component.translatable("gui.workers.area.authoring.open_failed"));
            return;
        }

        Minecraft.getInstance().setScreen(areaEntity.getScreen(player));
    }

    @Override
    public MessageToClientOpenWorkAreaScreen fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUUID(uuid);
    }
}
