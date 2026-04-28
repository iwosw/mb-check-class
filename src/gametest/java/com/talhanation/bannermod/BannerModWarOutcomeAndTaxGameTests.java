package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.governance.BannerModTreasuryManager;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.audit.WarAuditEntry;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.cooldown.WarCooldownKind;
import com.talhanation.bannermod.war.cooldown.WarCooldownPolicy;
import com.talhanation.bannermod.war.runtime.OccupationRecord;
import com.talhanation.bannermod.war.runtime.OccupationRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.war.runtime.WarOutcomeApplier;
import com.talhanation.bannermod.war.runtime.WarState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Live-runtime gametests for the WAR-001/002/004 slices. Each test exercises the real
 * {@link WarRuntimeContext} stack on a {@link ServerLevel} (registry, declarations,
 * occupations, cooldowns, audit, treasury) so that the wiring between
 * {@code WarOutcomeApplier} / {@code OccupationTaxRuntime} / {@code WarCooldownPolicy}
 * and the live SavedData layer cannot drift away from the pure-JUnit contracts.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModWarOutcomeAndTaxGameTests {

    private static final UUID ATTACKER_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000004001");
    private static final UUID DEFENDER_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000004002");
    private static final String ATTACKER_TEAM_ID = "war_test_attacker";
    private static final String DEFENDER_TEAM_ID = "war_test_defender";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void warOccupyAppliesThroughLiveApplierAndAudits(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Setup setup = setupAttackerDefender(helper, level, "occupy");

        WarOutcomeApplier applier = WarRuntimeContext.applierFor(level);
        WarOutcomeApplier.Result result = applier.applyOccupy(
                setup.warId,
                List.of(new ChunkPos(setup.defenderClaimPos)),
                level.getGameTime());

        helper.assertTrue(result.valid(),
                "Expected applyOccupy through live applier to succeed for a fresh declared war");
        helper.assertTrue(WarRuntimeContext.declarations(level).byId(setup.warId).orElseThrow().state() == WarState.RESOLVED,
                "Expected the war to resolve after live applyOccupy");
        helper.assertTrue(WarRuntimeContext.occupations(level).forOccupied(setup.defenderEntityId).size() == 1,
                "Expected exactly one occupation persisted against the defender entity");
        helper.assertTrue(latestAuditDetail(WarRuntimeContext.audit(level), setup.warId).contains("type=OCCUPATION"),
                "Expected the live audit log to record the occupation outcome");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void occupationTaxAccrualMovesTreasuryAndAuditsThroughLiveRuntime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Setup setup = setupAttackerDefender(helper, level, "tax");
        UUID attackerClaimOwnerId = setup.attackerEntityId;
        UUID defenderClaimOwnerId = setup.defenderEntityId;
        setup.attackerClaim.setOwnerPoliticalEntityId(attackerClaimOwnerId);
        setup.defenderClaim.setOwnerPoliticalEntityId(defenderClaimOwnerId);
        ClaimEvents.recruitsClaimManager.testInsertClaim(setup.attackerClaim);
        ClaimEvents.recruitsClaimManager.testInsertClaim(setup.defenderClaim);
        seedTreasury(level, setup.defenderClaim, 100);
        // Ensure the occupier has a destination ledger.
        seedTreasury(level, setup.attackerClaim, 0);

        OccupationRuntime occupations = WarRuntimeContext.occupations(level);
        long placedAt = level.getGameTime();
        OccupationRecord placed = occupations.place(
                setup.warId, attackerClaimOwnerId, defenderClaimOwnerId,
                List.of(new ChunkPos(setup.defenderClaimPos)),
                placedAt).orElseThrow();

        // tickAtAccrue is +15 from placement; intervalTicks=10 → exactly one cycle is due.
        long intervalTicks = 10L;
        long tickAtAccrue = placedAt + 15L;

        BannerModTreasuryManager treasury = BannerModTreasuryManager.get(level);
        int defenderBalanceBeforeTax = treasury.getLedger(setup.defenderClaim.getUUID()).treasuryBalance();
        int attackerBalanceBeforeTax = treasury.getLedger(setup.attackerClaim.getUUID()).treasuryBalance();
        WarRuntimeContext.taxRuntime(level).accrue(5, intervalTicks, tickAtAccrue);

        int defenderBalanceAfterTax = treasury.getLedger(setup.defenderClaim.getUUID()).treasuryBalance();
        int attackerBalanceAfterTax = treasury.getLedger(setup.attackerClaim.getUUID()).treasuryBalance();
        helper.assertTrue(defenderBalanceBeforeTax - defenderBalanceAfterTax >= 5,
                "Expected defender claim treasury debited by at least 5 after one tax cycle, got before="
                        + defenderBalanceBeforeTax + " after=" + defenderBalanceAfterTax);
        helper.assertTrue(attackerBalanceAfterTax - attackerBalanceBeforeTax >= 5,
                "Expected attacker claim treasury credited by at least 5 after one tax cycle, got before="
                        + attackerBalanceBeforeTax + " after=" + attackerBalanceAfterTax);

        WarAuditEntry paid = latestAuditOfType(WarRuntimeContext.audit(level), setup.warId, "OCCUPATION_TAX_PAID");
        helper.assertTrue(paid != null && paid.detail().contains("paid=5"),
                "Expected OCCUPATION_TAX_PAID audit entry recording paid=5");
        helper.assertTrue(occupations.byId(placed.id()).orElseThrow().lastTaxedAtGameTime()
                        == placedAt + intervalTicks,
                "Expected occupation lastTaxedAt to advance by exactly one interval");

        // Second accrue at the same logical tick is inside the next interval window — no-op.
        int paidAuditCountBefore = auditCount(WarRuntimeContext.audit(level), setup.warId, "OCCUPATION_TAX_PAID");
        WarRuntimeContext.taxRuntime(level).accrue(5, intervalTicks, tickAtAccrue);
        helper.assertTrue(treasury.getLedger(setup.defenderClaim.getUUID()).treasuryBalance() == defenderBalanceAfterTax,
                "Expected idempotent second accrue to leave defender balance untouched");
        helper.assertTrue(treasury.getLedger(setup.attackerClaim.getUUID()).treasuryBalance() == attackerBalanceAfterTax,
                "Expected idempotent second accrue to leave attacker balance untouched");
        helper.assertTrue(auditCount(WarRuntimeContext.audit(level), setup.warId, "OCCUPATION_TAX_PAID") == paidAuditCountBefore,
                "Expected idempotent second accrue to add zero new paid audit entries for this war");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void lostTerritoryImmunityBlocksRedeclarationThroughLiveCooldown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Setup setup = setupAttackerDefender(helper, level, "immunity");

        // Apply tribute (any outcome that grants LOST_TERRITORY_IMMUNITY); the small seeded balance
        // makes the transfer real, but immunity grant is independent of the paid amount.
        seedTreasury(level, setup.defenderClaim, 5);
        WarRuntimeContext.applierFor(level).applyTribute(setup.warId, 1L, level.getGameTime());

        // Verify the cooldown is active right now via the live cooldown runtime.
        boolean active = WarRuntimeContext.cooldowns(level)
                .isActive(setup.defenderEntityId, WarCooldownKind.LOST_TERRITORY_IMMUNITY, level.getGameTime());
        helper.assertTrue(active,
                "Expected lost-territory immunity to be granted to defender after applyTribute");

        // Try a redeclaration from a DIFFERENT attacker. Using the same attacker would trip
        // peace_cooldown_active first (samePair, recently resolved war), which is correct
        // policy behavior but masks the immunity check. A fresh attacker isolates immunity.
        UUID secondAttackerLeader = uniqueLeaderUuid(ATTACKER_LEADER_UUID, "immunity_v2", true);
        BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level, secondAttackerLeader, "war-attacker-immunity-v2");
        BannerModDedicatedServerGameTestSupport.ensureFaction(
                level, "war_test_attacker_immunity_v2", secondAttackerLeader, "war-attacker-immunity-v2");
        UUID secondAttackerEntityId = WarRuntimeContext.registry(level)
                .byName("war_test_attacker_immunity_v2").orElseThrow().id();

        var policyResult = WarCooldownPolicy.canDeclareWithImmunity(
                secondAttackerEntityId,
                setup.defenderEntityId,
                WarRuntimeContext.declarations(level).all(),
                level.getGameTime(),
                0L,                                  // no peace cooldown for this assertion
                10,                                  // generous defender daily limit
                WarRuntimeContext.demilitarizations(level),
                WarRuntimeContext.cooldowns(level));
        helper.assertTrue(!policyResult.valid(),
                "Expected immediate redeclaration to be denied while immunity is active");
        helper.assertTrue("defender_lost_territory_immunity".equals(policyResult.reason()),
                "Expected the denial reason to be defender_lost_territory_immunity, got: " + policyResult.reason());
        helper.succeed();
    }

    // ------------------------------------------------------------------------

    private record Setup(UUID warId,
                         UUID attackerEntityId,
                         UUID defenderEntityId,
                         RecruitsClaim attackerClaim,
                         RecruitsClaim defenderClaim,
                         BlockPos attackerClaimPos,
                         BlockPos defenderClaimPos) {
    }

    private static Setup setupAttackerDefender(GameTestHelper helper, ServerLevel level, String tag) {
        String attackerTeam = ATTACKER_TEAM_ID + "_" + tag;
        String defenderTeam = DEFENDER_TEAM_ID + "_" + tag;
        UUID attackerLeader = uniqueLeaderUuid(ATTACKER_LEADER_UUID, tag, true);
        UUID defenderLeader = uniqueLeaderUuid(DEFENDER_LEADER_UUID, tag, false);

        Player attackerLeaderPlayer = BannerModDedicatedServerGameTestSupport
                .createFakeServerPlayer(level, attackerLeader, "war-attacker-" + tag);
        Player defenderLeaderPlayer = BannerModDedicatedServerGameTestSupport
                .createFakeServerPlayer(level, defenderLeader, "war-defender-" + tag);

        BannerModDedicatedServerGameTestSupport.ensureFaction(level, attackerTeam, attackerLeader,
                attackerLeaderPlayer.getName().getString());
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, defenderTeam, defenderLeader,
                defenderLeaderPlayer.getName().getString());
        BannerModDedicatedServerGameTestSupport.joinTeam(level, attackerTeam, attackerLeaderPlayer);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, defenderTeam, defenderLeaderPlayer);

        BlockPos attackerClaimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos defenderClaimPos = helper.absolutePos(new BlockPos(34, 2, 34));
        RecruitsClaim attackerClaim = BannerModDedicatedServerGameTestSupport.seedClaim(
                level, attackerClaimPos, attackerTeam, attackerLeader,
                attackerLeaderPlayer.getName().getString());
        RecruitsClaim defenderClaim = BannerModDedicatedServerGameTestSupport.seedClaim(
                level, defenderClaimPos, defenderTeam, defenderLeader,
                defenderLeaderPlayer.getName().getString());

        UUID attackerEntityId = WarRuntimeContext.registry(level).byName(attackerTeam).orElseThrow().id();
        UUID defenderEntityId = WarRuntimeContext.registry(level).byName(defenderTeam).orElseThrow().id();

        WarDeclarationRecord declared = WarRuntimeContext.declarations(level).declareWar(
                attackerEntityId,
                defenderEntityId,
                WarGoalType.WHITE_PEACE,
                "test",
                List.of(),
                List.of(),
                List.of(),
                level.getGameTime(),
                0L
        ).orElseThrow();

        return new Setup(declared.id(), attackerEntityId, defenderEntityId,
                attackerClaim, defenderClaim, attackerClaimPos, defenderClaimPos);
    }

    private static void seedTreasury(ServerLevel level, RecruitsClaim claim, int amount) {
        BannerModTreasuryManager.get(level).depositTaxes(
                claim.getUUID(), claim.getCenter(), null, amount, level.getGameTime());
    }

    private static int treasuryBalanceForEntity(ServerLevel level, UUID politicalEntityId) {
        int total = 0;
        for (RecruitsClaim claim : ClaimEvents.recruitsClaimManager.getAllClaims()) {
            if (politicalEntityId.equals(claim.getOwnerPoliticalEntityId())) {
                total += BannerModTreasuryManager.get(level).getLedger(claim.getUUID()).treasuryBalance();
            }
        }
        return total;
    }

    private static String latestAuditDetail(WarAuditLogSavedData audit, UUID warId) {
        WarAuditEntry latest = null;
        for (WarAuditEntry entry : audit.all()) {
            if (warId.equals(entry.warId())) {
                latest = entry;
            }
        }
        return latest == null ? "" : latest.detail();
    }

    private static int auditCount(WarAuditLogSavedData audit, UUID warId, String type) {
        int count = 0;
        for (WarAuditEntry entry : audit.all()) {
            if (warId.equals(entry.warId()) && type.equals(entry.type())) {
                count++;
            }
        }
        return count;
    }

    private static WarAuditEntry latestAuditOfType(WarAuditLogSavedData audit, UUID warId, String type) {
        WarAuditEntry latest = null;
        for (WarAuditEntry entry : audit.all()) {
            if (warId.equals(entry.warId()) && type.equals(entry.type())) {
                latest = entry;
            }
        }
        return latest;
    }

    /**
     * Make per-test leader UUIDs unique so multiple gametests in the same JVM cannot share
     * a faction or leader and accidentally cross-contaminate registry/cooldown state.
     */
    private static UUID uniqueLeaderUuid(UUID seed, String tag, boolean attacker) {
        long hash = ((long) tag.hashCode() << 1) ^ (attacker ? 1L : 0L);
        return new UUID(seed.getMostSignificantBits() ^ hash,
                seed.getLeastSignificantBits() ^ (hash * 31));
    }
}
