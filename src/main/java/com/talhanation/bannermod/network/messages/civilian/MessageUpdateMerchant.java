package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.MerchantAccessControl;
import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateMerchant implements Message<MessageUpdateMerchant> {

    public UUID merchantUuid;
    public boolean isCreative;
    public boolean isTrading;
    public boolean dailyRefresh;
    public MessageUpdateMerchant() {}
    public MessageUpdateMerchant(UUID merchantUuid, boolean isCreative, boolean isTrading) {
        this.merchantUuid = merchantUuid;
        this.isCreative = isCreative;
        this.isTrading = isTrading;
    }
    public MessageUpdateMerchant(UUID merchantUuid, boolean isCreative, boolean isTrading, boolean dailyRefresh) {
        this.merchantUuid  = merchantUuid;
        this.isCreative    = isCreative;
        this.isTrading     = isTrading;
        this.dailyRefresh  = dailyRefresh;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;

        Entity entity = player.serverLevel().getEntity(this.merchantUuid);
        if (entity instanceof MerchantEntity merchant
                && merchant.isAlive()
                && player.getBoundingBox().inflate(32.0D).intersects(merchant.getBoundingBox())) {
            update(merchant, player);
        }

    }

    private void update(MerchantEntity merchant, ServerPlayer player){
        if (!MerchantAccessControl.canManage(merchant.getOwnerUUID(), player.getUUID(), player.hasPermissions(2))) {
            return;
        }
        merchant.setCreative(this.isCreative);
        merchant.setTrading(this.isTrading);
        merchant.setDailyRefresh(this.dailyRefresh);
    }
    public MessageUpdateMerchant fromBytes(FriendlyByteBuf buf) {
        this.merchantUuid  = buf.readUUID();
        this.isCreative    = buf.readBoolean();
        this.isTrading     = buf.readBoolean();
        this.dailyRefresh  = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(merchantUuid);
        buf.writeBoolean(this.isCreative);
        buf.writeBoolean(this.isTrading);
        buf.writeBoolean(this.dailyRefresh);
    }
}
