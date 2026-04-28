package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.PatrolLeaderScreen;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

import java.util.List;


public class MessageToClientUpdateLeaderScreen implements BannerModMessage<MessageToClientUpdateLeaderScreen> {
    public List<BlockPos> waypoints;
    public List<ItemStack> waypointItems;
    public int size;

    public MessageToClientUpdateLeaderScreen() {
    }

    public MessageToClientUpdateLeaderScreen(List<BlockPos> waypoints, List<ItemStack> waypointItems, int size) {
        this.waypoints = waypoints;
        this.waypointItems = waypointItems;
        this.size = size;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    public void executeClientSide(BannerModNetworkContext context) {

    }

    @Override
    public MessageToClientUpdateLeaderScreen fromBytes(FriendlyByteBuf buf) {
        this.waypoints = buf.readList(byteBuf -> byteBuf.readBlockPos());
        this.waypointItems = buf.readList(byteBuf -> ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) byteBuf));
        this.size = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(waypoints, (byteBuf, value) -> byteBuf.writeBlockPos(value));
        buf.writeCollection(waypointItems, (byteBuf, value) -> ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) byteBuf, value));
        buf.writeInt(this.size);
    }
}
