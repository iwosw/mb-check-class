package com.talhanation.bannermod.war.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.war.MessageToClientUpdateWarState;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.runtime.BattleWindowSchedule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Server-authoritative broadcaster for the warfare-RP runtime snapshot.
 *
 * <p>Checks a dirty/version counter once per second; if any client-visible runtime changed since
 * the last broadcast, pushes a fresh {@link MessageToClientUpdateWarState} to every online player.
 * On player login, sends the current snapshot immediately so the new client has a populated
 * {@link WarClientState} before any HUD render runs.</p>
 */
public class WarStateBroadcaster {
    private static final int TICK_INTERVAL = 20;

    private int counter = 0;
    private int lastBroadcastVersion = -1;
    private boolean primed = false;
    private int cachedPayloadVersion = -1;
    private CompoundTag cachedPayload;

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        counter = 0;
        lastBroadcastVersion = -1;
        primed = false;
        cachedPayloadVersion = -1;
        cachedPayload = null;
        WarSyncDirtyTracker.reset();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        counter = 0;
        lastBroadcastVersion = -1;
        primed = false;
        cachedPayloadVersion = -1;
        cachedPayload = null;
        WarSyncDirtyTracker.reset();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel().getServer().overworld();
        if (level == null) return;
        sendSnapshotTo(player, level);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;
        ServerLevel level = server.overworld();
        if (level == null) return;
        if (++counter < TICK_INTERVAL) return;
        counter = 0;

        int version = WarSyncDirtyTracker.version();
        if (primed && version == lastBroadcastVersion) return;
        lastBroadcastVersion = version;
        primed = true;

        CompoundTag payload = snapshotPayload(level, version);
        BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.ALL.noArg(),
                new MessageToClientUpdateWarState(payload));
    }

    private void sendSnapshotTo(ServerPlayer player, ServerLevel level) {
        CompoundTag payload = snapshotPayload(level, WarSyncDirtyTracker.version());
        BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new MessageToClientUpdateWarState(payload));
    }

    private CompoundTag snapshotPayload(ServerLevel level, int version) {
        if (cachedPayload != null && cachedPayloadVersion == version) {
            return cachedPayload;
        }
        cachedPayload = WarClientState.encode(
                WarRuntimeContext.registry(level).all(),
                WarRuntimeContext.declarations(level).all(),
                WarRuntimeContext.sieges(level).all(),
                resolveSchedule(),
                WarRuntimeContext.allyInvites(level).all(),
                WarRuntimeContext.occupations(level).all(),
                WarRuntimeContext.revolts(level).all()
        );
        cachedPayloadVersion = version;
        return cachedPayload;
    }

    private static BattleWindowSchedule resolveSchedule() {
        try {
            return WarServerConfig.resolveSchedule();
        } catch (IllegalStateException ex) {
            return BattleWindowSchedule.defaultSchedule();
        }
    }

}
