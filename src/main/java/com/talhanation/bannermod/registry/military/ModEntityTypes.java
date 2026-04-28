package com.talhanation.bannermod.registry.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, BannerModMain.MOD_ID);


    public static final DeferredHolder<EntityType<?>, EntityType<RecruitEntity>> RECRUIT = ENTITY_TYPES.register("recruit",
            () -> EntityType.Builder.of(RecruitEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "recruit").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<RecruitShieldmanEntity>> RECRUIT_SHIELDMAN = ENTITY_TYPES.register("recruit_shieldman",
            () -> EntityType.Builder.of(RecruitShieldmanEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "recruit_shield").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BowmanEntity>> BOWMAN = ENTITY_TYPES.register("bowman",
            () -> EntityType.Builder.of(BowmanEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "bowman").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CrossBowmanEntity>> CROSSBOWMAN = ENTITY_TYPES.register("crossbowman",
            () -> EntityType.Builder.of(CrossBowmanEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "crossbowman").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NomadEntity>> NOMAD = ENTITY_TYPES.register("nomad",
            () -> EntityType.Builder.of(NomadEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "nomad").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HorsemanEntity>> HORSEMAN = ENTITY_TYPES.register("horseman",
            () -> EntityType.Builder.of(HorsemanEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "horseman").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MessengerEntity>> MESSENGER = ENTITY_TYPES.register("messenger",
            () -> EntityType.Builder.of(MessengerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "messenger").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ScoutEntity>> SCOUT = ENTITY_TYPES.register("scout",
            () -> EntityType.Builder.of(ScoutEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "scout").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CommanderEntity>> PATROL_LEADER = ENTITY_TYPES.register("patrol_leader",
            () -> EntityType.Builder.of(CommanderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "patrol_leader").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CaptainEntity>> CAPTAIN = ENTITY_TYPES.register("captain",
            () -> EntityType.Builder.of(CaptainEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "captain").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VillagerNobleEntity>> VILLAGER_NOBLE = ENTITY_TYPES.register("villager_noble",
            () -> EntityType.Builder.of(VillagerNobleEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .canSpawnFarFromPlayer()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "villager_noble").toString()));

}
