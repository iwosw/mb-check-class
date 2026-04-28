package com.talhanation.bannermod.network.messages.civilian;


import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageOpenMerchantTradeScreen implements Message<MessageOpenMerchantTradeScreen> {
    private UUID player;
    private UUID merchantUuid;
    public MessageOpenMerchantTradeScreen() {
        this.player = new UUID(0L, 0L);
    }

    public MessageOpenMerchantTradeScreen(Player player, UUID merchant) {
        this.player = player.getUUID();
        this.merchantUuid = merchant;
    }
    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }
    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        Entity entity = player.serverLevel().getEntity(this.merchantUuid);
        if (entity instanceof MerchantEntity merchant
                && merchant.isAlive()
                && player.getBoundingBox().inflate(32.0D).intersects(merchant.getBoundingBox())) {
            merchant.openTradeGUI(player);
        }
    }
    @Override
    public MessageOpenMerchantTradeScreen fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.merchantUuid = buf.readUUID();
        return this;
    }
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeUUID(this.merchantUuid);
    }
}
