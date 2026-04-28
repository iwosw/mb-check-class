package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModGovernorRulesTest {

    @Test
    void friendlyClaimAssignmentAllowsGovernorDesignationWhenRecruitAndOwnerExist() {
        ChunkPos chunkPos = new ChunkPos(4, 7);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        String ownerKey = ownerKey(claim);
        UUID recruitUuid = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();

        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, ownerKey);

        assertEquals(BannerModGovernorRules.Decision.ALLOW,
                BannerModGovernorRules.assignmentDecision(binding, recruitUuid, ownerUuid));
        assertTrue(BannerModGovernorRules.isAllowed(
                BannerModGovernorRules.assignmentDecision(binding, recruitUuid, ownerUuid)));
    }

    @Test
    void hostileUnclaimedAndDegradedStatesDenyGovernorControl() {
        ChunkPos chunkPos = new ChunkPos(2, 9);
        RecruitsClaim hostileClaim = claim(chunkPos, "redguild");
        String friendlyOwnerKey = UUID.randomUUID().toString();
        BannerModGovernorSnapshot snapshot = BannerModGovernorSnapshot.create(UUID.randomUUID(), chunkPos, "blueguild")
                .withGovernor(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(BannerModGovernorRules.Decision.HOSTILE_SETTLEMENT,
                BannerModGovernorRules.assignmentDecision(
                        BannerModSettlementBinding.resolveFactionStatus(hostileClaim, chunkPos, friendlyOwnerKey),
                        UUID.randomUUID(),
                        UUID.randomUUID()));
        assertEquals(BannerModGovernorRules.Decision.UNCLAIMED_SETTLEMENT,
                BannerModGovernorRules.controlDecision(
                        BannerModSettlementBinding.resolveSettlementStatus((RecruitsClaim) null, chunkPos, "blueguild"),
                        snapshot));
        assertEquals(BannerModGovernorRules.Decision.DEGRADED_SETTLEMENT,
                BannerModGovernorRules.controlDecision(
                        BannerModSettlementBinding.resolveSettlementStatus(hostileClaim, chunkPos, friendlyOwnerKey),
                        snapshot));
    }

    @Test
    void managerRoundTripsSnapshotsByClaimUuidWithoutChangingClaimIdentity() {
        ChunkPos chunkPos = new ChunkPos(6, 3);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        BannerModGovernorSnapshot original = BannerModGovernorSnapshot.create(claim.getUUID(), claim.getCenter(), ownerKey(claim))
                .withGovernor(UUID.randomUUID(), UUID.randomUUID())
                .withHeartbeatReport(200L, 180L, 5, 12, 9, List.of("stable"), List.of("garrison_low"))
                .withFiscalRollup(new BannerModTreasuryLedgerSnapshot.FiscalRollup(18, 9, 9, 3, 12, 200L));

        BannerModGovernorManager manager = new BannerModGovernorManager();
        manager.putSnapshot(original);

        CompoundTag persisted = manager.save(new CompoundTag(), null);
        BannerModGovernorManager reloaded = BannerModGovernorManager.load(persisted, null);
        BannerModGovernorSnapshot restored = reloaded.getSnapshot(claim.getUUID());

        assertNotNull(restored);
        assertEquals(claim.getUUID(), restored.claimUuid());
        assertEquals(claim.getCenter(), restored.anchorChunk());
        assertEquals(original.governorRecruitUuid(), restored.governorRecruitUuid());
        assertEquals(original.governorOwnerUuid(), restored.governorOwnerUuid());
        assertEquals(original.treasuryBalance(), restored.treasuryBalance());
        assertEquals(original.lastTreasuryNet(), restored.lastTreasuryNet());
        assertEquals(original.projectedTreasuryBalance(), restored.projectedTreasuryBalance());
        assertEquals(original.incidentTokens(), restored.incidentTokens());
        assertNull(reloaded.getSnapshot(UUID.randomUUID()));
    }

    @Test
    void getOrCreateSnapshotMarksSavedDataDirtyWhenSnapshotIsInserted() {
        ChunkPos chunkPos = new ChunkPos(6, 3);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        BannerModGovernorManager manager = new BannerModGovernorManager();

        manager.getOrCreateSnapshot(claim.getUUID(), BannerModGovernorSnapshot.create(claim.getUUID(), claim.getCenter(), ownerKey(claim)));

        assertTrue(manager.isDirty());
    }

    @Test
    void putSnapshotDoesNotMarkSavedDataDirtyWhenSnapshotIsUnchanged() {
        ChunkPos chunkPos = new ChunkPos(6, 3);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        BannerModGovernorSnapshot snapshot = BannerModGovernorSnapshot.create(claim.getUUID(), claim.getCenter(), ownerKey(claim));
        BannerModGovernorManager manager = new BannerModGovernorManager();
        manager.putSnapshot(snapshot);
        BannerModGovernorManager reloaded = BannerModGovernorManager.load(manager.save(new CompoundTag(), null), null);

        reloaded.putSnapshot(snapshot);

        assertFalse(reloaded.isDirty());
    }

    private static RecruitsClaim claim(ChunkPos chunkPos, String factionId) {
        RecruitsClaim claim = new RecruitsClaim(factionId, UUID.nameUUIDFromBytes(factionId.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        claim.addChunk(chunkPos);
        claim.setCenter(chunkPos);
        return claim;
    }

    private static String ownerKey(RecruitsClaim claim) {
        return claim.getOwnerPoliticalEntityId().toString();
    }
}
