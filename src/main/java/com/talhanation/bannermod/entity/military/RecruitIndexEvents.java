package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            RecruitIndex.instance().clear(serverLevel.dimension());
        }
    }
}
