package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementBindingTest {

    @Test
    void friendlyClaimStatusAllowsPlacementAndSettlementOperation() {
        ChunkPos chunkPos = new ChunkPos(4, 7);
        RecruitsClaim claim = claim(chunkPos, "blueguild");

        BannerModSettlementBinding.Binding placementBinding = BannerModSettlementBinding.resolveFactionStatus(claim, chunkPos, "blueguild");
        BannerModSettlementBinding.Binding settlementBinding = BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, "blueguild");

        assertEquals(BannerModSettlementBinding.Status.FRIENDLY_CLAIM, placementBinding.status());
        assertTrue(placementBinding.isFriendly());
        assertEquals(BannerModSettlementBinding.Status.FRIENDLY_CLAIM, settlementBinding.status());
        assertTrue(BannerModSettlementBinding.allowsSettlementOperation(settlementBinding));
    }

    @Test
    void hostileClaimStatusStaysNonFriendlyForPlacementChecks() {
        ChunkPos chunkPos = new ChunkPos(2, 9);
        RecruitsClaim claim = claim(chunkPos, "redguild");

        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveFactionStatus(claim, chunkPos, "blueguild");

        assertEquals(BannerModSettlementBinding.Status.HOSTILE_CLAIM, binding.status());
        assertFalse(binding.isFriendly());
    }

    @Test
    void unclaimedStatusStaysExplicitWhenNoClaimCoversTheChunk() {
        ChunkPos chunkPos = new ChunkPos(1, 1);

        BannerModSettlementBinding.Binding placementBinding = BannerModSettlementBinding.resolveFactionStatus((RecruitsClaim) null, chunkPos, "blueguild");
        BannerModSettlementBinding.Binding settlementBinding = BannerModSettlementBinding.resolveSettlementStatus((RecruitsClaim) null, chunkPos, "blueguild");

        assertEquals(BannerModSettlementBinding.Status.UNCLAIMED, placementBinding.status());
        assertEquals(BannerModSettlementBinding.Status.UNCLAIMED, settlementBinding.status());
        assertFalse(BannerModSettlementBinding.allowsSettlementOperation(settlementBinding));
    }

    @Test
    void degradedMismatchStatusMarksSettlementFootprintsThatLostFactionAlignment() {
        ChunkPos chunkPos = new ChunkPos(6, 3);
        RecruitsClaim claim = claim(chunkPos, "redguild");

        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, "blueguild");

        assertEquals(BannerModSettlementBinding.Status.DEGRADED_MISMATCH, binding.status());
        assertEquals("blueguild", binding.settlementFactionId());
        assertEquals("redguild", binding.claimFactionId());
        assertFalse(BannerModSettlementBinding.allowsSettlementOperation(binding));
    }

    private static RecruitsClaim claim(ChunkPos chunkPos, String factionId) {
        RecruitsFaction faction = new RecruitsFaction(factionId, "leader", new CompoundTag());
        RecruitsClaim claim = instantiateClaim(factionId, faction);
        claim.addChunk(chunkPos);
        return claim;
    }

    private static RecruitsClaim instantiateClaim(String name, RecruitsFaction faction) {
        try {
            Constructor<RecruitsClaim> constructor = RecruitsClaim.class.getDeclaredConstructor(UUID.class, String.class, RecruitsFaction.class);
            constructor.setAccessible(true);
            return constructor.newInstance(UUID.randomUUID(), name, faction);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create claim fixture", exception);
        }
    }
}
