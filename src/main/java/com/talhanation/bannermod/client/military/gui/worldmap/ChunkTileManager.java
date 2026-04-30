package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ChunkTileManager {
    private static final long TILE_SAVE_COOLDOWN_MS = 2000;
    private static final long DEBUG_LOG_COOLDOWN_MS = 2000;
    private static ChunkTileManager instance;
    private final Map<String, ChunkTile> loadedTiles = new HashMap<>();
    private final Minecraft mc = Minecraft.getInstance();
    private File worldMapDir;
    private int currentTileX = Integer.MAX_VALUE;
    private int currentTileZ = Integer.MAX_VALUE;
    private final Map<String, Long> lastUpdateTimes = new HashMap<>();
    private final Map<String, Long> lastSaveTimes = new HashMap<>();
    private long lastNeighborUpdateTime = 0;
    private long lastDebugLogTime = 0;
    private int lastUpdatedNeighborIndex = 0;

    public static ChunkTileManager getInstance() {
        if (instance == null) instance = new ChunkTileManager();
        return instance;
    }

    public void initialize(Level level) {
        if (level == null) return;
        String worldName = detectStorageId();
        this.worldMapDir = new File(mc.gameDirectory, "recruits/worldmap/" + worldName);
        this.worldMapDir.mkdirs();
        BannerModMain.LOGGER.info("[WorldMap] initialized tile storage at {}", this.worldMapDir.getAbsolutePath());
    }

    public void updateCurrentTile() {
        updateCurrentTile(true);
    }

    public void updateCurrentTile(boolean updateNeighbors) {
        if (mc.level == null || mc.player == null) return;

        int chunkX = mc.player.chunkPosition().x;
        int chunkZ = mc.player.chunkPosition().z;
        int tileX = ChunkTile.chunkToTileCoord(chunkX);
        int tileZ = ChunkTile.chunkToTileCoord(chunkZ);
        String currentTileKey = tileX + "_" + tileZ;

        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTimes.get(currentTileKey);

        if (tileX != currentTileX || tileZ != currentTileZ ||
                lastUpdate == null || currentTime - lastUpdate > 1000) {
            updateTile(tileX, tileZ);
            currentTileX = tileX;
            currentTileZ = tileZ;
        }

        if (updateNeighbors && currentTime - lastNeighborUpdateTime >= 500) {
            updateOneNeighborTile(tileX, tileZ);
            lastNeighborUpdateTime = currentTime;
        }
    }

    private void updateOneNeighborTile(int centerX, int centerZ) {
        int[][] neighbors = {
                {centerX-1, centerZ-1}, {centerX, centerZ-1}, {centerX+1, centerZ-1},
                {centerX-1, centerZ}, {centerX+1, centerZ},
                {centerX-1, centerZ+1}, {centerX, centerZ+1}, {centerX+1, centerZ+1}
        };

        if (lastUpdatedNeighborIndex >= neighbors.length) lastUpdatedNeighborIndex = 0;
        int[] neighbor = neighbors[lastUpdatedNeighborIndex];

        String neighborKey = neighbor[0] + "_" + neighbor[1];
        Long neighborLastUpdate = lastUpdateTimes.get(neighborKey);
        if (neighborLastUpdate == null || System.currentTimeMillis() - neighborLastUpdate > 10000) {
            updateTile(neighbor[0], neighbor[1]);
        }
        lastUpdatedNeighborIndex++;
    }

    private TileUpdateStats updateTile(int tileX, int tileZ) {
        ChunkTile tile = getOrCreateTile(tileX, tileZ);
        File tileFile = getTileFile(tileX, tileZ);
        TileUpdateStats stats = updateOnlyLoadedChunks(tile);
        long currentTime = System.currentTimeMillis();
        tile.uploadIfNeeded();
        saveTileIfDue(tile, tileFile, currentTime);
        lastUpdateTimes.put(tileX + "_" + tileZ, currentTime);
        logTileUpdateIfDue(tileX, tileZ, stats, tileFile, currentTime);
        return stats;
    }

    public boolean updateTileIfStale(int tileX, int tileZ, long maxAgeMs) {
        String key = tileX + "_" + tileZ;
        Long lastUpdate = lastUpdateTimes.get(key);
        if (lastUpdate != null && System.currentTimeMillis() - lastUpdate < maxAgeMs) return false;

        updateTile(tileX, tileZ);
        return true;
    }

    private void saveTileIfDue(ChunkTile tile, File tileFile, long currentTime) {
        String key = tile.getTileX() + "_" + tile.getTileZ();
        Long lastSave = lastSaveTimes.get(key);
        if (!tile.needsSave() || lastSave != null && currentTime - lastSave < TILE_SAVE_COOLDOWN_MS) return;

        tile.saveToFile(tileFile);
        lastSaveTimes.put(key, currentTime);
    }

    private TileUpdateStats updateOnlyLoadedChunks(ChunkTile tile) {
        if (mc.level == null || mc.player == null) return TileUpdateStats.EMPTY;

        int startChunkX = ChunkTile.tileToChunkCoord(tile.getTileX());
        int startChunkZ = ChunkTile.tileToChunkCoord(tile.getTileZ());
        int loadedChunks = 0;
        int meaningfulChunks = 0;
        int changedChunks = 0;
        int firstMeaningfulPixels = 0;
        int firstSamplePixel = 0;

        for (int cz = 0; cz < ChunkTile.TILE_SIZE; cz++) {
            for (int cx = 0; cx < ChunkTile.TILE_SIZE; cx++) {
                ChunkPos chunkPos = new ChunkPos(startChunkX + cx, startChunkZ + cz);
                if (isChunkLoaded(chunkPos)) {
                    loadedChunks++;
                    ChunkImage chunkImage = new ChunkImage(mc.level, chunkPos);
                    if (chunkImage.isMeaningful()) {
                        meaningfulChunks++;
                        if (firstMeaningfulPixels == 0) {
                            firstMeaningfulPixels = chunkImage.getMeaningfulPixelCount();
                            firstSamplePixel = chunkImage.getSamplePixel();
                        }
                    }
                    if (tile.updateFromChunkImage(chunkImage, cx, cz)) {
                        changedChunks++;
                    }
                    chunkImage.close();
                }
            }
        }
        return new TileUpdateStats(loadedChunks, meaningfulChunks, changedChunks, firstMeaningfulPixels, firstSamplePixel);
    }

    private void logTileUpdateIfDue(int tileX, int tileZ, TileUpdateStats stats, File tileFile, long currentTime) {
        if (currentTime - lastDebugLogTime < DEBUG_LOG_COOLDOWN_MS) return;
        lastDebugLogTime = currentTime;
        String dimension = mc.level == null ? "null" : mc.level.dimension().location().toString();
        String playerChunk = mc.player == null ? "null" : mc.player.chunkPosition().toString();
        BannerModMain.LOGGER.info(
                "[WorldMap] update tile={}_{} dim={} playerChunk={} loadedChunks={} meaningfulChunks={} changedChunks={} firstPixels={} samplePixel=0x{} file={}",
                tileX,
                tileZ,
                dimension,
                playerChunk,
                stats.loadedChunks,
                stats.meaningfulChunks,
                stats.changedChunks,
                stats.firstMeaningfulPixels,
                Integer.toHexString(stats.firstSamplePixel),
                tileFile.getAbsolutePath());
    }

    private boolean isChunkLoaded(ChunkPos chunkPos) {
        if (mc.level == null || mc.player == null) return false;
        try {
            return mc.level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public ChunkTile getOrCreateTile(int tileX, int tileZ) {
        String key = tileX + "_" + tileZ;
        ChunkTile tile = loadedTiles.get(key);
        if (tile == null) {
            tile = new ChunkTile(tileX, tileZ);
            tile.loadOrCreate(getTileFile(tileX, tileZ));
            loadedTiles.put(key, tile);
        }
        tile.markAccessed();
        return tile;
    }

    private static String detectStorageId() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSingleplayerServer() != null) {
                String levelName = mc.getSingleplayerServer().getWorldData().getLevelName();
                if (levelName != null && !levelName.isEmpty())
                    return levelName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
            }
            ServerData sd = mc.getCurrentServer();
            if (sd != null && sd.ip != null && !sd.ip.isEmpty())
                return sd.ip.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
        } catch (Exception ignored) {}
        return "unknown";
    }

    private File getTileFile(int tileX, int tileZ) {
        if (worldMapDir == null) {
            initialize(mc.level);
        }
        return new File(worldMapDir, tileX + "_" + tileZ + ".png");
    }

    private record TileUpdateStats(int loadedChunks, int meaningfulChunks, int changedChunks, int firstMeaningfulPixels, int firstSamplePixel) {
        private static final TileUpdateStats EMPTY = new TileUpdateStats(0, 0, 0, 0, 0);
    }

    public void close() {
        for (ChunkTile tile : loadedTiles.values()) {
            tile.saveToFile(getTileFile(tile.getTileX(), tile.getTileZ()));
            tile.close();
        }
        loadedTiles.clear();
        lastSaveTimes.clear();
    }

    public Map<String, ChunkTile> getLoadedTiles() {
        return loadedTiles;
    }

    public boolean isChunkExplored(ChunkPos chunk) {
        int tileX = ChunkTile.chunkToTileCoord(chunk.x);
        int tileZ = ChunkTile.chunkToTileCoord(chunk.z);
        ChunkTile tile = loadedTiles.get(tileX + "_" + tileZ);
        if (tile == null || tile.getImage() == null) return false;

        int localX = Math.floorMod(chunk.x, ChunkTile.TILE_SIZE) * ChunkTile.PIXELS_PER_CHUNK + ChunkTile.PIXELS_PER_CHUNK / 2;
        int localZ = Math.floorMod(chunk.z, ChunkTile.TILE_SIZE) * ChunkTile.PIXELS_PER_CHUNK + ChunkTile.PIXELS_PER_CHUNK / 2;

        return (tile.getImage().getPixelRGBA(localX, localZ) >> 24 & 0xFF) > 0;
    }
}
