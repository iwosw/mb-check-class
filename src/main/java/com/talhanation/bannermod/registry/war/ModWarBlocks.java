package com.talhanation.bannermod.registry.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.runtime.SiegeStandardBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModWarBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, BannerModMain.MOD_ID);

    public static final DeferredHolder<Block, Block> SIEGE_STANDARD = BLOCKS.register("siege_standard",
            () -> new SiegeStandardBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(6.0F, 12.0F)
                    .noOcclusion()));
}
