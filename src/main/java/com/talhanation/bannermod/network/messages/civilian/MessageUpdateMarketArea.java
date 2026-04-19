package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class MessageUpdateMarketArea implements Message<MessageUpdateMarketArea> {

    public UUID uuid;
    public boolean isOpen;
    public String marketName;

    public MessageUpdateMarketArea() {}

    public MessageUpdateMarketArea(UUID uuid, boolean isOpen, String marketName) {
        this.uuid = uuid;
        this.isOpen = isOpen;
        this.marketName = marketName;
    }

    @Override
    public Dist getExecutingSide() { return Dist.DEDICATED_SERVER; }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        MarketArea market = WorkAreaMessageSupport.resolveAuthorizedWorkArea(player, this.uuid, MarketArea.class);
        if (market == null) {
            return;
        }

        market.setOpen(isOpen);
        market.setMarketName(marketName);
        if (player.level() instanceof ServerLevel serverLevel) {
            WorkAreaMessageSupport.refreshSettlementSnapshot(serverLevel, market.blockPosition());
        }
    }

    @Override
    public MessageUpdateMarketArea fromBytes(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.isOpen = buf.readBoolean();
        this.marketName = buf.readUtf();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeBoolean(isOpen);
        buf.writeUtf(marketName);
    }
}
