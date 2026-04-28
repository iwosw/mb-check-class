package com.talhanation.bannermod.network.compat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public final class BannerModNetworkContext {
    private final IPayloadContext delegate;

    public BannerModNetworkContext(IPayloadContext delegate) {
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
