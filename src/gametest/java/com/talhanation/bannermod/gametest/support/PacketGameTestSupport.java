package com.talhanation.bannermod.gametest.support;

import com.talhanation.bannermod.network.payload.BannerModMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class PacketGameTestSupport {
    private PacketGameTestSupport() {
    }

    public static <T extends BannerModMessage<T>> void dispatchServerbound(ServerPlayer sender, T outgoing, Supplier<T> decoder) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        outgoing.toBytes(buffer);
        T incoming = decoder.get().fromBytes(buffer);
        incoming.executeServerSide(new ServerboundContext(sender));
    }

    private record ServerboundContext(ServerPlayer sender) implements IPayloadContext {
        @Override
        public ICommonPacketListener listener() {
            return null;
        }

        @Override
        public Player player() {
            return sender;
        }

        @Override
        public CompletableFuture<Void> enqueueWork(Runnable runnable) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <T> CompletableFuture<T> enqueueWork(Supplier<T> supplier) {
            return CompletableFuture.completedFuture(supplier.get());
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public void handle(CustomPacketPayload payload) {
        }

        @Override
        public void finishCurrentTask(ConfigurationTask.Type type) {
        }

        @Override
        public void handle(Packet<?> packet) {
            if (packet instanceof ClientboundCustomPayloadPacket || packet instanceof ServerboundCustomPayloadPacket) {
                return;
            }
            IPayloadContext.super.handle(packet);
        }
    }
}
