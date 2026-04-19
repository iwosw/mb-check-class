package com.talhanation.bannermod.registry.citizen;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * DeferredRegister for the unified {@link CitizenEntity} type. Kept in
 * its own registry class (separate from {@code registry.civilian} and
 * {@code registry.military}) so the Citizen-base migration stays
 * cleanly scoped and the eventual Cit-07 retirement of legacy civilian
 * / military registrations doesn't risk touching unrelated code.
 */
public final class ModCitizenEntityTypes {

    private ModCitizenEntityTypes() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.ENTITY_TYPES, BannerModMain.MOD_ID);

    public static final RegistryObject<EntityType<CitizenEntity>> CITIZEN = ENTITY_TYPES.register("citizen",
            () -> EntityType.Builder.<CitizenEntity>of(CitizenEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .canSpawnFarFromPlayer()
                    .setTrackingRange(32)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .build(new ResourceLocation(BannerModMain.MOD_ID, "citizen").toString()));
}
