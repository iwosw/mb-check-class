package com.talhanation.bannermod;


import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorkersUpdateChecker {

    @SubscribeEvent
    public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event){
        // Disabled in the merged bannermod runtime: the legacy "workers" mod id is not loaded.
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event){
        // Disabled in the merged bannermod runtime: the legacy "workers" mod id is not loaded.
    }
}
