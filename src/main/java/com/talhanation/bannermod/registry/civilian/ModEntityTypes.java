package com.talhanation.bannermod.registry.civilian;

import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.entity.civilian.*;
import com.talhanation.bannermod.entity.civilian.workarea.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {

        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(Registries.ENTITY_TYPE, BannerModMain.MOD_ID);

        public static final DeferredHolder<EntityType<?>, EntityType<CropArea>> CROPAREA = ENTITY_TYPES.register("croparea",
                () -> EntityType.Builder.of(CropArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "croparea").toString()));
        public static final DeferredHolder<EntityType<?>, EntityType<LumberArea>> LUMBERAREA = ENTITY_TYPES.register("lumberarea",
                () -> EntityType.Builder.of(LumberArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "lumberarea").toString()));
        public static final DeferredHolder<EntityType<?>, EntityType<BuildArea>> BUILDAREA = ENTITY_TYPES.register("buildarea",
                () -> EntityType.Builder.of(BuildArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "buildarea").toString()));
        public static final DeferredHolder<EntityType<?>, EntityType<MiningArea>> MININGAREA = ENTITY_TYPES.register("miningarea",
                () -> EntityType.Builder.of(MiningArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "miningarea").toString()));
        public static final DeferredHolder<EntityType<?>, EntityType<StorageArea>> STORAGEAREA = ENTITY_TYPES.register("storagearea",
                () -> EntityType.Builder.of(StorageArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "storagearea").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<MarketArea>> MARKETAREA = ENTITY_TYPES.register("marketarea",
                () -> EntityType.Builder.of(MarketArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "marketarea").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<FishingArea>> FISHINGAREA = ENTITY_TYPES.register("fishingarea",
                () -> EntityType.Builder.of(FishingArea::new, MobCategory.MISC)
                        .sized(1.2F, 2.00F)
                        .fireImmune().noSummon()
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "fishingarea").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<AnimalPenArea>> ANIMAL_PEN_AREA = ENTITY_TYPES.register("animalpenarea",
            () -> EntityType.Builder.of(AnimalPenArea::new, MobCategory.MISC)
                    .sized(1.2F, 2.00F)
                    .fireImmune().noSummon()
                    .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "animalpenarea").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<AnimalFarmerEntity>> ANIMAL_FARMER = ENTITY_TYPES.register("animal_farmer",
                        () -> EntityType.Builder.of(AnimalFarmerEntity::new, MobCategory.CREATURE)
                                        .sized(0.6F, 1.95F)
                                        .canSpawnFarFromPlayer()
                                        .setTrackingRange(32)
                                        .setShouldReceiveVelocityUpdates(true)
                                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "animal_farmer").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<LumberjackEntity>> LUMBERJACK = ENTITY_TYPES.register("lumberjack",
                () -> EntityType.Builder.of(LumberjackEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "lumberjack").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<FarmerEntity>> FARMER = ENTITY_TYPES.register("farmer",
                () -> EntityType.Builder.of(FarmerEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "farmer").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<MinerEntity>> MINER = ENTITY_TYPES.register("miner",
                () -> EntityType.Builder.of(MinerEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "miner").toString()));

        public static final DeferredHolder<EntityType<?>, EntityType<BuilderEntity>> BUILDER = ENTITY_TYPES.register("builder",
                () -> EntityType.Builder.of(BuilderEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "builder").toString()));

       public static final DeferredHolder<EntityType<?>, EntityType<MerchantEntity>> MERCHANT = ENTITY_TYPES.register("merchant",
                () -> EntityType.Builder.of(MerchantEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "merchant").toString()));


       public static final DeferredHolder<EntityType<?>, EntityType<FishermanEntity>> FISHERMAN = ENTITY_TYPES.register("fisherman",
                () -> EntityType.Builder.of(FishermanEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 1.95F)
                        .canSpawnFarFromPlayer()
                        .setTrackingRange(32)
                        .setShouldReceiveVelocityUpdates(true)
                        .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "fisherman").toString()));

       public static final DeferredHolder<EntityType<?>, EntityType<FishingBobberEntity>> FISHING_BOBBER = ENTITY_TYPES.register("fishing_bobber",
                        () -> EntityType.Builder.<FishingBobberEntity>of(FishingBobberEntity::new, MobCategory.MISC)
                                .sized(0.25F, 0.25F)
                                .clientTrackingRange(4)
                                .updateInterval(5)
                                .build(ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "fishing_bobber").toString()));
}
