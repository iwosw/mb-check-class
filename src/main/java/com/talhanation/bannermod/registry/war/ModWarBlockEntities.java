package com.talhanation.bannermod.registry.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.runtime.SiegeStandardBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModWarBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BannerModMain.MOD_ID);

    public static final RegistryObject<BlockEntityType<SiegeStandardBlockEntity>> SIEGE_STANDARD =
            BLOCK_ENTITIES.register("siege_standard",
                    () -> BlockEntityType.Builder
                            .of(SiegeStandardBlockEntity::new, ModWarBlocks.SIEGE_STANDARD.get())
                            .build(null));
}
