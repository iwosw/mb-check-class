package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.network.messages.military.MessageDoPayment;
import com.talhanation.bannermod.network.messages.military.MessageUpdateClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        RecruitsFaction ownFaction = ClientManager.ownFaction;
        int cost = getClaimCost(ownFaction);
        if (!canPlayerPay(cost, player)) return;
        if (!ClientManager.configValueIsClaimingAllowed) return;
        if (!isPlayerInOverworld()) return;

        List<ChunkPos> area = getClaimArea(screen.selectedChunk);
        RecruitsClaim newClaim = new RecruitsClaim(ownFaction);
        for (ChunkPos pos : area) {
            newClaim.addChunk(pos);
        }
        newClaim.setCenter(screen.selectedChunk);
        newClaim.setPlayer(new RecruitsPlayerInfo(player.getUUID(), player.getName().getString(), ownFaction));
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDoPayment(player.getUUID(), cost));
        ClientManager.recruitsClaims.add(newClaim);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateClaim(newClaim));
    }

    void claimChunk() {
        Player player = screen.getPlayer();
        if (!canPlayerPay(ClientManager.configValueChunkCost, player)) return;
        if (!ClientManager.configValueIsClaimingAllowed) return;
        if (!isPlayerInOverworld()) return;

        RecruitsClaim neighborClaim = getNeighborClaim(screen.selectedChunk);
        if (neighborClaim == null) return;
        if (!Objects.equals(ClientManager.ownFaction.getStringID(), neighborClaim.getOwnerFaction().getStringID())) return;

        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (claim.equals(neighborClaim)) {
                neighborClaim.addChunk(screen.selectedChunk);
                recalculateCenter(neighborClaim);
                break;
            }
        }

        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDoPayment(player.getUUID(), ClientManager.configValueChunkCost));
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateClaim(neighborClaim));
    }

    @Nullable
    RecruitsClaim getNeighborClaim(ChunkPos chunk) {
        if (chunk == null) return null;
        ChunkPos[] neighbors = {
                new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
                new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1)
        };
        for (ChunkPos neighbor : neighbors) {
            for (RecruitsClaim claim : ClientManager.recruitsClaims) {
                if (claim.containsChunk(neighbor)) return claim;
            }
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
        if (pos == null || ClientManager.ownFaction == null) return false;
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

    int getClaimCost(RecruitsFaction ownerTeam) {
        if (!ClientManager.configValueCascadeClaimCost) return ClientManager.configValueClaimCost;

        int amount = 1;
        if (ownerTeam != null) {
            for (RecruitsClaim claim : ClientManager.recruitsClaims) {
                if (claim.getOwnerFaction().getStringID().equals(ownerTeam.getStringID())) amount++;
            }
        }
        return amount * ClientManager.configValueClaimCost;
    }

    boolean canPlayerPay(int cost, Player player) {
        return player.isCreative() || cost <= player.getInventory().countItem(ClientManager.getCurrencyItemStackOrDefault().getItem());
    }

    static boolean isInBufferZone(ChunkPos chunk, RecruitsFaction ownFaction) {
        if (ownFaction == null) return false;
        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (claim.getOwnerFaction() == null || claim.getOwnerFaction().getStringID().equals(ownFaction.getStringID())) {
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
        if (!ClientManager.configValueIsClaimingAllowed || pos == null || ClientManager.ownFaction == null) return false;
        if (isPlayerTooFar(pos)) return false;
        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (claim.containsChunk(pos)) return false;
        }
        RecruitsClaim neighbor = getNeighborClaim(pos);
        if (neighbor == null || neighbor.getClaimedChunks().size() >= RecruitsClaim.MAX_SIZE) return false;
        return !isInBufferZone(pos, ClientManager.ownFaction);
    }

    boolean canClaimArea(List<ChunkPos> areaChunks) {
        if (screen.selectedChunk == null || areaChunks == null || areaChunks.isEmpty() || ClientManager.ownFaction == null) {
            return false;
        }
        if (isPlayerTooFar(screen.selectedChunk)) return false;

        for (ChunkPos chunk : areaChunks) {
            for (RecruitsClaim claim : ClientManager.recruitsClaims) {
                if (claim.containsChunk(chunk)) return false;
            }
            if (isInBufferZone(chunk, ClientManager.ownFaction)) return false;
        }
        return true;
    }

    List<ChunkPos> getClaimableChunks(ChunkPos center, int radius) {
        List<ChunkPos> result = new ArrayList<>();
        if (center == null || ClientManager.ownFaction == null) return result;

        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                ChunkPos chunk = new ChunkPos(x, z);
                if (canClaimChunkRaw(chunk)) result.add(chunk);
            }
        }
        return result;
    }

    boolean canClaimChunkRaw(ChunkPos pos) {
        for (RecruitsClaim claim : ClientManager.recruitsClaims) {
            if (claim.containsChunk(pos)) return false;
        }
        RecruitsClaim neighbor = getNeighborClaim(pos);
        if (neighbor == null) return false;
        return !isInBufferZone(pos, ClientManager.ownFaction);
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
