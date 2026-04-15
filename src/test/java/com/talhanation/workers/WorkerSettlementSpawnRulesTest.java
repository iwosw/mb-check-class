package com.talhanation.workers;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerSettlementSpawnRulesTest {

    private static final BannerModSettlementBinding.Binding FRIENDLY = new BannerModSettlementBinding.Binding(
            BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
            "settlement",
            "settlement"
    );
    private static final BannerModSettlementBinding.Binding HOSTILE = new BannerModSettlementBinding.Binding(
            BannerModSettlementBinding.Status.HOSTILE_CLAIM,
            "settlement",
            "other"
    );
    private static final BannerModSettlementBinding.Binding UNCLAIMED = new BannerModSettlementBinding.Binding(
            BannerModSettlementBinding.Status.UNCLAIMED,
            "settlement",
            null
    );
    private static final BannerModSettlementBinding.Binding DEGRADED = new BannerModSettlementBinding.Binding(
            BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
            "settlement",
            "other"
    );

    @Test
    void friendlyClaimAllowsClaimWorkerGrowthWhenCooldownIsSatisfied() {
        WorkerSettlementSpawnRules.ClaimGrowthConfig config = new WorkerSettlementSpawnRules.ClaimGrowthConfig(
                true,
                200L,
                4,
                List.of(WorkerSettlementSpawnRules.WorkerProfession.FARMER)
        );

        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                0,
                200L,
                config
        );

        assertTrue(decision.allowed());
        assertEquals(200L, decision.requiredCooldownTicks());
        assertEquals(WorkerSettlementSpawnRules.WorkerProfession.FARMER, decision.professionOptional().orElseThrow());
    }

    @Test
    void hostileDegradedAndUnclaimedClaimsDenyClaimWorkerGrowth() {
        WorkerSettlementSpawnRules.ClaimGrowthConfig config = new WorkerSettlementSpawnRules.ClaimGrowthConfig(
                true,
                200L,
                4,
                List.of(WorkerSettlementSpawnRules.WorkerProfession.FARMER)
        );

        WorkerSettlementSpawnRules.Decision hostileDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.HOSTILE_CLAIM,
                0,
                200L,
                config
        );
        WorkerSettlementSpawnRules.Decision degradedDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
                0,
                200L,
                config
        );
        WorkerSettlementSpawnRules.Decision unclaimedDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.UNCLAIMED,
                0,
                200L,
                config
        );

        assertFalse(hostileDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.NOT_FRIENDLY_CLAIM, hostileDecision.denialReasonOptional().orElseThrow());
        assertFalse(degradedDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.NOT_FRIENDLY_CLAIM, degradedDecision.denialReasonOptional().orElseThrow());
        assertFalse(unclaimedDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.NOT_FRIENDLY_CLAIM, unclaimedDecision.denialReasonOptional().orElseThrow());
    }

    @Test
    void claimWorkerGrowthCooldownRequirementScalesWithCurrentWorkers() {
        WorkerSettlementSpawnRules.ClaimGrowthConfig config = new WorkerSettlementSpawnRules.ClaimGrowthConfig(
                true,
                200L,
                8,
                List.of(WorkerSettlementSpawnRules.WorkerProfession.FARMER)
        );

        WorkerSettlementSpawnRules.Decision deniedDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                2,
                599L,
                config
        );
        WorkerSettlementSpawnRules.Decision allowedDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                2,
                600L,
                config
        );

        assertEquals(600L, deniedDecision.requiredCooldownTicks());
        assertFalse(deniedDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.COOLDOWN_ACTIVE, deniedDecision.denialReasonOptional().orElseThrow());
        assertEquals(600L, allowedDecision.requiredCooldownTicks());
        assertTrue(allowedDecision.allowed());
    }

    @Test
    void friendlyClaimAllowsWorkerBirthWhenQuotaAndCooldownAllowIt() {
        WorkerSettlementSpawnRules.RuleConfig config = new WorkerSettlementSpawnRules.RuleConfig(
                true,
                4,
                3,
                List.of(WorkerSettlementSpawnRules.WorkerProfession.FARMER)
        );

        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateBirth(
                FRIENDLY,
                6,
                1,
                false,
                config
        );

        assertTrue(decision.allowed());
        assertEquals(WorkerSettlementSpawnRules.WorkerProfession.FARMER, decision.professionOptional().orElseThrow());
        assertFalse(decision.denialReasonOptional().isPresent());
    }

    @Test
    void hostileOrUnclaimedSettlementDeniesBirthAndAutonomousSpawn() {
        WorkerSettlementSpawnRules.RuleConfig config = new WorkerSettlementSpawnRules.RuleConfig(
                true,
                4,
                3,
                List.of(WorkerSettlementSpawnRules.WorkerProfession.FARMER)
        );

        WorkerSettlementSpawnRules.Decision hostileBirth = WorkerSettlementSpawnRules.evaluateBirth(HOSTILE, 6, 0, false, config);
        WorkerSettlementSpawnRules.Decision unclaimedSpawn = WorkerSettlementSpawnRules.evaluateSettlementSpawn(UNCLAIMED, 6, 0, false, config);

        assertFalse(hostileBirth.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.NOT_FRIENDLY_CLAIM, hostileBirth.denialReasonOptional().orElseThrow());
        assertFalse(unclaimedSpawn.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.NOT_FRIENDLY_CLAIM, unclaimedSpawn.denialReasonOptional().orElseThrow());
    }

    @Test
    void quotaExhaustionAndCooldownDenyWithoutRandomness() {
        WorkerSettlementSpawnRules.RuleConfig config = new WorkerSettlementSpawnRules.RuleConfig(
                true,
                4,
                2,
                List.of(
                        WorkerSettlementSpawnRules.WorkerProfession.FARMER,
                        WorkerSettlementSpawnRules.WorkerProfession.MINER
                )
        );

        WorkerSettlementSpawnRules.Decision quotaDecision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(FRIENDLY, 6, 2, false, config);
        WorkerSettlementSpawnRules.Decision cooldownDecision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(FRIENDLY, 6, 1, true, config);

        assertFalse(quotaDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.WORKER_CAP_REACHED, quotaDecision.denialReasonOptional().orElseThrow());
        assertFalse(cooldownDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.COOLDOWN_ACTIVE, cooldownDecision.denialReasonOptional().orElseThrow());
    }

    @Test
    void configDerivedDefaultsAllowFriendlyBirthAndBoundedSpawn() {
        WorkerSettlementSpawnRules.Decision birthDecision = WorkerSettlementSpawnRules.evaluateBirth(
                FRIENDLY,
                8,
                1,
                false,
                WorkersServerConfig.workerBirthRuleConfig()
        );
        WorkerSettlementSpawnRules.Decision spawnDecision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(
                FRIENDLY,
                8,
                1,
                false,
                WorkersServerConfig.workerSettlementSpawnRuleConfig()
        );

        assertTrue(birthDecision.allowed());
        assertTrue(spawnDecision.allowed());
        assertTrue(birthDecision.professionOptional().isPresent());
        assertTrue(spawnDecision.professionOptional().isPresent());
    }

    @Test
    void disablingBirthOrSpawnToggleForcesDenyEvenWhenClaimAndQuotaFit() {
        WorkerSettlementSpawnRules.RuleConfig disabledBirth = WorkersServerConfig.workerBirthRuleConfig().withEnabled(false);
        WorkerSettlementSpawnRules.RuleConfig disabledSpawn = WorkersServerConfig.workerSettlementSpawnRuleConfig().withEnabled(false);

        WorkerSettlementSpawnRules.Decision birthDecision = WorkerSettlementSpawnRules.evaluateBirth(FRIENDLY, 8, 1, false, disabledBirth);
        WorkerSettlementSpawnRules.Decision spawnDecision = WorkerSettlementSpawnRules.evaluateSettlementSpawn(FRIENDLY, 8, 1, false, disabledSpawn);

        assertFalse(birthDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.FEATURE_DISABLED, birthDecision.denialReasonOptional().orElseThrow());
        assertFalse(spawnDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.FEATURE_DISABLED, spawnDecision.denialReasonOptional().orElseThrow());
    }

    @Test
    void configDerivedClaimGrowthDefaultsAllowFriendlyGrowthBelowCap() {
        WorkerSettlementSpawnRules.ClaimGrowthConfig config = WorkersServerConfig.claimWorkerGrowthConfig();

        WorkerSettlementSpawnRules.Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1,
                config.requiredCooldownTicks(1),
                config
        );

        assertTrue(decision.allowed());
        assertTrue(decision.professionOptional().isPresent());
    }

    @Test
    void disablingClaimGrowthOrReachingCapDeniesFriendlyClaimGrowth() {
        WorkerSettlementSpawnRules.ClaimGrowthConfig enabledConfig = WorkersServerConfig.claimWorkerGrowthConfig();
        WorkerSettlementSpawnRules.ClaimGrowthConfig disabledConfig = enabledConfig.withEnabled(false);

        WorkerSettlementSpawnRules.Decision disabledDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                1,
                enabledConfig.requiredCooldownTicks(1),
                disabledConfig
        );
        WorkerSettlementSpawnRules.Decision capDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                enabledConfig.workerCap(),
                enabledConfig.requiredCooldownTicks(enabledConfig.workerCap()),
                enabledConfig
        );

        assertFalse(disabledDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.FEATURE_DISABLED, disabledDecision.denialReasonOptional().orElseThrow());
        assertFalse(capDecision.allowed());
        assertEquals(WorkerSettlementSpawnRules.DenialReason.WORKER_CAP_REACHED, capDecision.denialReasonOptional().orElseThrow());
    }

}
