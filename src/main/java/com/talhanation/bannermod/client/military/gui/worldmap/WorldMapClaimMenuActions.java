package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.military.MessageClaimIntent;
import com.talhanation.bannermod.network.messages.military.MessageTeleportPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class WorldMapClaimMenuActions {
    private static final Component TEXT_CLAIM_CHUNK = Component.translatable("gui.recruits.map.claim_chunk");
    private static final Component TEXT_CLAIM_AREA = Component.translatable("gui.recruits.map.claim_area");
    private static final Component TEXT_EDIT_CLAIM = Component.translatable("gui.recruits.map.edit_claim");
    private static final Component TEXT_REMOVE_CHUNK = Component.translatable("gui.recruits.map.remove_chunk");
    private static final Component TEXT_REMOVE_CHUNK_ADMIN = Component.translatable("gui.recruits.map.remove_chunk_admin");
    private static final Component TEXT_DELETE_CLAIM_ADMIN = Component.translatable("gui.recruits.map.delete_claim_admin");
    private static final Component TEXT_TELEPORT_ADMIN = Component.translatable("gui.recruits.map.teleport_admin");

    private final WorldMapScreen screen;

    WorldMapClaimMenuActions(WorldMapScreen screen) {
        this.screen = screen;
    }

    void addEntries(WorldMapContextMenu menu, ItemStack claimChunkCost, ItemStack claimAreaCost) {
        menu.addEntry(TEXT_CLAIM_CHUNK.getString(),
                () -> screen.canClaimChunk(screen.selectedChunk)
                        && screen.isPlayerClaimLeader(screen.getNeighborClaim(screen.selectedChunk)),
                WorldMapScreen::claimChunk,
                claimChunkCost,
                "bufferzone, chunk"
        );

        menu.addEntry(TEXT_CLAIM_AREA.getString(),
                () -> screen.canClaimArea(screen.getClaimArea(screen.selectedChunk))
                        && screen.getPlayer() != null,
                WorldMapScreen::claimArea,
                claimAreaCost,
                "bufferzone, area"
        );

        menu.addEntry(TEXT_EDIT_CLAIM.getString(),
                () -> screen.selectedClaim != null
                        && screen.isPlayerClaimLeader(),
                this::openClaimEditor
        );

        menu.addEntry(TEXT_REMOVE_CHUNK.getString(),
                () -> screen.selectedChunk != null && screen.selectedClaim != null
                        && screen.canRemoveChunk(screen.selectedChunk, screen.selectedClaim)
                        && screen.isPlayerClaimLeader(screen.selectedClaim),
                this::removeSelectedChunk
        );

        menu.addEntry(TEXT_REMOVE_CHUNK_ADMIN.getString(),
                () -> screen.selectedChunk != null && screen.selectedClaim != null
                        && screen.isPlayerAdminAndCreative()
                        && screen.canRemoveChunk(screen.selectedChunk, screen.selectedClaim)
                        && !screen.isPlayerClaimLeader(screen.selectedClaim),
                this::removeSelectedChunk,
                "admin"
        );

        menu.addEntry(TEXT_DELETE_CLAIM_ADMIN.getString(),
                () -> screen.isPlayerAdminAndCreative() && screen.selectedClaim != null,
                this::deleteSelectedClaim,
                "admin"
        );

        menu.addEntry(TEXT_TELEPORT_ADMIN.getString(),
                screen::isPlayerAdminAndCreative,
                this::teleportToClickedBlock,
                "admin"
        );
    }

    private void openClaimEditor(WorldMapScreen screen) {
        screen.getMinecraft().setScreen(new ClaimEditScreen(screen, screen.selectedClaim, screen.getPlayer()));
        screen.selectedClaim = null;
    }

    private void removeSelectedChunk(WorldMapScreen screen) {
        if (!screen.selectedClaim.containsChunk(screen.selectedChunk)) return;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageClaimIntent(MessageClaimIntent.Action.REMOVE_CHUNK, screen.selectedClaim.getUUID(), screen.selectedChunk));
        screen.selectedChunk = null;
    }

    private void deleteSelectedClaim(WorldMapScreen screen) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageClaimIntent(MessageClaimIntent.Action.DELETE, screen.selectedClaim.getUUID(), screen.selectedChunk));
        screen.selectedClaim = null;
    }

    private void teleportToClickedBlock(WorldMapScreen screen) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageTeleportPlayer(screen.getClickedBlockPos()));
    }
}
