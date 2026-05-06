package com.talhanation.bannermod.entity.citizen;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BannerModMain.MOD_ID)
public final class CitizenIndexEvents {
    private CitizenIndexEvents() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        CitizenIndex.instance().onEntityJoin(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        CitizenIndex.instance().onEntityLeave(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            CitizenIndex.instance().clear(serverLevel.dimension());
        }
    }
}
