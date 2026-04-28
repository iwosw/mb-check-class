package com.talhanation.bannermod.war.events;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.config.WarServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Periodic driver for {@link com.talhanation.bannermod.war.runtime.OccupationTaxRuntime}.
 *
 * <p>Polled once per real second on the server tick. Each call asks the tax runtime to
 * accrue any due cycles based on the configured per-chunk amount and interval; the runtime
 * itself is idempotent against repeat calls inside the same interval, so the polling
 * cadence is not load-bearing for correctness — it just bounds how soon a newly-due
 * cycle will fire.</p>
 */
public class WarOccupationTaxTicker {
    private static final int TICK_INTERVAL = 20;

    private int counter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        ServerLevel level = server.overworld();
        if (level == null) return;
        if (++counter < TICK_INTERVAL) return;
        counter = 0;

        int taxPerChunk = WarServerConfig.OccupationTaxAmountPerChunk.get();
        long intervalTicks = WarServerConfig.occupationTaxIntervalTicks();
        if (taxPerChunk <= 0 || intervalTicks <= 0L) return;

        WarRuntimeContext.taxRuntime(level).accrue(taxPerChunk, intervalTicks, level.getGameTime());
    }
}
