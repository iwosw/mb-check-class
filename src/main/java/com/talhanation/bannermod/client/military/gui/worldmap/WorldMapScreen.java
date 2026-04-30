package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.army.map.FormationMapRelation;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.compat.SmallShips;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class WorldMapScreen extends Screen {
    private static final ResourceLocation MAP_ICONS = ResourceLocation.parse("textures/map/map_icons.png");
    private final ChunkTileManager tileManager;
    private final WorldMapClaimController claimController;
    private final Player player;
    private static final double MIN_SCALE = 0.2;
    private static final double MAX_SCALE = 10.0;
    private static final double DEFAULT_SCALE = 2.0;
    private static final double SCALE_STEP = 0.1;
    private static final int CHUNK_HIGHLIGHT_COLOR = 0x40FFFFFF;
    private static final int CHUNK_SELECTION_COLOR = 0xFFFFFFFF;
    private static final int DARK_GRAY_BG = 0xFF101010;

    double offsetX = 0, offsetZ = 0;
    public static double scale = DEFAULT_SCALE;
    public double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    private long navigatingMapUntil = 0L;
    private ChunkPos hoveredChunk = null;
    ChunkPos selectedChunk = null;
    private int clickedBlockX = 0, clickedBlockZ = 0;
    private int hoverBlockX = 0, hoverBlockZ = 0;
    private WorldMapContextMenu contextMenu;
    RecruitsClaim selectedClaim = null;
    private ClaimInfoMenu claimInfoMenu;
    private UUID selectedFormationContactId = null;
    int snapshotWorldX = 0;
    int snapshotWorldZ = 0;
    private final WorldMapRouteInteractionLayer routeInteractionLayer;

    public WorldMapScreen() {
        super(Component.literal(""));
        this.tileManager = ChunkTileManager.getInstance();
        this.claimController = new WorldMapClaimController(this);
        this.player = Minecraft.getInstance().player;
        this.routeInteractionLayer = new WorldMapRouteInteractionLayer(this, player);
        this.contextMenu = new WorldMapContextMenu(this);
        this.claimInfoMenu = new ClaimInfoMenu(this);
    }

    public BlockPos getHoveredBlockPos() {
        return new BlockPos(hoverBlockX, resolveSurfaceY(hoverBlockX, hoverBlockZ), hoverBlockZ);
    }

    public BlockPos getClickedBlockPos() {
        return new BlockPos(clickedBlockX, resolveSurfaceY(clickedBlockX, clickedBlockZ), clickedBlockZ);
    }

    int resolveSurfaceY(int worldX, int worldZ) {
        net.minecraft.client.multiplayer.ClientLevel level = minecraft.level;
        if (level == null) return 64;
        ChunkPos chunk = new ChunkPos(worldX >> 4, worldZ >> 4);
        if (level.getChunkSource().getChunk(chunk.x, chunk.z, false) == null) return 64;
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1;
        return Math.max(y, level.getMinBuildHeight());
    }

    public Player getPlayer() {
        return player;
    }

    Minecraft getMinecraftInstance() {
        return minecraft;
    }

    net.minecraft.client.gui.Font getScreenFont() {
        return font;
    }

    public boolean isPlayerAdminAndCreative() {
        return player.hasPermissions(2) && player.isCreative();
    }

    public double getScale() {
        return scale;
    }

    public boolean isNavigatingMap() {
        return isDragging || System.currentTimeMillis() < navigatingMapUntil;
    }

    private void markMapNavigation() {
        navigatingMapUntil = System.currentTimeMillis() + 300L;
    }

    public void setSelectedChunk(ChunkPos chunk) {
        this.selectedChunk = chunk;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft.level != null && player != null) {
            tileManager.initialize(minecraft.level);
            centerOnPlayer();
        }
        claimInfoMenu.init();
        routeInteractionLayer.init();
    }

    public void refreshRouteUI() {
        routeInteractionLayer.refreshUI();
    }

    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void centerOnPlayer() {
        if (player != null) {
            int chunkX = player.chunkPosition().x;
            int chunkZ = player.chunkPosition().z;
            offsetX = -(chunkX * 16 * scale) + width / 2.0;
            offsetZ = -(chunkZ * 16 * scale) + height / 2.0;
        }
    }

    public void resetZoom() {
        scale = DEFAULT_SCALE;
        centerOnPlayer();
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.enableScissor(0, 0, width, height);

        renderMapTiles(guiGraphics);
        if (routeInteractionLayer.isClaimTransparencyEnabled() && routeInteractionLayer.hasSelectedRoute()) {
            ClaimRenderer.renderClaimsOverlayTransparent(guiGraphics, this.selectedClaim, this.offsetX, this.offsetZ, scale, width, height);
        } else {
            ClaimRenderer.renderClaimsOverlay(guiGraphics, this.selectedClaim, this.offsetX, this.offsetZ, scale, width, height);
        }

        if (contextMenu.isVisible()) {
            String entryTag = contextMenu.getHoveredEntryTag();
            if (entryTag != null) {
                if (entryTag.contains("bufferzone"))
                    ClaimRenderer.renderBufferZone(guiGraphics, offsetX, offsetZ, scale);
                if (entryTag.contains("area"))
                    ClaimRenderer.renderAreaPreview(guiGraphics, getClaimArea(selectedChunk), offsetX, offsetZ, scale);
                if (entryTag.contains("chunk"))
                    ClaimRenderer.renderAreaPreview(guiGraphics, getClaimableChunks(selectedChunk, 16), offsetX, offsetZ, scale);
            }
        }

        if (player != null) renderPlayerPosition(guiGraphics);

        WorldMapMoveOrderMarker.render(guiGraphics, offsetX, offsetZ, scale);

        FormationMapOverlayRenderer.render(guiGraphics, offsetX, offsetZ, scale, selectedFormationContactId);

        if (selectedChunk != null && (selectedClaim == null || contextMenu.isVisible())) {
            renderChunkOutline(guiGraphics, selectedChunk.x, selectedChunk.z, CHUNK_SELECTION_COLOR);
        }

        if (hoveredChunk != null) renderChunkHighlight(guiGraphics, hoveredChunk.x, hoveredChunk.z);

        routeInteractionLayer.renderRouteOverlay(guiGraphics, mouseX, mouseY);

        guiGraphics.disableScissor();

        renderCoordinatesAndZoom(guiGraphics);
        renderClaimSyncStatus(guiGraphics);
        renderFPS(guiGraphics);

        // Buttons (+ and ⚙)
        routeInteractionLayer.renderUi(guiGraphics, mouseX, mouseY, partialTicks);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        contextMenu.render(guiGraphics, this);

        if (selectedClaim != null && claimInfoMenu.isVisible()) {
            Point p = getClaimInfoMenuPosition(selectedClaim, claimInfoMenu.width, claimInfoMenu.height);
            claimInfoMenu.setPosition(p.x, p.y);
            claimInfoMenu.render(guiGraphics);
        }

        routeInteractionLayer.renderPopups(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, DARK_GRAY_BG);
    }

    private void renderClaimSyncStatus(GuiGraphics guiGraphics) {
        Component status;
        int color;
        if (!ClientManager.hasClaimsSnapshot) {
            status = Component.translatable("gui.bannermod.map.claim_state.waiting_sync");
            color = 0xFFAAAAAA;
        } else if (ClientManager.claimsSnapshotStale) {
            status = Component.translatable("gui.bannermod.map.claim_state.stale");
            color = 0xFFFFD36A;
        } else if (ClientManager.recruitsClaims.isEmpty()) {
            status = Component.translatable("gui.bannermod.map.claim_state.empty");
            color = 0xFF8FA8FF;
        } else {
            return;
        }
        int panelWidth = Math.min(220, this.width - 20);
        int panelX = 10;
        int panelY = 34;
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 18, 0xAA101010);
        guiGraphics.renderOutline(panelX, panelY, panelWidth, 18, color);
        guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(status.getString(), panelWidth - 10), panelX + 5, panelY + 5, color, false);
    }

    private void renderMapTiles(GuiGraphics guiGraphics) {
        double tileSize = ChunkTile.TILE_PIXEL_SIZE;
        double scaledTileSize = tileSize * scale;

        double leftEdge = -offsetX;
        double rightEdge = width - offsetX;
        double topEdge = -offsetZ;
        double bottomEdge = height - offsetZ;

        int startTileX = (int) Math.floor(leftEdge / scaledTileSize - 0.5);
        int endTileX = (int) Math.ceil(rightEdge / scaledTileSize + 0.5);
        int startTileZ = (int) Math.floor(topEdge / scaledTileSize - 0.5);
        int endTileZ = (int) Math.ceil(bottomEdge / scaledTileSize + 0.5);

        for (int tileZ = startTileZ; tileZ <= endTileZ; tileZ++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                ChunkTile tile = tileManager.getOrCreateTile(tileX, tileZ);
                ResourceLocation textureId = tile.getTextureId();
                if (textureId == null) continue;

                double tileWorldX = tileX * scaledTileSize + offsetX;
                double tileWorldZ = tileZ * scaledTileSize + offsetZ;
                double drawX = tileWorldX - 0.5;
                double drawZ = tileWorldZ - 0.5;
                double drawSize = scaledTileSize + 1.0;

                int x = (int) Math.floor(drawX);
                int z = (int) Math.floor(drawZ);
                int size = (int) Math.ceil(drawSize);

                if (Math.abs(scale - 1.0) < 0.01) {
                    x = (int) Math.round(tileWorldX);
                    z = (int) Math.round(tileWorldZ);
                    size = (int) Math.round(scaledTileSize);
                }

                RenderSystem.setShaderTexture(0, textureId);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                guiGraphics.blit(textureId, x, z, 0, 0, size, size,
                        ChunkTile.TILE_PIXEL_SIZE, ChunkTile.TILE_PIXEL_SIZE);
            }
        }
    }

    public void addWaypointAtClicked() {
        routeInteractionLayer.addWaypointAtClicked();
    }

    public boolean canPlaceWaypointAt(int worldX, int worldZ) {
        if (minecraft.level == null) return false;
        ChunkPos chunk = new ChunkPos(worldX >> 4, worldZ >> 4);
        if (!tileManager.isChunkExplored(chunk)) return false;
        return minecraft.level.getChunkSource().getChunk(chunk.x, chunk.z, false) != null;
    }

    public void openWaypointEditPopup(double mouseX, double mouseY) {
        routeInteractionLayer.openWaypointEditPopup(mouseX, mouseY);
    }

    public void removeWaypointAt(double mouseX, double mouseY) {
        routeInteractionLayer.removeWaypointAt(mouseX, mouseY);
    }

    public boolean isWaypointHoveredAt(double mouseX, double mouseY) {
        return routeInteractionLayer.isWaypointHoveredAt(mouseX, mouseY);
    }

    // -------------------------------------------------------------------------
    // Player rendering
    // -------------------------------------------------------------------------

    private static final ItemStack BOAT_STACK = new ItemStack(Items.OAK_BOAT);

    private void renderPlayerPosition(GuiGraphics guiGraphics) {
        double playerWorldX = player.getX();
        double playerWorldZ = player.getZ();
        int pixelX = (int) (offsetX + playerWorldX * scale);
        int pixelZ = (int) (offsetZ + playerWorldZ * scale);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(pixelX, pixelZ, 0);
        if (player.getVehicle() instanceof Boat) renderPlayerBoat(pose, guiGraphics);
        else renderPlayerIcon(pose, guiGraphics);
        pose.popPose();
        renderPlayerNameTag(guiGraphics, pixelX, pixelZ);
    }

    private void renderPlayerBoat(PoseStack pose, GuiGraphics guiGraphics) {
        float yaw = player.getYRot() % 360f;
        if (yaw < -180f) yaw += 360f;
        if (yaw >= 180f) yaw -= 360f;
        boolean flipX = yaw > 0;
        pose.pushPose();
        if (flipX) pose.scale(-1f, 1f, 1f);
        pose.scale(1.5f, 1.5f, 1.5f);
        Lighting.setupForFlatItems();
        ItemStack boat = BOAT_STACK;
        if (BannerModMain.isSmallShipsLoaded && player.getVehicle() != null && SmallShips.isSmallShip(player.getVehicle())) {
            boat = SmallShips.getSmallShipsItem();
        }
        RenderSystem.disableCull();
        guiGraphics.renderItem(boat, -8, -8);
        RenderSystem.enableCull();
        pose.popPose();
    }

    private void renderPlayerIcon(PoseStack pose, GuiGraphics guiGraphics) {
        pose.mulPose(Axis.ZP.rotationDegrees(player.getYRot()));
        pose.scale(5.0f, 5.0f, 5.0f);
        int iconIndex = 0;
        float u0 = (iconIndex % 16) / 16f;
        float v0 = (iconIndex / 16) / 16f;
        float u1 = u0 + 1f / 16f;
        float v1 = v0 + 1f / 16f;
        guiGraphics.flush();
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.text(MAP_ICONS));
        Matrix4f matrix = pose.last().pose();
        int light = 0xF000F0;
        int color = 0xFFFFFFFF;
        consumer.addVertex(matrix, -1f, 1f, 0f).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, 1f, 1f, 0f).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, 1f, -1f, 0f).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(matrix, -1f, -1f, 0f).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
    }

    private void renderPlayerNameTag(GuiGraphics guiGraphics, int pixelX, int pixelZ) {
        if (player != null && scale > 1.5) {
            String playerName = player.getName().getString();
            float textScale = (float) Math.min(1.0, scale / 1.25);
            int textWidth = font.width(playerName);
            int textHeight = font.lineHeight;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(pixelX - (textWidth * textScale) / 2.0, pixelZ - (textHeight * textScale) / 2.0 - 10, 0);
            guiGraphics.pose().scale(textScale, textScale, 1.0f);
            guiGraphics.drawString(font, playerName, 0, 0, 0xFFFFFF, false);
            guiGraphics.pose().popPose();
        }
    }

    private void renderChunkHighlight(GuiGraphics guiGraphics, int chunkX, int chunkZ) {
        int pixelX = WorldMapRenderPrimitives.screenX(chunkX * 16, offsetX, scale);
        int pixelZ = WorldMapRenderPrimitives.screenZ(chunkZ * 16, offsetZ, scale);
        int size = Math.max(1, (int) Math.round(16 * scale));
        guiGraphics.fill(pixelX, pixelZ, pixelX + size, pixelZ + size, CHUNK_HIGHLIGHT_COLOR);
    }

    private void renderChunkOutline(GuiGraphics guiGraphics, int chunkX, int chunkZ, int color) {
        int pixelX = WorldMapRenderPrimitives.screenX(chunkX * 16, offsetX, scale);
        int pixelZ = WorldMapRenderPrimitives.screenZ(chunkZ * 16, offsetZ, scale);
        int size = Math.max(1, (int) Math.round(16 * scale));
        guiGraphics.hLine(pixelX, pixelX + size, pixelZ, color);
        guiGraphics.hLine(pixelX, pixelX + size, pixelZ + size, color);
        guiGraphics.vLine(pixelX, pixelZ, pixelZ + size, color);
        guiGraphics.vLine(pixelX + size, pixelZ, pixelZ + size, color);
    }

    private void renderCoordinatesAndZoom(GuiGraphics guiGraphics) {
        int hoverY = resolveSurfaceY(hoverBlockX, hoverBlockZ);
        String coords = String.format("X: %d, Y: %d, Z: %d", hoverBlockX, hoverY, hoverBlockZ);
        String zoom = String.format("Zoom: %.1fx", scale);
        String combined = coords + " | " + zoom;
        int textWidth = font.width(combined);
        int bgX = width / 2 - textWidth / 2 - 8;
        int bgY = height - 30;
        int bgWidth = textWidth + 16;
        WorldMapRenderPrimitives.panel(guiGraphics, bgX, bgY, bgWidth, 20);
        guiGraphics.drawCenteredString(font, combined, width / 2, height - 25, 0xFFFFFF);
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (routeInteractionLayer.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (claimInfoMenu.isVisible() && claimInfoMenu.mouseClicked(mouseX, mouseY, button)) return true;

        if (contextMenu.isVisible()) {
            if (contextMenu.mouseClicked(mouseX, mouseY, button, this)) return true;
        }

        if (hoveredChunk != null) selectedChunk = hoveredChunk;

        FormationMapContact clickedContact = FormationMapOverlayRenderer.contactAt(mouseX, mouseY, offsetX, offsetZ, scale);
        if (button == 0 && clickedContact != null) {
            if (clickedContact.relation() == FormationMapRelation.SUBORDINATE) {
                selectedFormationContactId = clickedContact.contactId();
            }
            return true;
        }

        RecruitsClaim clickedClaim = ClaimRenderer.getClaimAtPosition(mouseX, mouseY, offsetX, offsetZ, scale);
        if (clickedClaim != null) {
            boolean canInspect = !ClientManager.configFogOfWarEnabled
                    || isPlayerAdminAndCreative()
                    || ClaimRenderer.isClaimExplored(clickedClaim);
            if (canInspect) {
                selectedClaim = clickedClaim;
                claimInfoMenu.openForClaim(selectedClaim, (int) mouseX, (int) mouseY);
            } else {
                selectedClaim = null;
                claimInfoMenu.close();
            }
        } else {
            selectedClaim = null;
            claimInfoMenu.close();
        }

        if (button == 1) {
            double worldX = (mouseX - offsetX) / scale;
            double worldZ = (mouseY - offsetZ) / scale;
            clickedBlockX = (int) Math.floor(worldX);
            clickedBlockZ = (int) Math.floor(worldZ);
            FormationMapContact selectedContact = getSelectedFormationContact();
            this.contextMenu = new WorldMapContextMenu(this, selectedContact, clickedContact);
            contextMenu.openAt((int) mouseX, (int) mouseY);
            snapshotWorldX = clickedBlockX;
            snapshotWorldZ = clickedBlockZ;
            claimInfoMenu.close();
        }

        if (button == 0) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            isDragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private FormationMapContact getSelectedFormationContact() {
        if (selectedFormationContactId == null) return null;
        for (FormationMapContact contact : ClientManager.formationMapContacts) {
            if (selectedFormationContactId.equals(contact.contactId())) {
                return contact;
            }
        }
        selectedFormationContactId = null;
        return null;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (contextMenu.isVisible()) return false;
        if (button == 0) {
            if (routeInteractionLayer.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
            isDragging = false;
        }
        if (claimInfoMenu.isVisible()) claimInfoMenu.mouseReleased(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (routeInteractionLayer.mouseDragged(mouseX, mouseY)) {
            return true;
        }
        if (isDragging) {
            offsetX += mouseX - lastMouseX;
            offsetZ += mouseY - lastMouseY;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            markMapNavigation();
            if (claimInfoMenu.isVisible()) claimInfoMenu.close();
            return true;
        }
        if (claimInfoMenu.isVisible()) claimInfoMenu.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        if (routeInteractionLayer.isAnyPopupVisible()) return true;
        if (claimInfoMenu.isVisible()) claimInfoMenu.close();
        if (contextMenu.isVisible()) contextMenu.close();

        double zoomFactor = 1.0 + (delta > 0 ? SCALE_STEP : -SCALE_STEP);
        double newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * zoomFactor));

        double mouseWorldX = (mouseX - offsetX) / scale;
        double mouseWorldZ = (mouseY - offsetZ) / scale;
        scale = newScale;
        offsetX = mouseX - mouseWorldX * scale;
        offsetZ = mouseY - mouseWorldZ * scale;
        markMapNavigation();
        return true;
    }

    public double mouseX, mouseY;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        routeInteractionLayer.mouseMoved(mouseX, mouseY);

        if (routeInteractionLayer.isUiHovered(mouseX, mouseY)) {
            hoveredChunk = null;
            return;
        }

        hoverBlockX = (int) Math.floor((mouseX - offsetX) / scale);
        hoverBlockZ = (int) Math.floor((mouseY - offsetZ) / scale);
        hoveredChunk = new ChunkPos(hoverBlockX >> 4, hoverBlockZ >> 4);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (routeInteractionLayer.keyPressed(keyCode)) return true;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (claimInfoMenu.isVisible()) {
                claimInfoMenu.close();
                return true;
            }
            if (contextMenu.isVisible()) {
                contextMenu.close();
                return true;
            }
            onClose();
            return true;
        }

        if (!contextMenu.isVisible() && !claimInfoMenu.isVisible()) {
            double moveSpeed = 40.0 / scale;
            switch (keyCode) {
                case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> offsetZ += moveSpeed;
                case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> offsetZ -= moveSpeed;
                case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> offsetX += moveSpeed;
                case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> offsetX -= moveSpeed;
                case GLFW.GLFW_KEY_EQUAL -> mouseScrolled(width / 2.0, height / 2.0, 0, 1);
                case GLFW.GLFW_KEY_MINUS -> mouseScrolled(width / 2.0, height / 2.0, 0, -1);
                case GLFW.GLFW_KEY_C -> centerOnPlayer();
                case GLFW.GLFW_KEY_R -> resetZoom();
            }
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_W
                    || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_S
                    || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A
                    || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D
                    || keyCode == GLFW.GLFW_KEY_C || keyCode == GLFW.GLFW_KEY_R) {
                markMapNavigation();
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (routeInteractionLayer.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        routeInteractionLayer.tick();
    }

    public void onClose() {
        tileManager.close();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean canAddRoute() {
        return routeInteractionLayer.hasSelectedRoute();
    }

    public void addRoute() {
        routeInteractionLayer.openAddRoutePopup();
    }

    @Nullable
    public RecruitsRoute getSelectedRoute() {
        return routeInteractionLayer.getSelectedRoute();
    }

    public void setSelectedRoute(@Nullable RecruitsRoute route) {
        routeInteractionLayer.setSelectedRoute(route);
    }

    void clearHoveredAndSelectedChunk() {
        hoveredChunk = null;
        selectedChunk = null;
    }

    void closeContextMenu() {
        contextMenu.close();
    }

    // -------------------------------------------------------------------------
    // FPS
    // -------------------------------------------------------------------------

    private long lastFpsTime = 0;
    private int fpsCounter = 0;
    private int currentFps = 0;

    private void renderFPS(GuiGraphics guiGraphics){
        long currentTime = System.currentTimeMillis();
        fpsCounter++;
        if (currentTime - lastFpsTime >= 1000) {
            currentFps = fpsCounter;
            fpsCounter = 0;
            lastFpsTime = currentTime;
        }
        String fpsText = String.format("FPS: %d", currentFps);
        guiGraphics.drawString(font, fpsText, width - font.width(fpsText) - 15, 5, 0x00FF00);
    }

        // -------------------------------------------------------------------------
        // Faction / claim helpers (unchanged)
        // -------------------------------------------------------------------------

        public boolean isPlayerClaimLeader() {
            return this.isPlayerClaimLeader(selectedClaim);
        }

        public boolean isPlayerClaimLeader(RecruitsClaim claim){
            if (player == null || claim == null) return false;
            return claim.getPlayerInfo().getUUID().equals(player.getUUID());
        }

        public List<ChunkPos> getClaimArea(ChunkPos pos){
            return claimController.getClaimArea(pos);
        }

        public void claimArea() {
            claimController.claimArea();
        }

        public void claimChunk() {
            claimController.claimChunk();
        }

        @Nullable
        public RecruitsClaim getNeighborClaim(ChunkPos chunk){
            return claimController.getNeighborClaim(chunk);
        }

        public void recalculateCenter(RecruitsClaim claim){
            claimController.recalculateCenter(claim);
        }

        public void centerOnClaim(RecruitsClaim claim){
            if (claim == null || claim.getCenter() == null) return;
            ChunkPos center = claim.getCenter();
            offsetX = -(center.x * 16 * scale) + width / 2.0;
            offsetZ = -(center.z * 16 * scale) + height / 2.0;
        }

        public Rectangle getClaimScreenBounds(RecruitsClaim claim){
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (ChunkPos pos : claim.getClaimedChunks()) {
                minX = Math.min(minX, pos.x);
                maxX = Math.max(maxX, pos.x);
                minZ = Math.min(minZ, pos.z);
                maxZ = Math.max(maxZ, pos.z);
            }
            int x1 = (int) (offsetX + minX * 16 * scale);
            int y1 = (int) (offsetZ + minZ * 16 * scale);
            int x2 = (int) (offsetX + (maxX + 1) * 16 * scale);
            int y2 = (int) (offsetZ + (maxZ + 1) * 16 * scale);
            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }

        public Point getClaimInfoMenuPosition(RecruitsClaim claim,int menuWidth, int menuHeight){
            Rectangle bounds = getClaimScreenBounds(claim);
            int margin = 10;
            int x = bounds.x + bounds.width + margin;
            int y = bounds.y + bounds.height / 2 - menuHeight / 2;
            if (x + menuWidth > width) x = bounds.x - menuWidth - margin;
            if (y < 10) y = 10;
            if (y + menuHeight > height - 10) y = height - menuHeight - 10;
            return new Point(x, y);
        }

        public boolean canRemoveChunk(ChunkPos pos, RecruitsClaim claim){
            return claimController.canRemoveChunk(pos, claim);
        }

        public int getClaimCost(){
            return claimController.getClaimCost();
        }

        public boolean canPlayerPay( int cost, Player player){
            return claimController.canPlayerPay(cost, player);
        }

        public static boolean isInBufferZone(ChunkPos chunk){
            return WorldMapClaimController.isInBufferZone(chunk);
        }

        public boolean canClaimChunk(ChunkPos pos){
            return claimController.canClaimChunk(pos);
        }

        public boolean canClaimArea(List < ChunkPos > areaChunks) {
            return claimController.canClaimArea(areaChunks);
        }

        public List<ChunkPos> getClaimableChunks(ChunkPos center,int radius){
            return claimController.getClaimableChunks(center, radius);
        }

        public boolean canClaimChunkRaw(ChunkPos pos){
            return claimController.canClaimChunkRaw(pos);
        }
    }
