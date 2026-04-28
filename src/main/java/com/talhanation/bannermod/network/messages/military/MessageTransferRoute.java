package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.UUID;

import static com.talhanation.bannermod.bootstrap.BannerModMain.SIMPLE_CHANNEL;

/**
 * Client → Server: player wants to transfer a route to another online player.
 * Server → forwards as {@link MessageToClientReceiveRoute} to the target player.
 */
public class MessageTransferRoute implements BannerModMessage<MessageTransferRoute> {

    private UUID targetPlayerUUID;
    private CompoundTag routeNBT;

    public MessageTransferRoute() {}

    public MessageTransferRoute(UUID targetPlayerUUID, RecruitsRoute route) {
        this.targetPlayerUUID = targetPlayerUUID;
        this.routeNBT = route.toNBT();
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer sender = Objects.requireNonNull(context.getSender());
        if (!isRouteTransferPayloadValid(targetPlayerUUID, routeNBT)) return;

        ServerPlayer target = sender.getServer().getPlayerList().getPlayer(targetPlayerUUID);
        if (target == null) return;
        SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> target),
                new MessageToClientReceiveRoute(routeNBT));
    }

    static boolean isRouteTransferPayloadValid(UUID targetPlayerUUID, CompoundTag routeNBT) {
        if (targetPlayerUUID == null) return false;
        return MessageToClientReceiveRoute.decodeRouteForClient(routeNBT) != null;
    }

    @Override
    public MessageTransferRoute fromBytes(FriendlyByteBuf buf) {
        this.targetPlayerUUID = buf.readUUID();
        this.routeNBT = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.targetPlayerUUID);
        buf.writeNbt(this.routeNBT);
    }
}
