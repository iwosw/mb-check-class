package com.talhanation.bannermod.client.military.gui.worldmap;

import net.minecraft.client.gui.GuiGraphics;

final class WorldMapMoveOrderMarker {
    private static final long DURATION_MS = 2200L;
    private static final int DOT_OUTER_COLOR = 0xFFFFFFFF;
    private static final int DOT_INNER_COLOR = 0xFFFFCC33;
    private static final int LINE_COLOR = 0xFFFFCC33;

    private static double originX;
    private static double originZ;
    private static double targetX;
    private static double targetZ;
    private static long startedAtMs;
    private static boolean active;

    private WorldMapMoveOrderMarker() {
    }

    static void trigger(double originWorldX, double originWorldZ, double targetWorldX, double targetWorldZ) {
        originX = originWorldX;
        originZ = originWorldZ;
        targetX = targetWorldX;
        targetZ = targetWorldZ;
        startedAtMs = System.currentTimeMillis();
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

        graphics.fill(targetPxX - radius - 1, targetPxZ - radius - 1, targetPxX + radius + 2, targetPxZ + radius + 2, outerColor);
        graphics.fill(targetPxX - radius, targetPxZ - radius, targetPxX + radius + 1, targetPxZ + radius + 1, innerColor);
    }

    private static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
