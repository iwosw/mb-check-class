package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;


public class MessageToClientUpdateOnlinePlayers implements BannerModMessage<MessageToClientUpdateOnlinePlayers> {
    private CompoundTag nbt;

    public MessageToClientUpdateOnlinePlayers() {
    }

    public MessageToClientUpdateOnlinePlayers(List<RecruitsPlayerInfo> playerInfoList) {
        this.nbt = RecruitsPlayerInfo.toNBT(playerInfoList);
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(BannerModNetworkContext context) {
        RuntimeProfilingCounters.recordNbtPacket("network.full_sync.online_players", nbt);
        ClientManager.onlinePlayers = RecruitsPlayerInfo.getListFromNBT(nbt);
        ClientManager.markOnlinePlayersChanged();
    }

    @Override
    public MessageToClientUpdateOnlinePlayers fromBytes(FriendlyByteBuf buf) {
        this.nbt = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.nbt);
    }

}
