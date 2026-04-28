package com.talhanation.bannermod.registry.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, BannerModMain.MOD_ID);

    public static final DeferredHolder<Block, Block> RECRUIT_BLOCK = BLOCKS.register("recruit_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    public static final DeferredHolder<Block, Block> BOWMAN_BLOCK = BLOCKS.register("bowman_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    public static final DeferredHolder<Block, Block> NOMAD_BLOCK = BLOCKS.register("nomad_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    public static final DeferredHolder<Block, Block> CROSSBOWMAN_BLOCK = BLOCKS.register("crossbowman_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    public static final DeferredHolder<Block, Block> HORSEMAN_BLOCK = BLOCKS.register("horseman_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

    public static final DeferredHolder<Block, Block> RECRUIT_SHIELD_BLOCK = BLOCKS.register("recruit_shield_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

}
