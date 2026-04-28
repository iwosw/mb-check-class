package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.UUID;

public class MessageDoTradeWithMerchant implements BannerModMessage<MessageDoTradeWithMerchant> {

    public UUID merchantUuid;
    public UUID trade;
    public MessageDoTradeWithMerchant() {}
    public MessageDoTradeWithMerchant(UUID merchantUuid, UUID trade) {
        this.merchantUuid = merchantUuid;
        this.trade = trade;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    public void executeServerSide(BannerModNetworkContext context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.merchantUuid);
        if (entity instanceof MerchantEntity merchant
                && merchant.isAlive()
                && player.getBoundingBox().inflate(32.0D).intersects(merchant.getBoundingBox())) {
            merchant.doTrade(trade, player);
        }

    }

    public MessageDoTradeWithMerchant fromBytes(FriendlyByteBuf buf) {
        this.merchantUuid = buf.readUUID();
        this.trade = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(merchantUuid);
        buf.writeUUID(this.trade);
    }
}
