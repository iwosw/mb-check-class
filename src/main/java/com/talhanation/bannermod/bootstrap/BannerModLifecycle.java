package com.talhanation.bannermod.bootstrap;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Lifecycle event handlers extracted from legacy recruits.Main and workers.WorkersMain.
 * Additional command/event registration lives here so BannerModMain stays focused on startup wiring.
 */
public class BannerModLifecycle {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // Recruit and worker command registration will be filled in by wave 4 (events + commands wave)
    }
}
