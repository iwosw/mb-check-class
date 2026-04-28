package com.talhanation.bannermod.network.messages.war;

import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;

/**
 * Server → client snapshot of warfare-RP state (political entities, active wars, siege standards).
 *
 * <p>Pushed by {@code WarStateBroadcaster} on player login and whenever the underlying
 * {@code SavedData} changes. The payload is a single {@link CompoundTag} so adding new
 * top-level entries (e.g. occupations, demilitarizations) later doesn't break the wire.
 */
public class MessageToClientUpdateWarState implements BannerModMessage<MessageToClientUpdateWarState> {
    private CompoundTag payload;

    public MessageToClientUpdateWarState() {
    }

    public MessageToClientUpdateWarState(CompoundTag payload) {
        this.payload = payload;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.clientbound();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(BannerModNetworkContext context) {
        RuntimeProfilingCounters.recordNbtPacket("network.full_sync.war_state", payload);
        WarClientState.applyFromNbt(payload);
    }

    @Override
    public MessageToClientUpdateWarState fromBytes(FriendlyByteBuf buf) {
        this.payload = buf.readNbt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.payload);
    }
}
