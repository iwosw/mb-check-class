package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;

public class ChunkImage {
    private static final int WATER_DEEP_RGB = 0x1E466E;
    private static final int WATER_SHALLOW_RGB = 0x2D5B82;
    private static final int GRASS_RGB = 0x4F8F45;
    private static final int LEAVES_RGB = 0x376E35;
    private static final int SAND_RGB = 0xD4C184;
    private static final int RED_SAND_RGB = 0xB46B3D;
    private static final int STONE_RGB = 0x85837C;
    private static final int DEEPSLATE_RGB = 0x55565C;
    private static final int SNOW_RGB = 0xE1E8EA;
    private static final int DIRT_RGB = 0x7C5B3B;
    private static final int PODZOL_RGB = 0x6E5130;
    private static final int CLAY_RGB = 0x9A8F88;
    private static final int WOOD_RGB = 0x7B5A35;
    private static final int PLANKS_RGB = 0x9C7650;
    private static final int PATH_RGB = 0x9A7A4E;
    private static final int BRICK_RGB = 0x8E4E3E;
    private static final int CROP_RGB = 0xA2A04B;

    private final NativeImage image;
    private int meaningfulPixelCount;
    private int samplePixel;

    public ChunkImage(ClientLevel level, ChunkPos pos) {
        this.image = generateVanillaStyleImage(level, pos);
    }

    private NativeImage generateVanillaStyleImage(ClientLevel level, ChunkPos pos) {
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, ChunkTile.PIXELS_PER_CHUNK, ChunkTile.PIXELS_PER_CHUNK, true);
        for (int blockX = 0; blockX < ChunkTile.BLOCKS_PER_CHUNK; blockX++) {
            for (int blockZ = 0; blockZ < ChunkTile.BLOCKS_PER_CHUNK; blockZ++) {
                int worldX = pos.getMinBlockX() + blockX;
                int worldZ = pos.getMinBlockZ() + blockZ;
                BlockPos topBlock = resolveTopBlock(level, worldX, worldZ);
                for (int subX = 0; subX < ChunkTile.PIXELS_PER_BLOCK; subX++) {
                    for (int subZ = 0; subZ < ChunkTile.PIXELS_PER_BLOCK; subZ++) {
                        int pixel = getTerrainColor(level, topBlock, subX, subZ);
                        img.setPixelRGBA(blockX * ChunkTile.PIXELS_PER_BLOCK + subX, blockZ * ChunkTile.PIXELS_PER_BLOCK + subZ, pixel);
                        if (((pixel >> 24) & 0xFF) > 0) {
                            meaningfulPixelCount++;
                            if (samplePixel == 0) samplePixel = pixel;
                        }
                    }
                }
            }
        }
        img.untrack();
        return img;
    }

    private BlockPos resolveTopBlock(ClientLevel level, int worldX, int worldZ) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int heightmapY = Math.max(minY, Math.min(maxY, level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1));
        BlockPos heightmapPos = new BlockPos(worldX, heightmapY, worldZ);
        if (isRenderable(level.getBlockState(heightmapPos))) return heightmapPos;

        BlockPos.MutableBlockPos scan = new BlockPos.MutableBlockPos(worldX, maxY, worldZ);
        while (scan.getY() >= minY) {
            if (isRenderable(level.getBlockState(scan))) return scan.immutable();
            scan.move(Direction.DOWN);
        }
        return heightmapPos;
    }

    private boolean isRenderable(BlockState state) {
        return !state.isAir() || !state.getFluidState().isEmpty();
    }

    private int getTerrainColor(ClientLevel level, BlockPos pos, int subX, int subZ) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return 0x00000000;

        boolean water = isWaterLike(state);
        int rgb = water ? getWaterRgb(level, pos) : getLandRgb(level, pos, state);
        if (!water) {
            rgb = applyRelief(level, pos, rgb);
        }
        rgb = applyTinyNoise(pos, rgb);
        rgb = applySubpixelShade(pos, subX, subZ, rgb);
        return toNativeAbgr(rgb);
    }

    private int getWaterRgb(ClientLevel level, BlockPos pos) {
        int biomeWater = level.getBiome(pos).value().getWaterColor();
        int depth = getWaterDepth(level, pos);
        float deepBlend = clamp(depth / 14.0f, 0.25f, 0.92f);
        int rgb = mixRgb(WATER_SHALLOW_RGB, WATER_DEEP_RGB, deepBlend);
        rgb = mixRgb(rgb, muteRgb(biomeWater, 0.82f), 0.18f);
        return adjustBrightness(rgb, 0.98f - Math.min(depth, 12) * 0.012f);
    }

    private int getLandRgb(ClientLevel level, BlockPos pos, BlockState state) {
        int rgb = getBaseLandRgb(level, pos, state);

        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)) {
            rgb = mixRgb(rgb, muteRgb(level.getBiome(pos).value().getGrassColor(pos.getX(), pos.getZ()), 0.88f), 0.38f);
        } else if (state.is(BlockTags.LEAVES)) {
            rgb = mixRgb(rgb, muteRgb(level.getBiome(pos).value().getFoliageColor(), 0.86f), 0.42f);
            rgb = adjustBrightness(rgb, 0.84f);
        }

        if (isSandLike(state) && isNextToWater(level, pos)) {
            rgb = adjustBrightness(rgb, 1.08f);
        }
        return boostSaturation(rgb, 1.02f);
    }

    private int getBaseLandRgb(ClientLevel level, BlockPos pos, BlockState state) {
        int mapRgb = getMapRgb(level, pos, state);

        if (state.is(BlockTags.LEAVES)) return mixRgb(mapRgb, LEAVES_RGB, 0.58f);
        if (state.is(BlockTags.PLANKS)) return mixRgb(mapRgb, PLANKS_RGB, 0.52f);
        if (state.is(BlockTags.LOGS)) return mixRgb(mapRgb, WOOD_RGB, 0.50f);
        if (state.is(Blocks.DIRT_PATH)) return mixRgb(mapRgb, PATH_RGB, 0.60f);
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.MOSS_BLOCK)) {
            return mixRgb(mapRgb, GRASS_RGB, 0.38f);
        }
        if (state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.CHISELED_SANDSTONE) || state.is(Blocks.CUT_SANDSTONE)) {
            return mixRgb(mapRgb, SAND_RGB, 0.62f);
        }
        if (state.is(Blocks.RED_SAND) || state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.SMOOTH_RED_SANDSTONE)
                || state.is(Blocks.CHISELED_RED_SANDSTONE) || state.is(Blocks.CUT_RED_SANDSTONE)) {
            return mixRgb(mapRgb, RED_SAND_RGB, 0.58f);
        }
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)) {
            return mixRgb(mapRgb, SNOW_RGB, 0.55f);
        }
        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) || state.is(Blocks.POLISHED_DEEPSLATE)
                || state.is(Blocks.DEEPSLATE_BRICKS) || state.is(Blocks.DEEPSLATE_TILES)) {
            return mixRgb(mapRgb, DEEPSLATE_RGB, 0.55f);
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE) || state.is(Blocks.DRIPSTONE_BLOCK)) {
            return mixRgb(mapRgb, STONE_RGB, 0.45f);
        }
        if (state.is(Blocks.STONE_BRICKS) || state.is(Blocks.MOSSY_STONE_BRICKS) || state.is(Blocks.CRACKED_STONE_BRICKS)
                || state.is(Blocks.BRICKS) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.RED_NETHER_BRICKS)) {
            return mixRgb(mapRgb, BRICK_RGB, 0.44f);
        }
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.MUD)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            return mixRgb(mapRgb, DIRT_RGB, 0.52f);
        }
        if (state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) return mixRgb(mapRgb, PODZOL_RGB, 0.48f);
        if (state.is(Blocks.CLAY) || state.is(Blocks.GRAVEL)) return mixRgb(mapRgb, CLAY_RGB, 0.45f);
        if (state.is(Blocks.WHEAT) || state.is(Blocks.CARROTS) || state.is(Blocks.POTATOES) || state.is(Blocks.BEETROOTS)
                || state.is(Blocks.MELON_STEM) || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.SUGAR_CANE)) {
            return mixRgb(mapRgb, CROP_RGB, 0.48f);
        }

        return mapRgb;
    }

    private int getMapRgb(ClientLevel level, BlockPos pos, BlockState state) {
        MapColor mapColor = state.getMapColor(level, pos);
        if (mapColor == null) return 0x101010;
        return mapColor.calculateRGBColor(MapColor.Brightness.NORMAL) & 0x00FFFFFF;
    }

    private int applyRelief(ClientLevel level, BlockPos pos, int rgb) {
        int x = pos.getX();
        int z = pos.getZ();
        int northwest = surfaceHeight(level, x - 1, z - 1);
        int southeast = surfaceHeight(level, x + 1, z + 1);
        int farNorthwest = surfaceHeight(level, x - 2, z - 2);
        int farSoutheast = surfaceHeight(level, x + 2, z + 2);
        int west = surfaceHeight(level, x - 1, z);
        int east = surfaceHeight(level, x + 1, z);
        int north = surfaceHeight(level, x, z - 1);
        int south = surfaceHeight(level, x, z + 1);

        float light = 1.0f + clamp((northwest - southeast) * 0.052f + (farNorthwest - farSoutheast) * 0.018f, -0.24f, 0.22f);
        float roughness = Math.min(0.075f, (Math.abs(west - east) + Math.abs(north - south)) * 0.014f);
        rgb = adjustBrightness(rgb, light);
        if (roughness > 0.0f) {
            rgb = adjustBrightness(rgb, 1.0f - roughness * 0.62f);
        }
        return rgb;
    }

    private int surfaceHeight(ClientLevel level, int worldX, int worldZ) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1;
    }

    private boolean isNextToWater(ClientLevel level, BlockPos pos) {
        return isWaterAt(level, pos.getX() - 1, pos.getZ())
                || isWaterAt(level, pos.getX() + 1, pos.getZ())
                || isWaterAt(level, pos.getX(), pos.getZ() - 1)
                || isWaterAt(level, pos.getX(), pos.getZ() + 1);
    }

    private boolean isWaterAt(ClientLevel level, int worldX, int worldZ) {
        BlockPos top = resolveTopBlock(level, worldX, worldZ);
        return isWaterLike(level.getBlockState(top));
    }

    private int toNativeAbgr(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    private int applyTinyNoise(BlockPos pos, int rgb) {
        int hash = pos.getX() * 73428767 ^ pos.getZ() * 91227153;
        hash ^= hash >>> 13;
        float factor = 0.985f + (hash & 0xF) * 0.002f;
        return adjustBrightness(rgb, factor);
    }

    private int applySubpixelShade(BlockPos pos, int subX, int subZ, int rgb) {
        int hash = pos.getX() * 1103515245 ^ pos.getZ() * 12345 ^ subX * 374761393 ^ subZ * 668265263;
        hash ^= hash >>> 16;
        float northwestLight = (subX == 0 ? 0.006f : -0.002f) + (subZ == 0 ? 0.006f : -0.002f);
        float noise = ((hash & 0x3) - 1.5f) * 0.002f;
        return adjustBrightness(rgb, 1.0f + northwestLight + noise);
    }

    private boolean isSandLike(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE) || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.SMOOTH_RED_SANDSTONE);
    }

    private int muteRgb(int rgb, float saturation) {
        return boostSaturation(rgb & 0x00FFFFFF, saturation);
    }

    private int mixRgb(int a, int b, float t) {
        t = clamp(t, 0.0f, 1.0f);
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int blue = Math.round(ab + (bb - ab) * t);
        return r << 16 | g << 8 | blue;
    }

    private int adjustBrightness(int rgb, float factor) {
        int r = clampColor(Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = clampColor(Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = clampColor(Math.round((rgb & 0xFF) * factor));
        return r << 16 | g << 8 | b;
    }

    private int boostSaturation(int rgb, float factor) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float gray = r * 0.299f + g * 0.587f + b * 0.114f;
        r = clampColor(Math.round(gray + (r - gray) * factor));
        g = clampColor(Math.round(gray + (g - gray) * factor));
        b = clampColor(Math.round(gray + (b - gray) * factor));
        return r << 16 | g << 8 | b;
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int getWaterDepth(ClientLevel level, BlockPos pos) {
        int depth = 0;
        BlockPos.MutableBlockPos mutable = pos.mutable();

        while (isWaterLike(level.getBlockState(mutable))
                && mutable.getY() > level.getMinBuildHeight()) {
            depth++;
            mutable.move(Direction.DOWN);
        }

        return depth;
    }

    private boolean isWaterLike(BlockState state) {
        return state.getFluidState().is(Fluids.WATER);
    }

    public NativeImage getNativeImage() {
        return this.image;
    }

    public boolean isMeaningful() {
        if (this.image == null) return false;
        return meaningfulPixelCount >= ChunkTile.PIXELS_PER_CHUNK * ChunkTile.PIXELS_PER_CHUNK / 10;
    }

    public int getMeaningfulPixelCount() {
        return meaningfulPixelCount;
    }

    public int getSamplePixel() {
        return samplePixel;
    }

    public void close() {
        try { if (image != null) image.close(); } catch (Exception ignored) {}
    }
}
