package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsFaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModGovernorServiceTest {

    @Test
    void ownerOrAdminCanDesignateFriendlyClaimGovernor() {
        RecruitsClaim claim = claim(new ChunkPos(4, 7), "blueguild");
        UUID ownerUuid = UUID.randomUUID();
        BannerModGovernorService service = new BannerModGovernorService(new BannerModGovernorManager());

        BannerModGovernorService.OperationResult ownerResult = service.assignGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(ownerUuid, "blueguild", false),
                new BannerModGovernorService.RecruitGovernorTarget(UUID.randomUUID(), ownerUuid, "blueguild")
        );
        BannerModGovernorService.OperationResult adminResult = service.assignGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(UUID.randomUUID(), "otherguild", true),
                new BannerModGovernorService.RecruitGovernorTarget(UUID.randomUUID(), ownerUuid, "blueguild")
        );

        assertTrue(ownerResult.allowed());
        assertTrue(adminResult.allowed());
        assertEquals(BannerModGovernorAuthority.Decision.ALLOW, ownerResult.authorityDecision());
        assertEquals(BannerModGovernorAuthority.Decision.ALLOW, adminResult.authorityDecision());
        assertNotNull(service.getGovernorSnapshot(claim.getUUID()));
    }

    @Test
    void sameTeamAndOutsiderCannotAssignOrRevokeGovernorWhenPolicyForbidsIt() {
        RecruitsClaim claim = claim(new ChunkPos(6, 3), "blueguild");
        UUID ownerUuid = UUID.randomUUID();
        BannerModGovernorService service = new BannerModGovernorService(new BannerModGovernorManager());
        BannerModGovernorService.RecruitGovernorTarget recruit =
                new BannerModGovernorService.RecruitGovernorTarget(UUID.randomUUID(), ownerUuid, "blueguild");

        BannerModGovernorService.OperationResult sameTeamAssign = service.assignGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(UUID.randomUUID(), "blueguild", false),
                recruit
        );
        BannerModGovernorService.OperationResult outsiderAssign = service.assignGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(UUID.randomUUID(), "redguild", false),
                recruit
        );

        service.assignGovernor(claim, new BannerModGovernorAuthority.ActorContext(ownerUuid, "blueguild", false), recruit);

        BannerModGovernorService.OperationResult sameTeamRevoke = service.revokeGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(UUID.randomUUID(), "blueguild", false)
        );
        BannerModGovernorService.OperationResult outsiderRevoke = service.revokeGovernor(
                claim,
                new BannerModGovernorAuthority.ActorContext(UUID.randomUUID(), "redguild", false)
        );

        assertFalse(sameTeamAssign.allowed());
        assertFalse(outsiderAssign.allowed());
        assertFalse(sameTeamRevoke.allowed());
        assertFalse(outsiderRevoke.allowed());
        assertEquals(BannerModGovernorAuthority.Decision.FORBIDDEN, sameTeamAssign.authorityDecision());
        assertEquals(BannerModGovernorAuthority.Decision.FORBIDDEN, outsiderAssign.authorityDecision());
    }

    @Test
    void degradedHostileAndUnclaimedSettlementsAreDeniedEvenWhenRecruitExists() {
        UUID ownerUuid = UUID.randomUUID();
        BannerModGovernorService.RecruitGovernorTarget recruit =
                new BannerModGovernorService.RecruitGovernorTarget(UUID.randomUUID(), ownerUuid, "blueguild");

        RecruitsClaim hostileClaim = claim(new ChunkPos(2, 9), "redguild");
        RecruitsClaim degradedClaim = claim(new ChunkPos(3, 5), "redguild");
        BannerModGovernorService hostileService = new BannerModGovernorService(new BannerModGovernorManager());
        BannerModGovernorService degradedService = new BannerModGovernorService(new BannerModGovernorManager());
        BannerModGovernorService unclaimedService = new BannerModGovernorService(new BannerModGovernorManager());

        BannerModGovernorService.OperationResult hostile = hostileService.assignGovernor(
                hostileClaim,
                new BannerModGovernorAuthority.ActorContext(ownerUuid, "blueguild", false),
                recruit,
                "blueguild",
                false
        );
        BannerModGovernorService.OperationResult degraded = degradedService.assignGovernor(
                degradedClaim,
                new BannerModGovernorAuthority.ActorContext(ownerUuid, "redguild", false),
                recruit,
                "blueguild",
                true
        );
        BannerModGovernorService.OperationResult unclaimed = unclaimedService.assignGovernor(
                null,
                new BannerModGovernorAuthority.ActorContext(ownerUuid, "blueguild", false),
                recruit
        );

        assertFalse(hostile.allowed());
        assertFalse(degraded.allowed());
        assertFalse(unclaimed.allowed());
        assertEquals(BannerModGovernorRules.Decision.HOSTILE_SETTLEMENT, hostile.governorDecision());
        assertEquals(BannerModGovernorRules.Decision.DEGRADED_SETTLEMENT, degraded.governorDecision());
        assertEquals(BannerModGovernorRules.Decision.UNCLAIMED_SETTLEMENT, unclaimed.governorDecision());
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
