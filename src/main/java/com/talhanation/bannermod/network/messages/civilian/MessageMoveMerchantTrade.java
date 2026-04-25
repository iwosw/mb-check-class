package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageMoveMerchantTrade implements Message<MessageMoveMerchantTrade> {

    public UUID merchantUuid;
    public UUID tradeUuid;
    public boolean moveUp;

    public MessageMoveMerchantTrade() {}
    public MessageMoveMerchantTrade(UUID merchantUuid, UUID tradeUuid, boolean moveUp) {
        this.merchantUuid = merchantUuid;
        this.tradeUuid    = tradeUuid;
        this.moveUp       = moveUp;
    }

    @Override
    public Dist getExecutingSide() { return Dist.DEDICATED_SERVER; }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        Entity entity = player.serverLevel().getEntity(this.merchantUuid);
        if (entity instanceof MerchantEntity merchant
                && merchant.isAlive()
                && player.getBoundingBox().inflate(32.0D).intersects(merchant.getBoundingBox())) {
            if (!player.getUUID().equals(merchant.getOwnerUUID()) && !player.hasPermissions(2)) return;
            if (moveUp) merchant.moveTradeUp(tradeUuid);
            else        merchant.moveTradeDown(tradeUuid);
        }
    }

    @Override
    public MessageMoveMerchantTrade fromBytes(FriendlyByteBuf buf) {
        this.merchantUuid = buf.readUUID();
        this.tradeUuid    = buf.readUUID();
        this.moveUp       = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(merchantUuid);
        buf.writeUUID(tradeUuid);
        buf.writeBoolean(moveUp);
    }
}
