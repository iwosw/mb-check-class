package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.MerchantAccessControl;
import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import com.talhanation.bannermod.persistence.civilian.WorkersMerchantTrade;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateMerchantTrade implements Message<MessageUpdateMerchantTrade> {

    public UUID merchantUuid;
    public CompoundTag nbt;
    public boolean remove;
    public MessageUpdateMerchantTrade() {}
    public MessageUpdateMerchantTrade(UUID merchantUuid, WorkersMerchantTrade trade, boolean remove) {
        this.merchantUuid = merchantUuid;
        this.nbt = trade.toNbt();
        this.remove = remove;
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

    public void update(MerchantEntity merchant, ServerPlayer player){
        if (!MerchantAccessControl.canManage(merchant.getOwnerUUID(), player.getUUID(), player.hasPermissions(2))) {
            return;
        }
        if(remove){
            merchant.removeTrade(WorkersMerchantTrade.fromNbt(nbt));
        }
        else{
            merchant.addOrUpdateTrade(WorkersMerchantTrade.fromNbt(nbt));
        }
    }

    public MessageUpdateMerchantTrade fromBytes(FriendlyByteBuf buf) {
        this.merchantUuid = buf.readUUID();
        this.nbt = buf.readNbt();
        this.remove = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(merchantUuid);
        buf.writeNbt(nbt);
        buf.writeBoolean(remove);
    }
}
