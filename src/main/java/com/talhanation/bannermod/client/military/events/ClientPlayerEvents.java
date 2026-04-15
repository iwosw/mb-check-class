package com.talhanation.bannermod.client.military.events;

import com.talhanation.bannermod.client.military.gui.worldmap.ChunkTileManager;
import com.talhanation.bannermod.client.military.gui.worldmap.WorldMapScreen;
import com.talhanation.bannermod.config.RecruitsClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientPlayerEvents {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(Minecraft.getInstance().screen instanceof WorldMapScreen)) return;
        if (!RecruitsClientConfig.UpdateMapTiles.get()) return;

        updateMapTiles();
    }

    private void updateMapTiles() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.level.dimension() != Level.OVERWORLD) return;

        ChunkTileManager.getInstance().updateCurrentTile();
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ChunkTileManager.getInstance().initialize((Level) event.getLevel());
        }
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkTileManager.getInstance().close();
        }
    }
}