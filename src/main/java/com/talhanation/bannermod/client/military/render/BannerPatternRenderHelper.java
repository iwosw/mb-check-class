package com.talhanation.bannermod.client.military.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public final class BannerPatternRenderHelper {
    private BannerPatternRenderHelper() {
    }

    public static void render(PoseStack poseStack,
                              MultiBufferSource buffer,
                              ModelPart flag,
                              int packedLight,
                              DyeColor baseColor,
                              BannerPatternLayers patternLayers) {
        flag.xRot = 0.0F;
        flag.y = -32.0F;
        net.minecraft.client.renderer.blockentity.BannerRenderer.renderPatterns(
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                flag, Sheets.BANNER_BASE, true, baseColor, patternLayers);
    }

    public static DyeColor nearestDyeColor(int argb) {
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        DyeColor nearest = DyeColor.WHITE;
        int nearestDistance = Integer.MAX_VALUE;
        for (DyeColor dyeColor : DyeColor.values()) {
            int dyeRgb = dyeColor.getTextureDiffuseColor();
            int dyeRed = (dyeRgb >> 16) & 0xFF;
            int dyeGreen = (dyeRgb >> 8) & 0xFF;
            int dyeBlue = dyeRgb & 0xFF;
            int distance = square(red - dyeRed) + square(green - dyeGreen) + square(blue - dyeBlue);
            if (distance < nearestDistance) {
                nearest = dyeColor;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static int square(int value) {
        return value * value;
    }
}
