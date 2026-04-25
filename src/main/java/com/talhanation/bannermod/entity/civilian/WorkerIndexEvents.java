package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WorkerIndexEvents {
    private WorkerIndexEvents() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        WorkerIndex.instance().onEntityJoin(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        WorkerIndex.instance().onEntityLeave(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WorkerIndex.instance().clear(serverLevel.dimension());
        }
    }
}
