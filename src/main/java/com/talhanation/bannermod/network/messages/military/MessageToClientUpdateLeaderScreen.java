package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.PatrolLeaderScreen;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
    public void executeClientSide(NetworkEvent.Context context) {

    }

    @Override
    public MessageToClientUpdateLeaderScreen fromBytes(FriendlyByteBuf buf) {
        this.waypoints = buf.readList(FriendlyByteBuf::readBlockPos);
        this.waypointItems = buf.readList(FriendlyByteBuf::readItem);
        this.size = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(waypoints, FriendlyByteBuf::writeBlockPos);
        buf.writeCollection(waypointItems, FriendlyByteBuf::writeItem);
        buf.writeInt(this.size);
    }
}

