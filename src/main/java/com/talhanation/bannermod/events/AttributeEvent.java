package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.military.*;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Random;

@EventBusSubscriber(modid = com.talhanation.bannermod.bootstrap.BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AttributeEvent {
    protected final Random random = new Random();

    @SubscribeEvent
    public static void entityAttributeEvent(final EntityAttributeCreationEvent event) {
        //event.put(ModEntityTypes.ASSASSIN.get(), AssassinEntity.setAttributes().build());
        //event.put(ModEntityTypes.ASSASSIN_LEADER.get(), AssassinLeaderEntity.setAttributes().build());
        event.put(ModEntityTypes.BOWMAN.get(), BowmanEntity.setAttributes().build());
        event.put(ModEntityTypes.CROSSBOWMAN.get(), CrossBowmanEntity.setAttributes().build());
        event.put(ModEntityTypes.NOMAD.get(), NomadEntity.setAttributes().build());
        event.put(ModEntityTypes.RECRUIT.get(), RecruitEntity.setAttributes().build());
        event.put(ModEntityTypes.RECRUIT_SHIELDMAN.get(), RecruitShieldmanEntity.setAttributes().build());
        event.put(ModEntityTypes.HORSEMAN.get(), HorsemanEntity.setAttributes().build());
        event.put(ModEntityTypes.MESSENGER.get(), MessengerEntity.setAttributes().build());
        event.put(ModEntityTypes.PATROL_LEADER.get(), CommanderEntity.setAttributes().build());
        event.put(ModEntityTypes.CAPTAIN.get(), CaptainEntity.setAttributes().build());
        event.put(ModEntityTypes.SCOUT.get(), ScoutEntity.setAttributes().build());
        event.put(ModEntityTypes.VILLAGER_NOBLE.get(), VillagerNobleEntity.setAttributes().build());
    }
}
