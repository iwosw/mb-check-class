package com.talhanation.bannermod.registry.civilian;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, BannerModMain.MOD_ID);
}
