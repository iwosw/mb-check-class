package com.talhanation.bannerlord.client.shared.events;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.recruits.Main;
import com.talhanation.bannerlord.client.shared.ClientManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class ClientSyncLifecycleEvents {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientManager.resetSynchronizedState();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientManager.resetSynchronizedState();
    }
}
