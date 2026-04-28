package com.talhanation.bannermod.war.events;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.runtime.ServerLevelObjectivePresenceProbe;
import com.talhanation.bannermod.war.runtime.WarRevoltScheduler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Drives auto-resolution of pending revolts during active battle windows. The work is
 * throttled to one pass every {@link #TICK_INTERVAL} server ticks: re-checking a wall-clock
 * battle window any faster has no observable effect.
 */
public class WarRevoltAutoResolver {
    private static final int TICK_INTERVAL = 100;

    private int counter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }
        if (++counter < TICK_INTERVAL) {
            return;
        }
        counter = 0;

        boolean windowOpen = WarServerConfig.resolveSchedule()
                .isOpen(ZonedDateTime.now(ZoneId.systemDefault()));
        if (!windowOpen) {
            return;
        }
        WarRevoltScheduler.tick(
                WarRuntimeContext.revolts(overworld),
                WarRuntimeContext.occupations(overworld),
                WarRuntimeContext.applierFor(overworld),
                new ServerLevelObjectivePresenceProbe(overworld, WarRuntimeContext.registry(overworld)),
                overworld.getGameTime(),
                true
        );
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        counter = 0;
    }
}
