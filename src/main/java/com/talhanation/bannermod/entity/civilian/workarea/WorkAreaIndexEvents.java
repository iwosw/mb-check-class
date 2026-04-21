package com.talhanation.bannermod.entity.civilian.workarea;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-bus hooks that keep {@link WorkAreaIndex} populated. Registered statically via
 * {@link Mod.EventBusSubscriber} so no explicit registration call is needed.
 */
@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
