package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

final class WorldMapRenderPrimitives {
    private static final int FULL_BRIGHT = 0xF000F0;

    private WorldMapRenderPrimitives() {
    }

    static int screenX(double worldX, double offsetX, double scale) {
        return (int) Math.round(offsetX + worldX * scale);
    }

    static int screenZ(double worldZ, double offsetZ, double scale) {
        return (int) Math.round(offsetZ + worldZ * scale);
    }

    static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xB8201810);
        graphics.renderOutline(x, y, width, height, 0xFF8A6A3A);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, 0x66301810);
    }

    static void button(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                       int x, int y, int width, int height, String label,
                       int labelColor, boolean selected) {
        boolean hovered = contains(mouseX, mouseY, x, y, width, height);
        int bg = selected ? 0xCC5A4025 : (hovered ? 0xCC4B3928 : 0xB82A2119);
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.renderOutline(x, y, width, height, hovered || selected ? 0xFFE0B86A : 0xAA8A6A3A);
        graphics.drawCenteredString(font, label, x + width / 2, y + (height - 8) / 2, labelColor);
    }

    static void button(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                       int x, int y, int width, int height, Component label,
                       int labelColor, boolean selected) {
        boolean hovered = contains(mouseX, mouseY, x, y, width, height);
        int bg = selected ? 0xCC5A4025 : (hovered ? 0xCC4B3928 : 0xB82A2119);
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.renderOutline(x, y, width, height, hovered || selected ? 0xFFE0B86A : 0xAA8A6A3A);
        graphics.drawCenteredString(font, label, x + width / 2, y + (height - 8) / 2, labelColor);
    }

    static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    static void solidLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        rasterLine(graphics, x1, y1, x2, y2, color, 1, 0);
    }

    static void dashedLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        rasterLine(graphics, x1, y1, x2, y2, color, 4, 3);
    }

    static void texturedIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y,
                             int atlasIndex, float scale, int argb) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, scale);

        float u0 = (atlasIndex % 16) / 16f;
        float v0 = (atlasIndex / 16) / 16f;
        float u1 = u0 + 1f / 16f;
        float v1 = v0 + 1f / 16f;

        graphics.flush();
        VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.text(texture));
        Matrix4f matrix = pose.last().pose();
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        iconVertex(consumer, matrix, -1f, 1f, u0, v0, r, g, b, a);
        iconVertex(consumer, matrix, 1f, 1f, u1, v0, r, g, b, a);
        iconVertex(consumer, matrix, 1f, -1f, u1, v1, r, g, b, a);
        iconVertex(consumer, matrix, -1f, -1f, u0, v1, r, g, b, a);
        graphics.flush();

        pose.popPose();
    }

    private static void rasterLine(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                                   int color, int dashCycle, int dashSkip) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int step = 0;

        while (true) {
            if (dashCycle <= 1 || step % dashCycle != dashSkip) {
                graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            }
            if (x1 == x2 && y1 == y2) {
                break;
            }
            int e2 = err * 2;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
            step++;
            if (step > 8192) {
                break;
            }
        }
    }

    private static void iconVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
                                   float u, float v, int r, int g, int b, int a) {
        consumer.addVertex(matrix, x, y, 0f)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);
    }
}
