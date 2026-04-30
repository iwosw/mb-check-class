package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@GameTestHolder(BannerModMain.MOD_ID)
public class WorldMapRenderGameTests {
    private static final Path ROOT = resolveProjectRoot();

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void worldMapInitializesRenderableTileBeforeFirstRender(GameTestHelper helper) {
        String screen = read(helper, "src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/WorldMapScreen.java");
        String tileManager = read(helper, "src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/ChunkTileManager.java");
        String chunkTile = read(helper, "src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/ChunkTile.java");
        String chunkImage = read(helper, "src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/ChunkImage.java");

        helper.assertTrue(screen.contains("tileManager.initialize(minecraft.level);"),
                "World map must initialize tile storage before rendering map tiles");
        helper.assertTrue(screen.contains("tileManager.updateCurrentTile(false);"),
                "World map must populate the current tile during screen init so first render is not blank");
        helper.assertTrue(screen.contains("tileManager.updateTileIfStale(centerTileX, centerTileZ"),
                "World map must refresh the centered visible tile when rendering a panned map view");
        helper.assertTrue(screen.contains("MAX_VISIBLE_TILE_UPDATES_PER_FRAME"),
                "World map visible-tile refresh must be bounded per frame");
        helper.assertTrue(screen.contains("renderMapTiles(guiGraphics);"),
                "World map render path must draw map tiles before overlays");
        helper.assertTrue(screen.contains("renderBackground(guiGraphics, mouseX, mouseY, partialTicks);\n        guiGraphics.flush();"),
                "World map must flush the buffered background before immediate tile blits or the background can cover the map");
        helper.assertTrue(!screen.contains("super.render(guiGraphics, mouseX, mouseY, partialTicks);"),
                "World map must not call Screen.render after map tiles because Screen.render redraws the background over the map");
        helper.assertTrue(screen.contains("ResourceLocation textureId = tile.getTextureId();"),
                "World map render must use the tile texture id produced by ChunkTile");
        helper.assertTrue(screen.contains("double tileSize = ChunkTile.TILE_BLOCK_SIZE;"),
                "World map render math must use world block tile size so higher texture resolution does not change zoom");
        helper.assertTrue(screen.contains("if (textureId == null)"),
                "World map render must skip only tiles without a registered texture");
        helper.assertTrue(screen.contains("[WorldMap] render"),
                "World map render path must emit throttled diagnostics when tile rendering is blank");
        helper.assertTrue(!screen.contains("textures/map/map_icons.png"),
                "World map player marker must not reference the removed vanilla map icon texture");
        helper.assertTrue(tileManager.contains("updateOnlyLoadedChunks(tile);"),
                "Tile manager must update loaded chunk pixels before saving/rendering tiles");
        helper.assertTrue(tileManager.contains("boolean updateTileIfStale"),
                "Tile manager must expose a cooldown-gated visible-tile refresh for map rendering");
        helper.assertTrue(chunkTile.contains("this.textureId = mc.getTextureManager().register"),
                "Chunk tiles must register a DynamicTexture that renderMapTiles can blit");
        helper.assertTrue(chunkTile.contains("PIXELS_PER_BLOCK = 2")
                        && chunkTile.contains("TILE_BLOCK_SIZE")
                        && chunkTile.contains("TILE_PIXEL_SIZE")
                        && chunkTile.contains("newInputStream(tileFile.toPath())")
                        && chunkTile.contains("NativeImage.read(stream)")
                        && !chunkTile.contains("NativeImage.read(tileFile"),
                "Chunk tiles must separate world tile size from 2x texture resolution");
        helper.assertTrue(chunkImage.contains("getTerrainColor(level")
                        && chunkImage.contains("level.getBiome(pos).value().getGrassColor")
                        && chunkImage.contains("applyRelief(level, pos, rgb)")
                        && chunkImage.contains("applyTinyNoise(pos, rgb)")
                        && chunkImage.contains("applySubpixelShade(pos, subX, subZ, rgb)")
                        && chunkImage.contains("isSandLike(state) && isNextToWater"),
                "World map chunk pixels must use the conservative Xaero-like flat-first terrain pipeline");
        helper.assertTrue(chunkImage.contains("toNativeAbgr(rgb)"),
                "World map chunk pixels must convert MapColor RGB into NativeImage ABGR order");

        helper.succeed();
    }

    private static String read(GameTestHelper helper, String relativePath) {
        try {
            return Files.readString(ROOT.resolve(relativePath)).replace("\r\n", "\n");
        } catch (IOException e) {
            helper.fail("Could not read " + relativePath + ": " + e.getMessage());
            return "";
        }
    }

    private static Path resolveProjectRoot() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (Files.exists(cwd.resolve("src/main/java"))) {
            return cwd;
        }
        Path parent = cwd.getParent();
        if (parent != null && Files.exists(parent.resolve("src/main/java"))) {
            return parent;
        }
        return cwd;
    }
}
