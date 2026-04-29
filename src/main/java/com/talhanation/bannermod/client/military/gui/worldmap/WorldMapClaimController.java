package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.network.messages.military.MessageClaimIntent;
import com.talhanation.bannermod.network.messages.military.MessageDoPayment;
import com.talhanation.bannermod.network.messages.military.MessageUpdateClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

final class WorldMapClaimController {
    private final WorldMapScreen screen;

    WorldMapClaimController(WorldMapScreen screen) {
        this.screen = screen;
    }

    List<ChunkPos> getClaimArea(ChunkPos pos) {
        List<ChunkPos> area = new ArrayList<>();
        if (pos == null) return area;
        int range = 2;
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                area.add(new ChunkPos(pos.x + dx, pos.z + dz));
            }
        }
        return area;
    }

    void claimArea() {
        Player player = screen.getPlayer();
        int cost = getClaimCost();
        if (!canPlayerPay(cost, player)) return;
        if (!ClientManager.configValueIsClaimingAllowed) return;
        if (!isPlayerInOverworld()) return;

        List<ChunkPos> area = getClaimArea(screen.selectedChunk);
        RecruitsClaim newClaim = new RecruitsClaim(player.getName().getString(), null);
        for (ChunkPos pos : area) {
            newClaim.addChunk(pos);
        }
        newClaim.setCenter(screen.selectedChunk);
        newClaim.setPlayer(new RecruitsPlayerInfo(player.getUUID(), player.getName().getString()));
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDoPayment(player.getUUID(), cost));
        ClientManager.recruitsClaims.add(newClaim);
        ClientManager.markClaimsChanged();
        ClientManager.markClaimsStale();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateClaim(newClaim));
    }

    void claimChunk() {
        Player player = screen.getPlayer();
        if (!canPlayerPay(ClientManager.configValueChunkCost, player)) return;
        if (!ClientManager.configValueIsClaimingAllowed) return;
        if (!isPlayerInOverworld()) return;

        RecruitsClaim neighborClaim = getNeighborClaim(screen.selectedChunk);
        if (neighborClaim == null) return;
        ClientManager.markClaimsStale();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageClaimIntent(MessageClaimIntent.Action.ADD_CHUNK, neighborClaim.getUUID(), screen.selectedChunk));
    }

    @Nullable
    RecruitsClaim getNeighborClaim(ChunkPos chunk) {
        if (chunk == null) return null;
        ChunkPos[] neighbors = {
                new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
                new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1)
        };
        for (ChunkPos neighbor : neighbors) {
            RecruitsClaim claim = ClientManager.getClaimAtChunk(neighbor);
            if (claim != null) return claim;
        }
        return null;
    }

    void recalculateCenter(RecruitsClaim claim) {
        List<ChunkPos> chunks = claim.getClaimedChunks();
        if (chunks.isEmpty()) return;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (ChunkPos pos : chunks) {
            if (pos.x < minX) minX = pos.x;
            if (pos.x > maxX) maxX = pos.x;
            if (pos.z < minZ) minZ = pos.z;
            if (pos.z > maxZ) maxZ = pos.z;
        }
        claim.setCenter(new ChunkPos((minX + maxX) / 2, (minZ + maxZ) / 2));
    }

    boolean canRemoveChunk(ChunkPos pos, RecruitsClaim claim) {
        if (pos == null) return false;
        if (isPlayerTooFar(pos)) return false;

        List<ChunkPos> claimedChunks = claim.getClaimedChunks();
        if (!claimedChunks.contains(pos)) return false;

        int unclaimedNeighbors = 0;
        for (ChunkPos neighbor : new ChunkPos[]{
                new ChunkPos(pos.x + 1, pos.z), new ChunkPos(pos.x - 1, pos.z),
                new ChunkPos(pos.x, pos.z + 1), new ChunkPos(pos.x, pos.z - 1)}) {
            if (!claimedChunks.contains(neighbor)) unclaimedNeighbors++;
        }
        return unclaimedNeighbors >= 2;
    }

    int getClaimCost() {
        if (!ClientManager.configValueCascadeClaimCost) return ClientManager.configValueClaimCost;
        return Math.max(1, ClientManager.recruitsClaims.size() + 1) * ClientManager.configValueClaimCost;
    }

    boolean canPlayerPay(int cost, Player player) {
        return player.isCreative() || cost <= player.getInventory().countItem(ClientManager.getCurrencyItemStackOrDefault().getItem());
    }

    static boolean isInBufferZone(ChunkPos chunk) {
        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (claim.getOwnerPoliticalEntityId() == null) {
                continue;
            }
            for (ChunkPos claimChunk : claim.getClaimedChunks()) {
                int dx = Math.abs(chunk.x - claimChunk.x);
                int dz = Math.abs(chunk.z - claimChunk.z);
                if (dx <= 3 && dz <= 3 && !(dx == 0 && dz == 0)) return true;
            }
        }
        return false;
    }

    boolean canClaimChunk(ChunkPos pos) {
        if (!ClientManager.configValueIsClaimingAllowed || pos == null) return false;
        if (isPlayerTooFar(pos)) return false;
        if (ClientManager.getClaimAtChunk(pos) != null) return false;
        RecruitsClaim neighbor = getNeighborClaim(pos);
        if (neighbor == null || neighbor.getClaimedChunks().size() >= RecruitsClaim.MAX_SIZE) return false;
        return !isInBufferZone(pos);
    }

    boolean canClaimArea(List<ChunkPos> areaChunks) {
        return false;
    }

    List<ChunkPos> getClaimableChunks(ChunkPos center, int radius) {
        List<ChunkPos> result = new ArrayList<>();
        if (center == null) return result;

        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                ChunkPos chunk = new ChunkPos(x, z);
                if (canClaimChunkRaw(chunk)) result.add(chunk);
            }
        }
        return result;
    }

    boolean canClaimChunkRaw(ChunkPos pos) {
        if (ClientManager.getClaimAtChunk(pos) != null) return false;
        RecruitsClaim neighbor = getNeighborClaim(pos);
        if (neighbor == null) return false;
        return !isInBufferZone(pos);
    }

    private boolean isPlayerTooFar(ChunkPos pos) {
        if (pos == null) return true;
        Player player = screen.getPlayer();
        int diffX = Math.abs(player.chunkPosition().x) - Math.abs(pos.x);
        int diffZ = Math.abs(player.chunkPosition().z) - Math.abs(pos.z);
        return Math.abs(diffZ) > 4 || Math.abs(diffX) > 4;
    }

    private boolean isPlayerInOverworld() {
        return screen.getMinecraft().level != null && screen.getMinecraft().level.dimension() == net.minecraft.world.level.Level.OVERWORLD;
    }
}
