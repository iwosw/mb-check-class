package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsFaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModGovernorRulesTest {

    @Test
    void friendlyClaimAssignmentAllowsGovernorDesignationWhenRecruitAndOwnerExist() {
        ChunkPos chunkPos = new ChunkPos(4, 7);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        UUID recruitUuid = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();

        BannerModSettlementBinding.Binding binding = BannerModSettlementBinding.resolveSettlementStatus(claim, chunkPos, "blueguild");

        assertEquals(BannerModGovernorRules.Decision.ALLOW,
                BannerModGovernorRules.assignmentDecision(binding, recruitUuid, ownerUuid));
        assertTrue(BannerModGovernorRules.isAllowed(
                BannerModGovernorRules.assignmentDecision(binding, recruitUuid, ownerUuid)));
    }

    @Test
    void hostileUnclaimedAndDegradedStatesDenyGovernorControl() {
        ChunkPos chunkPos = new ChunkPos(2, 9);
        RecruitsClaim hostileClaim = claim(chunkPos, "redguild");
        BannerModGovernorSnapshot snapshot = BannerModGovernorSnapshot.create(UUID.randomUUID(), chunkPos, "blueguild")
                .withGovernor(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(BannerModGovernorRules.Decision.HOSTILE_SETTLEMENT,
                BannerModGovernorRules.assignmentDecision(
                        BannerModSettlementBinding.resolveFactionStatus(hostileClaim, chunkPos, "blueguild"),
                        UUID.randomUUID(),
                        UUID.randomUUID()));
        assertEquals(BannerModGovernorRules.Decision.UNCLAIMED_SETTLEMENT,
                BannerModGovernorRules.controlDecision(
                        BannerModSettlementBinding.resolveSettlementStatus((RecruitsClaim) null, chunkPos, "blueguild"),
                        snapshot));
        assertEquals(BannerModGovernorRules.Decision.DEGRADED_SETTLEMENT,
                BannerModGovernorRules.controlDecision(
                        BannerModSettlementBinding.resolveSettlementStatus(hostileClaim, chunkPos, "blueguild"),
                        snapshot));
    }

    @Test
    void managerRoundTripsSnapshotsByClaimUuidWithoutChangingClaimIdentity() {
        ChunkPos chunkPos = new ChunkPos(6, 3);
        RecruitsClaim claim = claim(chunkPos, "blueguild");
        BannerModGovernorSnapshot original = BannerModGovernorSnapshot.create(claim.getUUID(), claim.getCenter(), claim.getOwnerFactionStringID())
                .withGovernor(UUID.randomUUID(), UUID.randomUUID())
                .withHeartbeatReport(200L, 180L, 5, 12, 9, List.of("stable"), List.of("garrison_low"));

        BannerModGovernorManager manager = new BannerModGovernorManager();
        manager.putSnapshot(original);

        CompoundTag persisted = manager.save(new CompoundTag());
        BannerModGovernorManager reloaded = BannerModGovernorManager.load(persisted);
        BannerModGovernorSnapshot restored = reloaded.getSnapshot(claim.getUUID());

        assertNotNull(restored);
        assertEquals(claim.getUUID(), restored.claimUuid());
        assertEquals(claim.getCenter(), restored.anchorChunk());
        assertEquals(original.governorRecruitUuid(), restored.governorRecruitUuid());
        assertEquals(original.governorOwnerUuid(), restored.governorOwnerUuid());
        assertEquals(original.incidentTokens(), restored.incidentTokens());
        assertNull(reloaded.getSnapshot(UUID.randomUUID()));
    }

    private static RecruitsClaim claim(ChunkPos chunkPos, String factionId) {
        RecruitsFaction faction = new RecruitsFaction(factionId, "leader", new CompoundTag());
        RecruitsClaim claim = instantiateClaim(factionId, faction);
        claim.addChunk(chunkPos);
        claim.setCenter(chunkPos);
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
