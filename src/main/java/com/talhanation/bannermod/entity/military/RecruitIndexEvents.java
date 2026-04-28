package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class RecruitIndexEvents {
    private RecruitIndexEvents() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        RecruitIndex.instance().onEntityJoin(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        RecruitIndex.instance().onEntityLeave(event.getEntity());
        if (event.getEntity() instanceof AbstractRecruitEntity recruit) {
            com.talhanation.bannermod.combat.RecruitMoraleService.invalidate(recruit.getUUID());
            com.talhanation.bannermod.combat.CavalryChargeService.invalidate(recruit.getUUID());
            com.talhanation.bannermod.combat.RangedSpacingService.invalidate(recruit.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            RecruitIndex.instance().clear(serverLevel.dimension());
        }
    }
}
