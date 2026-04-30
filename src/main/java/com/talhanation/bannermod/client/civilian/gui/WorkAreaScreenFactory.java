package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AnimalPenArea;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.entity.civilian.workarea.FishingArea;
import com.talhanation.bannermod.entity.civilian.workarea.LumberArea;
import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import com.talhanation.bannermod.entity.civilian.workarea.MiningArea;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class WorkAreaScreenFactory {
    private WorkAreaScreenFactory() {
    }

    @Nullable
    public static Screen create(AbstractWorkAreaEntity areaEntity, Player player) {
        if (areaEntity instanceof BuildArea buildArea) {
            return new BuildAreaScreen(buildArea, player);
        }
        if (areaEntity instanceof MiningArea miningArea) {
            return new MiningAreaScreen(miningArea, player);
        }
        if (areaEntity instanceof FishingArea fishingArea) {
            return new FishingAreaScreen(fishingArea, player);
        }
        if (areaEntity instanceof StorageArea storageArea) {
            return new StorageAreaScreen(storageArea, player);
        }
        if (areaEntity instanceof MarketArea marketArea) {
            return new MarketAreaScreen(marketArea, player);
        }
        if (areaEntity instanceof CropArea cropArea) {
            return new CropAreaScreen(cropArea, player);
        }
        if (areaEntity instanceof LumberArea lumberArea) {
            return new LumberAreaScreen(lumberArea, player);
        }
        if (areaEntity instanceof AnimalPenArea animalPenArea) {
            return new AnimalPenAreaScreen(animalPenArea, player);
        }
        return null;
    }
}
