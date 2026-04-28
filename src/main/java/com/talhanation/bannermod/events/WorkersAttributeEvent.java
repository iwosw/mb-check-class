package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.civilian.*;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = com.talhanation.bannermod.bootstrap.BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class WorkersAttributeEvent {

    @SubscribeEvent
    public static void entityAttributeEvent(final EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FARMER.get(), FarmerEntity.setAttributes().build());
        event.put(ModEntityTypes.LUMBERJACK.get(), LumberjackEntity.setAttributes().build());
        event.put(ModEntityTypes.MINER.get(), MinerEntity.setAttributes().build());
        event.put(ModEntityTypes.BUILDER.get(), BuilderEntity.setAttributes().build());
        event.put(ModEntityTypes.MERCHANT.get(), MerchantEntity.setAttributes().build());
        event.put(ModEntityTypes.FISHERMAN.get(), FishermanEntity.setAttributes().build());
        event.put(ModEntityTypes.ANIMAL_FARMER.get(), AnimalFarmerEntity.setAttributes().build());
        /*
        event.put(ModEntityTypes.SHEPHERD.get(), ShepherdEntity.setAttributes().build());
        event.put(ModEntityTypes.CATTLE_FARMER.get(), CattleFarmerEntity.setAttributes().build());
        event.put(ModEntityTypes.CHICKEN_FARMER.get(), ChickenFarmerEntity.setAttributes().build());
        event.put(ModEntityTypes.SWINEHERD.get(), SwineherdEntity.setAttributes().build());
        event.put(ModEntityTypes.RABBIT_FARMER.get(), RabbitFarmerEntity.setAttributes().build());
        event.put(ModEntityTypes.BEEKEEPER.get(), BeekeeperEntity.setAttributes().build());
         */
    }
}