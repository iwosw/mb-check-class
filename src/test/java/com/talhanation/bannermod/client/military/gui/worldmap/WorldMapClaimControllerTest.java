package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldMapClaimControllerTest {
    private final List<RecruitsClaim> originalClaims = ClientManager.recruitsClaims;

    @AfterEach
    void restoreClaims() {
        ClientManager.recruitsClaims = originalClaims;
    }

    @Test
    void bufferZoneStillFlagsForeignPoliticalClaims() {
        RecruitsClaim expanding = claim(UUID.fromString("00000000-0000-0000-0000-000000000011"), 0, 0);
        RecruitsClaim foreign = claim(UUID.fromString("00000000-0000-0000-0000-000000000022"), 4, 0);
        ClientManager.recruitsClaims = new ArrayList<>(List.of(expanding, foreign));

        assertTrue(WorldMapClaimController.isInBufferZone(new ChunkPos(3, 0), expanding));
    }

    @Test
    void ownPoliticalClaimEdgeIsNotTreatedAsForeignBufferZone() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000011");
        RecruitsClaim expanding = claim(owner, 0, 0);
        RecruitsClaim sameOwner = claim(owner, 4, 0);
        ClientManager.recruitsClaims = new ArrayList<>(List.of(expanding, sameOwner));

        assertFalse(WorldMapClaimController.isInBufferZone(new ChunkPos(1, 0), expanding));
        assertFalse(WorldMapClaimController.isInBufferZone(new ChunkPos(3, 0), expanding));
        assertTrue(WorldMapClaimController.isInBufferZone(new ChunkPos(1, 0)));
    }

    private static RecruitsClaim claim(UUID ownerPoliticalEntityId, int chunkX, int chunkZ) {
        RecruitsClaim claim = new RecruitsClaim("Claim-" + chunkX + "-" + chunkZ, ownerPoliticalEntityId);
        claim.addChunk(new ChunkPos(chunkX, chunkZ));
        claim.setCenter(new ChunkPos(chunkX, chunkZ));
        return claim;
    }
}
