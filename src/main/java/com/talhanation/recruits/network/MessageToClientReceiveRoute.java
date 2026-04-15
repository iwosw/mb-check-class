package com.talhanation.recruits.network;

import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.world.RecruitsRoute;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client: delivers a transferred route to the receiving player.
 * The client saves it to their local routes directory.
 */
public class MessageToClientReceiveRoute implements Message<MessageToClientReceiveRoute> {

    private CompoundTag routeNBT;

    public MessageToClientReceiveRoute() {}

    public MessageToClientReceiveRoute(CompoundTag routeNBT) {
        this.routeNBT = routeNBT;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        RecruitsRoute route = decodeRouteForClient(routeNBT);
        if (route == null) return;
        ClientManager.saveRoute(route);
    }

    static RecruitsRoute decodeRouteForClient(CompoundTag routeNBT) {
        if (routeNBT == null || routeNBT.isEmpty()) return null;
        if (!routeNBT.hasUUID("ID")) return null;

        String routeName = routeNBT.getString("Name");
        if (routeName == null || routeName.isBlank()) return null;

        return RecruitsRoute.fromNBT(routeNBT);
    }

    @Override
    public MessageToClientReceiveRoute fromBytes(FriendlyByteBuf buf) {
        this.routeNBT = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.routeNBT);
    }
}
