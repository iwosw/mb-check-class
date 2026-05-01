package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import com.talhanation.bannermod.persistence.military.RecruitsRoute.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.List;

public class RouteRenderer {

    private static final ResourceLocation MAP_ICONS = ResourceLocation.parse("textures/map/map_icons.png");

    private static final int COLOR_NORMAL = 0xFFF4E2B8;
    private static final int COLOR_NOT_LOADED = 0xFFD07063;
    private static final int LINE_SHADOW = 0xAA20150D;
    private static final int COLOR_LINE = 0xFFE0B86A;
    private static final int LABEL_FILL = 0xB8201810;
    private static final int LABEL_BORDER = 0xAA8A6A3A;
    private static final int ICON_INDEX = 6;

    // -------------------------------------------------------------------------

    public static void renderRoute(GuiGraphics guiGraphics, RecruitsRoute route,
                                   double offsetX, double offsetZ, double scale) {
        renderRoute(guiGraphics, route, offsetX, offsetZ, scale, null, -1);
    }

    public static void renderRoute(GuiGraphics guiGraphics, RecruitsRoute route,
                                   double offsetX, double offsetZ, double scale,
                                   @Nullable Waypoint draggingWaypoint, int dragInsertIndex) {
        if (route == null || route.getWaypoints().isEmpty()) return;

        List<Waypoint> waypoints = route.getWaypoints();

        renderLines(guiGraphics, waypoints, offsetX, offsetZ, scale, draggingWaypoint);
        renderInsertionIndicator(guiGraphics, waypoints, offsetX, offsetZ, scale,
                draggingWaypoint, dragInsertIndex);

        for (int i = 0; i < waypoints.size(); i++) {
            boolean isDragging = waypoints.get(i) == draggingWaypoint;
            renderWaypointIcon(guiGraphics, waypoints.get(i), i + 1, offsetX, offsetZ, scale, 0xFF, isDragging);
        }
    }

    public static void renderDragGhost(GuiGraphics guiGraphics, Waypoint waypoint, int mouseX, int mouseY) {
        if (waypoint == null) return;
        renderIconAt(guiGraphics, mouseX, mouseY, ICON_INDEX, COLOR_NORMAL);
        if (waypoint.getAction() != null) {
            String label = waypoint.getAction().toString();
            drawLabelChip(guiGraphics, label, mouseX, mouseY + 6, COLOR_NORMAL, LABEL_BORDER);
        }
    }

    // -------------------------------------------------------------------------

    private static void renderLines(GuiGraphics guiGraphics, List<Waypoint> waypoints,
                                    double offsetX, double offsetZ, double scale,
                                    @Nullable Waypoint draggingWaypoint) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint a = waypoints.get(i);
            Waypoint b = waypoints.get(i + 1);
            if (a == draggingWaypoint || b == draggingWaypoint) continue;
            int x1 = WorldMapRenderPrimitives.screenX(a.getPosition().getX(), offsetX, scale);
            int z1 = WorldMapRenderPrimitives.screenZ(a.getPosition().getZ(), offsetZ, scale);
            int x2 = WorldMapRenderPrimitives.screenX(b.getPosition().getX(), offsetX, scale);
            int z2 = WorldMapRenderPrimitives.screenZ(b.getPosition().getZ(), offsetZ, scale);
            int lineColor = isChunkLoaded(a) && isChunkLoaded(b) ? COLOR_LINE : COLOR_NOT_LOADED;
            WorldMapRenderPrimitives.solidLine(guiGraphics, x1, z1, x2, z2, LINE_SHADOW);
            WorldMapRenderPrimitives.solidLine(guiGraphics, x1, z1 - 1, x2, z2 - 1, lineColor);
        }
    }

    private static void renderInsertionIndicator(GuiGraphics guiGraphics, List<Waypoint> waypoints,
                                                  double offsetX, double offsetZ, double scale,
                                                  @Nullable Waypoint draggingWaypoint, int insertIndex) {
        if (draggingWaypoint == null || insertIndex < 0) return;

        java.util.List<Waypoint> without = new java.util.ArrayList<>(waypoints);
        without.remove(draggingWaypoint);

        int clampedIdx = Math.max(0, Math.min(insertIndex, without.size()));
        Waypoint prev = clampedIdx > 0              ? without.get(clampedIdx - 1) : null;
        Waypoint next = clampedIdx < without.size() ? without.get(clampedIdx)     : null;

        if (prev != null) {
            WorldMapRenderPrimitives.dashedLine(guiGraphics,
                    WorldMapRenderPrimitives.screenX(prev.getPosition().getX(), offsetX, scale),
                    WorldMapRenderPrimitives.screenZ(prev.getPosition().getZ(), offsetZ, scale),
                    WorldMapRenderPrimitives.screenX(draggingWaypoint.getPosition().getX(), offsetX, scale),
                    WorldMapRenderPrimitives.screenZ(draggingWaypoint.getPosition().getZ(), offsetZ, scale),
                    0xFF00FFFF);
        }
        if (next != null) {
            WorldMapRenderPrimitives.dashedLine(guiGraphics,
                    WorldMapRenderPrimitives.screenX(draggingWaypoint.getPosition().getX(), offsetX, scale),
                    WorldMapRenderPrimitives.screenZ(draggingWaypoint.getPosition().getZ(), offsetZ, scale),
                    WorldMapRenderPrimitives.screenX(next.getPosition().getX(), offsetX, scale),
                    WorldMapRenderPrimitives.screenZ(next.getPosition().getZ(), offsetZ, scale),
                    0xFF00FFFF);
        }
    }

    private static void renderWaypointIcon(GuiGraphics guiGraphics, Waypoint waypoint, int number,
                                           double offsetX, double offsetZ, double scale, int alpha,
                                           boolean isDragging) {
        int pixelX = WorldMapRenderPrimitives.screenX(waypoint.getPosition().getX(), offsetX, scale);
        int pixelZ = WorldMapRenderPrimitives.screenZ(waypoint.getPosition().getZ(), offsetZ, scale);

        boolean loaded = isChunkLoaded(waypoint);
        int color = loaded ? COLOR_NORMAL : COLOR_NOT_LOADED;
        int argb  = (alpha << 24) | (color & 0x00FFFFFF);

        renderIconAt(guiGraphics, pixelX, pixelZ, ICON_INDEX, argb);

        String numStr = String.valueOf(number);
        drawLabelChip(guiGraphics, numStr, pixelX, pixelZ - 12, argb, loaded ? LABEL_BORDER : COLOR_NOT_LOADED);

        if (scale > 2.0 && !isDragging) {
            Component label = null;
            if (!loaded) {
                label = Component.translatable("gui.recruits.map.route.not_loaded");
            } else if (waypoint.getAction() != null) {
                label = Component.literal(waypoint.getAction().toString());
            }
            if (label != null) {
                drawLabelChip(guiGraphics, label.getString(), pixelX, pixelZ + 6, argb,
                        loaded ? LABEL_BORDER : COLOR_NOT_LOADED);
            }
        }
    }

    private static boolean isChunkLoaded(Waypoint waypoint) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;
        ChunkPos chunk = new ChunkPos(
                waypoint.getPosition().getX() >> 4,
                waypoint.getPosition().getZ() >> 4);
        // Must be both explored on the map AND currently loaded in the level
        // so that surface Y can be resolved accurately when patrolling starts.
        if (!ChunkTileManager.getInstance().isChunkExplored(chunk)) return false;
        return level.getChunkSource().getChunk(chunk.x, chunk.z, false) != null;
    }

    private static void renderIconAt(GuiGraphics guiGraphics, int pixelX, int pixelZ,
                                     int iconIndex, int color) {
        WorldMapRenderPrimitives.texturedIcon(guiGraphics, MAP_ICONS, pixelX, pixelZ, iconIndex, 3.0F, color);
    }

    private static void drawLabelChip(GuiGraphics guiGraphics, String label, int centerX, int y, int textColor, int borderColor) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width(label) + 6;
        int x = centerX - width / 2;
        guiGraphics.fill(x, y, x + width, y + 10, LABEL_FILL);
        guiGraphics.renderOutline(x, y, width, 10, borderColor);
        guiGraphics.drawString(minecraft.font, label, x + 3, y + 1, textColor, false);
    }

    // -------------------------------------------------------------------------

    @Nullable
    public static Waypoint getWaypointAt(RecruitsRoute route, double mouseX, double mouseY,
                                         double offsetX, double offsetZ, double scale) {
        if (route == null) return null;
        int hitRadius = Math.max(5, (int)(8 * scale / 2.0));
        for (Waypoint wp : route.getWaypoints()) {
            int px = WorldMapRenderPrimitives.screenX(wp.getPosition().getX(), offsetX, scale);
            int pz = WorldMapRenderPrimitives.screenZ(wp.getPosition().getZ(), offsetZ, scale);
            if (Math.abs(mouseX - px) <= hitRadius && Math.abs(mouseY - pz) <= hitRadius) return wp;
        }
        return null;
    }

    public static int computeInsertIndex(RecruitsRoute route, Waypoint dragging,
                                         double mouseX, double mouseY,
                                         double offsetX, double offsetZ, double scale) {
        if (route == null) return 0;

        List<Waypoint> without = new java.util.ArrayList<>(route.getWaypoints());
        without.remove(dragging);
        if (without.isEmpty()) return 0;

        int bestIndex = 0;
        double bestDist = Double.MAX_VALUE;

        double fx = offsetX + without.get(0).getPosition().getX() * scale;
        double fz = offsetZ + without.get(0).getPosition().getZ() * scale;
        double d = Math.hypot(mouseX - fx, mouseY - fz);
        double bias = (mouseX < fx) ? 0 : 20;
        if (d + bias < bestDist) { bestDist = d + bias; bestIndex = 0; }

        for (int i = 0; i < without.size() - 1; i++) {
            double mx = (offsetX + without.get(i).getPosition().getX() * scale
                       + offsetX + without.get(i+1).getPosition().getX() * scale) / 2.0;
            double mz = (offsetZ + without.get(i).getPosition().getZ() * scale
                       + offsetZ + without.get(i+1).getPosition().getZ() * scale) / 2.0;
            d = Math.hypot(mouseX - mx, mouseY - mz);
            if (d < bestDist) { bestDist = d; bestIndex = i + 1; }
        }

        double lx = offsetX + without.get(without.size()-1).getPosition().getX() * scale;
        double lz = offsetZ + without.get(without.size()-1).getPosition().getZ() * scale;
        d = Math.hypot(mouseX - lx, mouseY - lz);
        bias = (mouseX > lx) ? 0 : 20;
        if (d + bias < bestDist) bestIndex = without.size();

        return bestIndex;
    }
}
