package net.minecraftforge.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public final class NetworkEvent {
    private NetworkEvent() {
    }

    public static final class Context {
        private final IPayloadContext delegate;

        public Context(IPayloadContext delegate) {
            this.delegate = delegate;
        }

        public ServerPlayer getSender() {
            return delegate.player() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        }

        public CompletableFuture<Void> enqueueWork(Runnable runnable) {
            return delegate.enqueueWork(runnable);
        }

        public void setPacketHandled(boolean handled) {
        }
    }
}
