package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.entity.civilian.workarea.FishingArea;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;


import java.util.List;


public class FishingAreaScreen extends WorkAreaScreen {
    private static final MutableComponent TEXT_SHEAR_LEAVES = Component.translatable("gui.workers.checkbox.shearLeaves");
    public final FishingArea fishingArea;
    public FishingAreaScreen(FishingArea fishingArea, Player player) {
        super(fishingArea.getCustomName(), fishingArea, player);
        this.fishingArea = fishingArea;
    }

    @Override
    protected void init() {
        setButtons();
    }

    @Override
    public void setButtons() {
        super.setButtons();
    }

    @Override
    protected List<Component> getSettingSummaryLines() {
        return List.of(
                Component.translatable("gui.bannermod.work_area.fishing.summary"),
                Component.translatable("gui.bannermod.work_area.fishing.hint")
        );
    }

}
