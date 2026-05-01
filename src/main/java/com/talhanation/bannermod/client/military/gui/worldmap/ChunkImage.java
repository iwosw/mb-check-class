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
    private static final int WATER_DEEP_RGB = 0x163A66;
    private static final int WATER_SHALLOW_RGB = 0x2F74A8;
    private static final int GRASS_RGB = 0x6FCF4B;
    private static final int LEAVES_RGB = 0x269A34;
    private static final int SAND_RGB = 0xE0CD88;
    private static final int RED_SAND_RGB = 0xC06B34;
    private static final int STONE_RGB = 0x92908A;
    private static final int DEEPSLATE_RGB = 0x4B4C55;
    private static final int SNOW_RGB = 0xEDF6F7;
    private static final int DIRT_RGB = 0x835A35;
    private static final int PODZOL_RGB = 0x76512B;
    private static final int CLAY_RGB = 0xA59A94;
    private static final int WOOD_RGB = 0x855E32;
    private static final int PLANKS_RGB = 0xB17C45;
    private static final int PATH_RGB = 0xB08A4A;
    private static final int BRICK_RGB = 0x9C4E3E;
    private static final int CROP_RGB = 0xB9AD3E;
    private static final int EDGE_INK_RGB = 0x2B2118;
    private static final int COAST_INK_RGB = 0x102D48;

    private final NativeImage image;
    private int meaningfulPixelCount;
    private int samplePixel;

    private record BlockSample(BlockPos pos, BlockState state, boolean water, int terrainClass, int baseRgb) {
        boolean visible() {
            return baseRgb != 0;
        }
    }

    public ChunkImage(ClientLevel level, ChunkPos pos) {
        this.image = generateVanillaStyleImage(level, pos);
    }

    private NativeImage generateVanillaStyleImage(ClientLevel level, ChunkPos pos) {
        NativeImage img = new NativeImage(NativeImage.Format.RGBA, ChunkTile.PIXELS_PER_CHUNK, ChunkTile.PIXELS_PER_CHUNK, true);
        BlockSample[][] samples = new BlockSample[ChunkTile.BLOCKS_PER_CHUNK][ChunkTile.BLOCKS_PER_CHUNK];

        for (int blockX = 0; blockX < ChunkTile.BLOCKS_PER_CHUNK; blockX++) {
            for (int blockZ = 0; blockZ < ChunkTile.BLOCKS_PER_CHUNK; blockZ++) {
                int worldX = pos.getMinBlockX() + blockX;
                int worldZ = pos.getMinBlockZ() + blockZ;
                samples[blockX][blockZ] = sampleBlock(level, worldX, worldZ);
            }
        }

        for (int blockX = 0; blockX < ChunkTile.BLOCKS_PER_CHUNK; blockX++) {
            for (int blockZ = 0; blockZ < ChunkTile.BLOCKS_PER_CHUNK; blockZ++) {
                BlockSample sample = samples[blockX][blockZ];
                if (!sample.visible()) {
                    continue;
                }
                int leftEdgeRgb = applyNeighborEdge(samples, blockX, blockZ, level, sample, -1, 0, sample.baseRgb());
                int northEdgeRgb = applyNeighborEdge(samples, blockX, blockZ, level, sample, 0, -1, sample.baseRgb());
                int cornerEdgeRgb = applyNeighborEdge(samples, blockX, blockZ, level, sample, 0, -1, leftEdgeRgb);
                for (int subX = 0; subX < ChunkTile.PIXELS_PER_BLOCK; subX++) {
                    for (int subZ = 0; subZ < ChunkTile.PIXELS_PER_BLOCK; subZ++) {
                        int rgb = sample.baseRgb();
                        if (subX == 0 && subZ == 0) {
                            rgb = cornerEdgeRgb;
                        } else if (subX == 0) {
                            rgb = leftEdgeRgb;
                        } else if (subZ == 0) {
                            rgb = northEdgeRgb;
                        }
                        rgb = applyContourLine(sample.pos(), sample.water(), subX, subZ, rgb);
                        rgb = applySubpixelShade(sample.pos(), subX, subZ, rgb);
                        int pixel = toNativeAbgr(rgb);
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

    private BlockSample sampleBlock(ClientLevel level, int worldX, int worldZ) {
        BlockPos topBlock = resolveTopBlock(level, worldX, worldZ);
        BlockState state = level.getBlockState(topBlock);
        if (state.isAir()) return new BlockSample(topBlock, state, false, 0, 0);

        boolean water = isWaterLike(state);
        int rgb = water ? getWaterRgb(level, topBlock) : getLandRgb(level, topBlock, state);
        if (!water) {
            rgb = applyRelief(level, topBlock, rgb);
            if (state.is(BlockTags.LEAVES)) {
                rgb = applyCanopyShade(level, topBlock, rgb);
            }
        }
        rgb = applyTinyNoise(topBlock, rgb);
        return new BlockSample(topBlock, state, water, terrainClass(state), rgb);
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
            rgb = mixRgb(rgb, livelyRgb(level.getBiome(pos).value().getGrassColor(pos.getX(), pos.getZ()), 1.22f, 1.12f), 0.48f);
            rgb = adjustBrightness(rgb, 1.08f);
        } else if (state.is(BlockTags.LEAVES)) {
            rgb = mixRgb(rgb, livelyRgb(level.getBiome(pos).value().getFoliageColor(), 1.18f, 1.02f), 0.46f);
            rgb = adjustBrightness(rgb, 0.92f);
        }

        if (isSandLike(state) && isNextToWater(level, pos)) {
            rgb = adjustBrightness(rgb, 1.08f);
        }
        if (isBuiltSurface(state)) {
            rgb = mixRgb(rgb, EDGE_INK_RGB, 0.08f);
            rgb = boostSaturation(rgb, 1.18f);
        }
        return boostSaturation(rgb, 1.12f);
    }

    private int getBaseLandRgb(ClientLevel level, BlockPos pos, BlockState state) {
        int mapRgb = getMapRgb(level, pos, state);

        if (state.is(BlockTags.LEAVES)) return mixRgb(mapRgb, LEAVES_RGB, 0.58f);
        if (state.is(BlockTags.PLANKS)) return mixRgb(mapRgb, PLANKS_RGB, 0.52f);
        if (state.is(BlockTags.LOGS)) return mixRgb(mapRgb, WOOD_RGB, 0.50f);
        if (state.is(Blocks.DIRT_PATH)) return mixRgb(mapRgb, PATH_RGB, 0.60f);
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.MOSS_BLOCK)) {
            return mixRgb(mapRgb, GRASS_RGB, 0.70f);
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

        float light = 1.0f + clamp((northwest - southeast) * 0.068f + (farNorthwest - farSoutheast) * 0.024f, -0.30f, 0.28f);
        float roughness = Math.min(0.105f, (Math.abs(west - east) + Math.abs(north - south)) * 0.018f);
        rgb = adjustBrightness(rgb, light);
        if (roughness > 0.0f) {
            rgb = adjustBrightness(rgb, 1.0f - roughness * 0.70f);
        }
        return rgb;
    }

    private int applyNeighborEdge(BlockSample[][] samples, int blockX, int blockZ, ClientLevel level, BlockSample sample, int dx, int dz, int rgb) {
        BlockSample neighbor = neighborSample(samples, blockX, blockZ, level, sample.pos(), dx, dz);

        if (sample.water() != neighbor.water()) {
            return mixRgb(adjustBrightness(rgb, sample.water() ? 0.78f : 0.88f), sample.water() ? COAST_INK_RGB : EDGE_INK_RGB, 0.28f);
        }

        int heightDelta = Math.abs(sample.pos().getY() - neighbor.pos().getY());
        if (heightDelta >= 4) {
            return mixRgb(adjustBrightness(rgb, 0.82f), EDGE_INK_RGB, 0.18f);
        }
        if (!sample.water() && sample.terrainClass() != neighbor.terrainClass()) {
            int terrain = sample.terrainClass();
            float ink = terrain == 2 || terrain == 4 ? 0.045f : 0.10f;
            float shade = terrain == 2 || terrain == 4 ? 0.96f : 0.90f;
            return mixRgb(adjustBrightness(rgb, shade), EDGE_INK_RGB, ink);
        }
        return rgb;
    }

    private BlockSample neighborSample(BlockSample[][] samples, int blockX, int blockZ, ClientLevel level, BlockPos pos, int dx, int dz) {
        int sampleX = blockX + dx;
        int sampleZ = blockZ + dz;
        if (sampleX >= 0 && sampleX < ChunkTile.BLOCKS_PER_CHUNK && sampleZ >= 0 && sampleZ < ChunkTile.BLOCKS_PER_CHUNK) {
            return samples[sampleX][sampleZ];
        }
        return sampleBlock(level, pos.getX() + dx, pos.getZ() + dz);
    }

    private int applyCanopyShade(ClientLevel level, BlockPos pos, int rgb) {
        int northwest = surfaceHeight(level, pos.getX() - 1, pos.getZ() - 1);
        int southeast = surfaceHeight(level, pos.getX() + 1, pos.getZ() + 1);
        float light = 1.0f + clamp((northwest - southeast) * 0.10f, -0.34f, 0.30f);

        int openSides = 0;
        if (!level.getBlockState(pos.north()).is(BlockTags.LEAVES)) openSides++;
        if (!level.getBlockState(pos.south()).is(BlockTags.LEAVES)) openSides++;
        if (!level.getBlockState(pos.east()).is(BlockTags.LEAVES)) openSides++;
        if (!level.getBlockState(pos.west()).is(BlockTags.LEAVES)) openSides++;
        if (openSides >= 2) {
            light -= 0.10f;
        }

        int hash = pos.getX() * 1664525 ^ pos.getZ() * 1013904223;
        light += ((hash >>> 6) & 0x7) * 0.018f - 0.054f;
        rgb = adjustBrightness(rgb, clamp(light, 0.72f, 1.28f));
        return boostSaturation(rgb, 1.16f);
    }

    private int applyContourLine(BlockPos pos, boolean water, int subX, int subZ, int rgb) {
        if (water || subZ != 0) return rgb;
        int y = pos.getY();
        if (y > 0 && y % 16 == 0) {
            return mixRgb(adjustBrightness(rgb, 0.88f), EDGE_INK_RGB, 0.08f);
        }
        if (y > 0 && subX == 0 && y % 8 == 0) {
            return adjustBrightness(rgb, 0.94f);
        }
        return rgb;
    }

    private int terrainClass(BlockState state) {
        if (isWaterLike(state)) return 1;
        if (state.is(BlockTags.LEAVES)) return 2;
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || isBuiltSurface(state)) return 3;
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.MOSS_BLOCK)) return 4;
        if (isSandLike(state)) return 5;
        if (state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE) || state.is(Blocks.DRIPSTONE_BLOCK)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE)) return 6;
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE)) return 7;
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MUD)) return 8;
        return 0;
    }

    private boolean isBuiltSurface(BlockState state) {
        return state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.BRICKS)
                || state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.CRACKED_STONE_BRICKS)
                || state.is(Blocks.NETHER_BRICKS)
                || state.is(Blocks.RED_NETHER_BRICKS)
                || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.BIRCH_PLANKS)
                || state.is(Blocks.JUNGLE_PLANKS)
                || state.is(Blocks.ACACIA_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.MANGROVE_PLANKS)
                || state.is(Blocks.CHERRY_PLANKS)
                || state.is(Blocks.BAMBOO_PLANKS)
                || state.is(Blocks.CRIMSON_PLANKS)
                || state.is(Blocks.WARPED_PLANKS);
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

    private int livelyRgb(int rgb, float saturation, float brightness) {
        return adjustBrightness(boostSaturation(rgb & 0x00FFFFFF, saturation), brightness);
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
