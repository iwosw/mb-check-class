package com.talhanation.bannermod.registry.civilian;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModProfessions {
    private static final Logger logger = LogManager.getLogger(BannerModMain.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, BannerModMain.MOD_ID);

    private static RegistryObject<VillagerProfession> makeProfession(String name,
            RegistryObject<PoiType> pointOfInterest) {
        logger.info("Registering profession for {} with POI {}", name, pointOfInterest);
        return PROFESSIONS.register(name,
                () -> new VillagerProfession(name, poi -> poi.get() == pointOfInterest.get(),
                        poi -> poi.get() == pointOfInterest.get(), ImmutableSet.of(), ImmutableSet.of(),
                        SoundEvents.VILLAGER_CELEBRATE));
    }
}
