package com.talhanation.bannermod.client.military.gui.worldmap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

final class WorldMapMoveOrderMarker {
    private static final long DURATION_MS = 2200L;
    private static final int DOT_OUTER_COLOR = 0xFFF4E2B8;
    private static final int DOT_INNER_COLOR = 0xFFE0B86A;
    private static final int LINE_COLOR = 0xFFE0B86A;
    private static final int PANEL_FILL = 0xB8201810;
    private static final int PANEL_BORDER = 0xFFE0B86A;
    private static final int DETAIL_TEXT = 0xFFE8D9BF;

    private static double originX;
    private static double originZ;
    private static double targetX;
    private static double targetZ;
    private static long startedAtMs;
    private static String title;
    private static String detail;
    private static boolean active;

    private WorldMapMoveOrderMarker() {
    }

    static void trigger(double originWorldX, double originWorldZ, double targetWorldX, double targetWorldZ,
                        Component titleText, Component detailText) {
        originX = originWorldX;
        originZ = originWorldZ;
        targetX = targetWorldX;
        targetZ = targetWorldZ;
        startedAtMs = System.currentTimeMillis();
        title = titleText.getString();
        detail = detailText.getString();
        active = true;
    }

    static void render(GuiGraphics graphics, double offsetX, double offsetZ, double scale) {
        if (!active) return;
        long now = System.currentTimeMillis();
        long elapsed = now - startedAtMs;
        if (elapsed >= DURATION_MS) {
            active = false;
            return;
        }

        float progress = elapsed / (float) DURATION_MS;
        float alpha = 1F - progress;
        int targetPxX = WorldMapRenderPrimitives.screenX(targetX, offsetX, scale);
        int targetPxZ = WorldMapRenderPrimitives.screenZ(targetZ, offsetZ, scale);
        int originPxX = WorldMapRenderPrimitives.screenX(originX, offsetX, scale);
        int originPxZ = WorldMapRenderPrimitives.screenZ(originZ, offsetZ, scale);

        WorldMapRenderPrimitives.dashedLine(graphics, originPxX, originPxZ, targetPxX, targetPxZ,
                withAlpha(LINE_COLOR, alpha * 0.85F));

        double pulse = Math.sin((elapsed / 130D) * Math.PI) * 0.5D + 0.5D;
        int radius = 3 + (int) Math.round(pulse * 4D);
        int outerColor = withAlpha(DOT_OUTER_COLOR, alpha);
        int innerColor = withAlpha(DOT_INNER_COLOR, alpha);

        graphics.fill(targetPxX - 1, targetPxZ - radius - 2, targetPxX + 2, targetPxZ + radius + 3, outerColor);
        graphics.fill(targetPxX - radius - 2, targetPxZ - 1, targetPxX + radius + 3, targetPxZ + 2, outerColor);
        graphics.fill(targetPxX - 1, targetPxZ - radius, targetPxX + 2, targetPxZ + radius + 1, innerColor);
        graphics.fill(targetPxX - radius, targetPxZ - 1, targetPxX + radius + 1, targetPxZ + 2, innerColor);
        renderStatusPanel(graphics, targetPxX + radius + 7, targetPxZ - 11, alpha);
    }

    private static void renderStatusPanel(GuiGraphics graphics, int x, int y, float alpha) {
        if (title == null || detail == null) {
            return;
        }
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        int width = Math.max(minecraft.font.width(title), minecraft.font.width(detail)) + 8;
        int height = 22;
        graphics.fill(x, y, x + width, y + height, withAlpha(PANEL_FILL, alpha));
        graphics.renderOutline(x, y, width, height, withAlpha(PANEL_BORDER, alpha));
        graphics.drawString(minecraft.font, title, x + 4, y + 4, withAlpha(DOT_OUTER_COLOR, alpha), false);
        graphics.drawString(minecraft.font, detail, x + 4, y + 13, withAlpha(DETAIL_TEXT, alpha), false);
    }

    private static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
