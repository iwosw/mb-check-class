package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;

public class ChunkTile {
    private static final int EMPTY_TILE_COLOR = 0xFF1A1A1A;
    private static final int EMPTY_TILE_GRID_COLOR = 0xFF252525;

    private final int tileX, tileZ;
    private NativeImage image;
    private DynamicTexture texture;
    private ResourceLocation textureId;
    private boolean needsUpdate = false;

    public static final int TILE_SIZE = 10;
    public static final int PIXELS_PER_CHUNK = 16;
    public static final int TILE_PIXEL_SIZE = TILE_SIZE * PIXELS_PER_CHUNK;

    public ChunkTile(int tileX, int tileZ) {
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public void loadOrCreate(File tileFile) {
        Minecraft mc = Minecraft.getInstance();

        try {
            if (tileFile.exists() && tileFile.length() > 0) {
                byte[] fileData = java.nio.file.Files.readAllBytes(tileFile.toPath());
                this.image = NativeImage.read(fileData);
                if (this.image.getWidth() != TILE_PIXEL_SIZE ||
                        this.image.getHeight() != TILE_PIXEL_SIZE) {
                    this.image.close();
                    this.image = null;
                }
            }
        } catch (IOException ignored) {
            this.image = null;
        }

        if (this.image == null) {
            this.image = new NativeImage(NativeImage.Format.RGBA, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE, false);
            fillEmptyTile();
            this.needsUpdate = true;
        } else if (isFullyTransparent()) {
            fillEmptyTile();
            this.needsUpdate = true;
        }

        this.texture = new DynamicTexture(this.image);
        this.texture.upload();
        this.textureId = mc.getTextureManager().register("chunktile_" + tileX + "_" + tileZ, this.texture);
    }

    private void fillEmptyTile() {
        for (int z = 0; z < TILE_PIXEL_SIZE; z++) {
            for (int x = 0; x < TILE_PIXEL_SIZE; x++) {
                boolean gridLine = x % PIXELS_PER_CHUNK == 0 || z % PIXELS_PER_CHUNK == 0;
                this.image.setPixelRGBA(x, z, gridLine ? EMPTY_TILE_GRID_COLOR : EMPTY_TILE_COLOR);
            }
        }
    }

    private boolean isFullyTransparent() {
        for (int z = 0; z < TILE_PIXEL_SIZE; z++) {
            for (int x = 0; x < TILE_PIXEL_SIZE; x++) {
                if ((this.image.getPixelRGBA(x, z) >>> 24) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void updateFromChunkImage(ChunkImage chunkImage, int chunkXInTile, int chunkZInTile) {
        if (this.image == null || chunkImage == null || !chunkImage.isMeaningful()) return;

        NativeImage chunkImg = chunkImage.getNativeImage();
        int startX = chunkXInTile * PIXELS_PER_CHUNK;
        int startZ = chunkZInTile * PIXELS_PER_CHUNK;
        boolean changed = false;

        for (int x = 0; x < PIXELS_PER_CHUNK; x++) {
            for (int z = 0; z < PIXELS_PER_CHUNK; z++) {
                int tileX = startX + x;
                int tileZ = startZ + z;
                int pixel = chunkImg.getPixelRGBA(x, z);
                if (this.image.getPixelRGBA(tileX, tileZ) != pixel) {
                    this.image.setPixelRGBA(tileX, tileZ, pixel);
                    changed = true;
                }
            }
        }

        if (!changed) return;
        this.texture.upload();
        this.needsUpdate = true;
    }

    public void saveToFile(File tileFile) {
        if (this.image == null || !this.needsUpdate) return;
        try {
            tileFile.getParentFile().mkdirs();
            this.image.writeToFile(tileFile);
            this.needsUpdate = false;
        } catch (IOException ignored) {}
    }

    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int size) {
        if (this.textureId != null) {
            guiGraphics.blit(this.textureId, x, y, 0, 0, size, size, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE);
        }
    }

    public void close() {
        try { if (this.image != null) this.image.close(); } catch (Exception ignored) {}
        if (this.textureId != null) Minecraft.getInstance().getTextureManager().release(this.textureId);
        try { if (this.texture != null) this.texture.close(); } catch (Exception ignored) {}
    }

    public int getTileX() { return tileX; }
    public int getTileZ() { return tileZ; }
    public NativeImage getImage() { return image; }
    public ResourceLocation getTextureId() { return textureId; }
    public void markAccessed() { }
    public void markNeedsUpdate() { this.needsUpdate = true; }
    public boolean needsSave() { return this.needsUpdate; }

    public static int chunkToTileCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, TILE_SIZE);
    }

    public static int tileToChunkCoord(int tileCoord) {
        return tileCoord * TILE_SIZE;
    }
}
