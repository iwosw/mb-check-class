package com.talhanation.bannermod.registry.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.runtime.SiegeStandardBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModWarBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BannerModMain.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SiegeStandardBlockEntity>> SIEGE_STANDARD =
            BLOCK_ENTITIES.register("siege_standard",
                    () -> BlockEntityType.Builder
                            .of(SiegeStandardBlockEntity::new, ModWarBlocks.SIEGE_STANDARD.get())
                            .build(null));
}
