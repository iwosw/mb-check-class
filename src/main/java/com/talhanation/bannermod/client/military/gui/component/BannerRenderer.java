package com.talhanation.bannermod.client.military.gui.component;

import com.mojang.blaze3d.platform.Lighting;
import com.talhanation.bannermod.client.military.render.BannerPatternRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

public class BannerRenderer {
    private BannerPatternLayers patternLayers;
    private DyeColor baseColor = DyeColor.WHITE;
    private final ModelPart flag;
    private ItemStack bannerItem = ItemStack.EMPTY;

    public BannerRenderer(@Nullable ItemStack bannerItem) {
        this.patternLayers = BannerPatternLayers.EMPTY;

        this.flag = Minecraft.getInstance().getEntityModels()
                .bakeLayer(ModelLayers.BANNER)
                .getChild("flag");
        setBannerItem(bannerItem == null ? ItemStack.EMPTY : bannerItem);
    }

    public void renderBanner(GuiGraphics guiGraphics, int left, int top, int width, int height, int scale0) {
        if (bannerItem.isEmpty()) return;

        Lighting.setupForFlatItems();
        guiGraphics.pose().pushPose();
        try {
            guiGraphics.pose().translate(left + 10, top + 20, 0.0D);
            guiGraphics.pose().scale(scale0, -scale0, 1.0F);

            float scale = 0.6666667F;
            guiGraphics.pose().scale(scale, -scale, -scale);
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            BannerPatternRenderHelper.render(guiGraphics.pose(), bufferSource, this.flag, 15728880,
                    this.baseColor, this.patternLayers);
            bufferSource.endBatch();
        } finally {
            guiGraphics.pose().popPose();
            Lighting.setupFor3DItems();
        }
    }

    public void setBannerItem(ItemStack bannerItem) {
        if (bannerItem != null && bannerItem.getItem() instanceof BannerItem item) {
            this.bannerItem = bannerItem;
            this.baseColor = item.getColor();
            this.patternLayers = bannerItem.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        } else {
            this.bannerItem = ItemStack.EMPTY;
            this.baseColor = DyeColor.WHITE;
            this.patternLayers = BannerPatternLayers.EMPTY;
        }
    }

}
