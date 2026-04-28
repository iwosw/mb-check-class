package com.talhanation.bannermod.registry.civilian;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPois {
    private static final Logger logger = LogManager.getLogger(BannerModMain.MOD_ID);
    public static final DeferredRegister<PoiType> POIS =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, BannerModMain.MOD_ID);

    private static DeferredHolder<PoiType, PoiType> makePoi(String name, DeferredHolder<Block, Block> block) {
        logger.info("Registering POI for " + block.getKey().toString());
        return POIS.register(name, () -> {
            Set<BlockState> blockStates = ImmutableSet.copyOf(block.get().getStateDefinition().getPossibleStates());
            return new PoiType(blockStates, 1, 1);
        });
    }
}
