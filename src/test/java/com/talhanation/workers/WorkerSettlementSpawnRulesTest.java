package com.talhanation.workers;

import com.talhanation.bannermod.settlement.BannerModSettlementBinding;
import com.talhanation.workers.settlement.WorkerSettlementSpawnRules;
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
}
