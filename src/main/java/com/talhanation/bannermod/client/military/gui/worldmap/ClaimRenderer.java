package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.gui.worldmap.ChunkTileManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimRenderer {
    private static final int LABEL_FILL = 0xB8201810;
    private static final int LABEL_BORDER = 0xCC8A6A3A;
    private static final int OUTLINE_SHADOW = 0xAA20150D;
    private static final int SELECTED_BORDER = 0xFFF4E2B8;
    private static final int SELECTED_SHADOW = 0xCC8A6A3A;

    public static void renderClaimsOverlay(GuiGraphics guiGraphics, RecruitsClaim selectedClaim, double offsetX, double offsetZ, double scale) {
        Minecraft minecraft = Minecraft.getInstance();
        renderClaimsOverlay(guiGraphics, selectedClaim, offsetX, offsetZ, scale,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    public static void renderClaimsOverlay(GuiGraphics guiGraphics, RecruitsClaim selectedClaim, double offsetX, double offsetZ, double scale, int viewportWidth, int viewportHeight) {
        if (ClientManager.recruitsClaims.isEmpty()) return;
        List<RecruitsClaim> visibleClaims = getVisibleClaims(offsetX, offsetZ, scale, viewportWidth, viewportHeight);

        for (RecruitsClaim claim : visibleClaims) {
            renderClaimFill(guiGraphics, claim, offsetX, offsetZ, scale);
        }

        for (RecruitsClaim claim : visibleClaims) {
            renderClaimPassiveOutline(guiGraphics, claim, offsetX, offsetZ, scale);
        }

        for (RecruitsClaim claim : visibleClaims) {
            renderClaimName(guiGraphics, claim, offsetX, offsetZ, scale);
        }

        if (selectedClaim != null && isClaimInViewport(selectedClaim, offsetX, offsetZ, scale, viewportWidth, viewportHeight)) {
            renderClaimSelectedOutline(guiGraphics, selectedClaim, offsetX, offsetZ, scale);
        }
    }

    /**
     * Renders claims with transparent fill so route waypoints underneath are visible.
     * Outlines and selected outline are kept intact; only the fill becomes invisible.
     */
    public static void renderClaimsOverlayTransparent(GuiGraphics guiGraphics, RecruitsClaim selectedClaim, double offsetX, double offsetZ, double scale) {
        Minecraft minecraft = Minecraft.getInstance();
        renderClaimsOverlayTransparent(guiGraphics, selectedClaim, offsetX, offsetZ, scale,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    public static void renderClaimsOverlayTransparent(GuiGraphics guiGraphics, RecruitsClaim selectedClaim, double offsetX, double offsetZ, double scale, int viewportWidth, int viewportHeight) {
        if (ClientManager.recruitsClaims.isEmpty()) return;
        List<RecruitsClaim> visibleClaims = getVisibleClaims(offsetX, offsetZ, scale, viewportWidth, viewportHeight);

        // Skip fill — only draw outlines and selection so the claim boundaries stay visible
        for (RecruitsClaim claim : visibleClaims) {
            renderClaimPassiveOutline(guiGraphics, claim, offsetX, offsetZ, scale);
        }

        if (selectedClaim != null && isClaimInViewport(selectedClaim, offsetX, offsetZ, scale, viewportWidth, viewportHeight)) {
            renderClaimSelectedOutline(guiGraphics, selectedClaim, offsetX, offsetZ, scale);
        }
    }

    private static List<RecruitsClaim> getVisibleClaims(double offsetX, double offsetZ, double scale, int viewportWidth, int viewportHeight) {
        List<RecruitsClaim> visibleClaims = new ArrayList<>();
        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (isClaimInViewport(claim, offsetX, offsetZ, scale, viewportWidth, viewportHeight)) {
                visibleClaims.add(claim);
            }
        }
        return visibleClaims;
    }

    private static boolean isClaimInViewport(RecruitsClaim claim, double offsetX, double offsetZ, double scale, int viewportWidth, int viewportHeight) {
        if (claim.getClaimedChunks().isEmpty()) return false;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (ChunkPos pos : claim.getClaimedChunks()) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minZ = Math.min(minZ, pos.z);
            maxZ = Math.max(maxZ, pos.z);
        }

        int x1 = (int)Math.floor(offsetX + minX * 16.0 * scale);
        int z1 = (int)Math.floor(offsetZ + minZ * 16.0 * scale);
        int x2 = (int)Math.floor(offsetX + (maxX + 1) * 16.0 * scale);
        int z2 = (int)Math.floor(offsetZ + (maxZ + 1) * 16.0 * scale);
        return x2 >= 0 && z2 >= 0 && x1 <= viewportWidth && z1 <= viewportHeight;
    }

    private static final int FOG_FILL_COLOR    = 0x30303030;
    private static final int FOG_OUTLINE_COLOR  = 0x80555555;

    private static boolean isAdminCreative() {
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.hasPermissions(2) && player.isCreative();
    }

    public static boolean isClaimExplored(RecruitsClaim claim) {
        ChunkTileManager tileManager = ChunkTileManager.getInstance();
        for (ChunkPos chunk : claim.getClaimedChunks()) {
            if (tileManager.isChunkExplored(chunk)) return true;
        }
        return false;
    }

    private static void renderClaimFill(GuiGraphics guiGraphics, RecruitsClaim claim, double offsetX, double offsetZ, double scale) {
        if (claim.getClaimedChunks().isEmpty()) return;

        int factionFillColor = (190 << 24) | (getClaimColor(claim) & 0x00FFFFFF);
        boolean adminCreative = isAdminCreative();
        ChunkTileManager tileManager = ChunkTileManager.getInstance();

        for (ChunkPos chunk : claim.getClaimedChunks()) {
            boolean explored = adminCreative || tileManager.isChunkExplored(chunk);
            renderChunk(guiGraphics, chunk, explored ? factionFillColor : FOG_FILL_COLOR, offsetX, offsetZ, scale);
        }
    }

    private static void renderChunk(GuiGraphics guiGraphics, ChunkPos chunk, int color, double offsetX, double offsetZ, double scale) {
        double worldX = chunk.x * 16.0;
        double worldZ = chunk.z * 16.0;

        int x1 = (int) Math.floor(offsetX + worldX * scale);
        int z1 = (int) Math.floor(offsetZ + worldZ * scale);

        int x2 = (int) Math.floor(offsetX + (worldX + 16.0) * scale);
        int z2 = (int) Math.floor(offsetZ + (worldZ + 16.0) * scale);

        if (x2 <= x1) x2 = x1 + 1;
        if (z2 <= z1) z2 = z1 + 1;

        guiGraphics.fill(x1, z1, x2, z2, color);
    }

    private static void renderClaimPassiveOutline(GuiGraphics guiGraphics, RecruitsClaim claim, double offsetX, double offsetZ, double scale) {
        if (claim.getClaimedChunks().isEmpty()) return;

        Set<ChunkPos> chunkSet = new HashSet<>(claim.getClaimedChunks());

        int factionOutlineColor = (200 << 24) | (getClaimColor(claim) & 0x00FFFFFF);
        int thickness = Math.max(1, (int)Math.round(scale * 0.5));
        boolean adminCreative = isAdminCreative();
        ChunkTileManager tileManager = ChunkTileManager.getInstance();

        for (ChunkPos chunk : claim.getClaimedChunks()) {
            boolean explored = adminCreative || !ClientManager.configFogOfWarEnabled || tileManager.isChunkExplored(chunk);
            int outlineColor = explored ? factionOutlineColor : FOG_OUTLINE_COLOR;

            boolean hasTop    = chunkSet.contains(new ChunkPos(chunk.x, chunk.z - 1));
            boolean hasBottom = chunkSet.contains(new ChunkPos(chunk.x, chunk.z + 1));
            boolean hasLeft   = chunkSet.contains(new ChunkPos(chunk.x - 1, chunk.z));
            boolean hasRight  = chunkSet.contains(new ChunkPos(chunk.x + 1, chunk.z));

            double worldX1 = chunk.x * 16.0;
            double worldZ1 = chunk.z * 16.0;

            int x1 = (int)Math.floor(offsetX + worldX1 * scale);
            int z1 = (int)Math.floor(offsetZ + worldZ1 * scale);
            int x2 = (int)Math.floor(offsetX + (worldX1 + 16.0) * scale);
            int z2 = (int)Math.floor(offsetZ + (worldZ1 + 16.0) * scale);

            if (x2 <= x1) x2 = x1 + 1;
            if (z2 <= z1) z2 = z1 + 1;

            if (!hasTop) {
                guiGraphics.fill(x1, z1, x2, z1 + thickness + 1, OUTLINE_SHADOW);
                guiGraphics.fill(x1, z1, x2, z1 + thickness, outlineColor);
            }
            if (!hasBottom) {
                guiGraphics.fill(x1, z2 - thickness - 1, x2, z2, OUTLINE_SHADOW);
                guiGraphics.fill(x1, z2 - thickness, x2, z2, outlineColor);
            }
            if (!hasLeft) {
                guiGraphics.fill(x1, z1, x1 + thickness + 1, z2, OUTLINE_SHADOW);
                guiGraphics.fill(x1, z1, x1 + thickness, z2, outlineColor);
            }
            if (!hasRight) {
                guiGraphics.fill(x2 - thickness - 1, z1, x2, z2, OUTLINE_SHADOW);
                guiGraphics.fill(x2 - thickness, z1, x2, z2, outlineColor);
            }
        }
    }

    private static void renderClaimSelectedOutline(GuiGraphics guiGraphics, RecruitsClaim claim, double offsetX, double offsetZ, double scale) {
        if (claim.getClaimedChunks().isEmpty()) return;

        Set<String> chunkSet = new HashSet<>();
        for (ChunkPos chunk : claim.getClaimedChunks()) {
            chunkSet.add(chunk.x + "," + chunk.z);
        }

        int borderColor = SELECTED_BORDER;
        int shadowColor = SELECTED_SHADOW;
        int borderThickness = Math.max(1, (int)(2 * scale / 2.0));

        for (ChunkPos chunk : claim.getClaimedChunks()) {
            boolean hasTop = chunkSet.contains(chunk.x + "," + (chunk.z - 1));
            boolean hasBottom = chunkSet.contains(chunk.x + "," + (chunk.z + 1));
            boolean hasLeft = chunkSet.contains((chunk.x - 1) + "," + chunk.z);
            boolean hasRight = chunkSet.contains((chunk.x + 1) + "," + chunk.z);

            if (hasTop && hasBottom && hasLeft && hasRight) {
                continue;
            }

            double worldX1 = chunk.x * 16.0;
            double worldZ1 = chunk.z * 16.0;
            double worldX2 = worldX1 + 16.0;
            double worldZ2 = worldZ1 + 16.0;

            int x1 = (int) Math.floor(offsetX + worldX1 * scale);
            int z1 = (int) Math.floor(offsetZ + worldZ1 * scale);
            int x2 = (int) Math.floor(offsetX + worldX2 * scale);
            int z2 = (int) Math.floor(offsetZ + worldZ2 * scale);

            if (!hasTop) {
                guiGraphics.fill(x1, z1, x2, z1 + borderThickness + 1, shadowColor);
                guiGraphics.fill(x1, z1, x2, z1 + borderThickness, borderColor);
            }
            if (!hasBottom) {
                guiGraphics.fill(x1, z2 - borderThickness - 1, x2, z2, shadowColor);
                guiGraphics.fill(x1, z2 - borderThickness, x2, z2, borderColor);
            }
            if (!hasLeft) {
                guiGraphics.fill(x1, z1, x1 + borderThickness + 1, z2, shadowColor);
                guiGraphics.fill(x1, z1, x1 + borderThickness, z2, borderColor);
            }
            if (!hasRight) {
                guiGraphics.fill(x2 - borderThickness - 1, z1, x2, z2, shadowColor);
                guiGraphics.fill(x2 - borderThickness, z1, x2, z2, borderColor);
            }
        }
    }

    public static void renderClaimName(GuiGraphics guiGraphics, RecruitsClaim claim, double offsetX, double offsetZ, double scale) {
        if (claim.getClaimedChunks().isEmpty() || scale < 1.0) return;

        boolean explored = !ClientManager.configFogOfWarEnabled || isAdminCreative() || isClaimExplored(claim);

        Font font = Minecraft.getInstance().font;
        String name = explored ? claim.getName() : "???";
        int nameColor = explored ? 0xFFFFFF : 0x888888;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (ChunkPos pos : claim.getClaimedChunks()) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minZ = Math.min(minZ, pos.z);
            maxZ = Math.max(maxZ, pos.z);
        }

        double centerWorldX = (minX + maxX + 1) * 16.0 / 2.0;
        double centerWorldZ = (minZ + maxZ + 1) * 16.0 / 2.0;

        double pixelX = offsetX + centerWorldX * scale;
        double pixelZ = offsetZ + centerWorldZ * scale;

        float textScale = (float)Math.min(1.0, scale / 1.25);

        int textWidth = font.width(name);
        int textHeight = font.lineHeight;
        int scaledTextWidth = Math.max(1, Math.round(textWidth * textScale));
        int scaledTextHeight = Math.max(1, Math.round(textHeight * textScale));
        int boxX = (int) Math.floor(pixelX - scaledTextWidth / 2.0) - 4;
        int boxY = (int) Math.floor(pixelZ - scaledTextHeight / 2.0) - 2;
        int boxWidth = scaledTextWidth + 8;
        int boxHeight = scaledTextHeight + 4;

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, LABEL_FILL);
        guiGraphics.renderOutline(boxX, boxY, boxWidth, boxHeight, explored ? LABEL_BORDER : 0xCC666666);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(pixelX - (textWidth * textScale) / 2.0, boxY + 2, 0);

        pose.scale(textScale, textScale, 1.0f);

        guiGraphics.drawString(font, name, 0, 0, nameColor, false);

        pose.popPose();
    }

    public static int getClaimColor(RecruitsClaim claim) {
        if (claim.getOwnerPoliticalEntityId() == null) return 0xFF888888;
        int hash = claim.getOwnerPoliticalEntityId().hashCode();
        return 0xFF000000 | (hash & 0x00FFFFFF);
    }

    public static RecruitsClaim getClaimAtPosition(double mouseX, double mouseY, double offsetX, double offsetZ, double scale) {
        double worldX = (mouseX - offsetX) / scale;
        double worldZ = (mouseY - offsetZ) / scale;

        int chunkX = (int)Math.floor(worldX / 16);
        int chunkZ = (int)Math.floor(worldZ / 16);
        ChunkPos mouseChunk = new ChunkPos(chunkX, chunkZ);

        return ClientManager.getClaimAtChunk(mouseChunk);
    }

    public static void renderBufferZone(GuiGraphics guiGraphics, double offsetX, double offsetZ, double scale) {
        Set<String> renderedBufferChunks = new HashSet<>();
        int bufferColor = 0x44FF4444;

        Set<String> ownClaimedChunks = new HashSet<>();

        for (RecruitsClaim foreignClaim : ClientManager.recruitsClaims) {
            String foreignOwnerKey = foreignClaim.getOwnerPoliticalEntityId() == null ? null : foreignClaim.getOwnerPoliticalEntityId().toString();
            if (foreignOwnerKey == null) {
                continue;
            }

            for (ChunkPos claimChunk : foreignClaim.getClaimedChunks()) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {

                        if (dx == 0 && dz == 0) continue;

                        int bufferX = claimChunk.x + dx;
                        int bufferZ = claimChunk.z + dz;
                        String chunkKey = bufferX + "," + bufferZ;

                        if (renderedBufferChunks.contains(chunkKey)) continue;

                        if (ownClaimedChunks.contains(chunkKey)) continue;

                        renderedBufferChunks.add(chunkKey);

                        ChunkPos bufferChunk = new ChunkPos(bufferX, bufferZ);
                        renderChunk(guiGraphics, bufferChunk, bufferColor, offsetX, offsetZ, scale);
                    }
                }
            }
        }
    }

    public static void renderAreaPreview(GuiGraphics guiGraphics, List<ChunkPos> areaChunks, double offsetX, double offsetZ, double scale) {
        if (areaChunks == null || areaChunks.isEmpty()) return;

        int previewColor = 0x33FFFFFF;

        for (ChunkPos chunk : areaChunks) {
            renderChunk(guiGraphics, chunk, previewColor, offsetX, offsetZ, scale);
        }
    }


}
