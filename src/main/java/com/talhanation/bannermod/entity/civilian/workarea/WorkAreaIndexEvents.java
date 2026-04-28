package com.talhanation.bannermod.entity.civilian.workarea;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Forge-bus hooks that keep {@link WorkAreaIndex} populated. Registered statically via
 * {@link EventBusSubscriber} so no explicit registration call is needed.
 */
@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class WorkAreaIndexEvents {
    private WorkAreaIndexEvents() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        WorkAreaIndex.instance().onEntityJoin(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        WorkAreaIndex.instance().onEntityLeave(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            WorkAreaIndex.instance().clear(serverLevel.dimension());
        }
    }
}
