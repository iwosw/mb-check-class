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
        int targetPxX = (int) Math.round(targetX * scale + offsetX);
        int targetPxZ = (int) Math.round(targetZ * scale + offsetZ);
        int originPxX = (int) Math.round(originX * scale + offsetX);
        int originPxZ = (int) Math.round(originZ * scale + offsetZ);

        drawLine(graphics, originPxX, originPxZ, targetPxX, targetPxZ, withAlpha(LINE_COLOR, alpha * 0.85F));

        double pulse = Math.sin((elapsed / 130D) * Math.PI) * 0.5D + 0.5D;
        int radius = 3 + (int) Math.round(pulse * 4D);
        int outerColor = withAlpha(DOT_OUTER_COLOR, alpha);
        int innerColor = withAlpha(DOT_INNER_COLOR, alpha);

        graphics.fill(targetPxX - radius - 1, targetPxZ - radius - 1, targetPxX + radius + 2, targetPxZ + radius + 2, outerColor);
        graphics.fill(targetPxX - radius, targetPxZ - radius, targetPxX + radius + 1, targetPxZ + radius + 1, innerColor);
    }

    private static void drawLine(GuiGraphics graphics, int x1, int z1, int x2, int z2, int color) {
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;
        int x = x1;
        int z = z1;
        int step = 0;
        while (true) {
            if ((step & 3) != 3) {
                graphics.fill(x, z, x + 1, z + 1, color);
            }
            if (x == x2 && z == z2) break;
            int e2 = err * 2;
            if (e2 > -dz) {
                err -= dz;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                z += sz;
            }
            step++;
            if (step > 4096) break;
        }
    }

    private static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
