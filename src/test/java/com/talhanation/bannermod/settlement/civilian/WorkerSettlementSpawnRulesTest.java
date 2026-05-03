package com.talhanation.bannermod.settlement.civilian;

import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules.ClaimGrowthConfig;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules.Decision;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules.DenialReason;
import com.talhanation.bannermod.settlement.civilian.WorkerSettlementSpawnRules.WorkerProfession;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkerSettlementSpawnRulesTest {

    private static final List<WorkerProfession> FARMER_SMITH = List.of(WorkerProfession.FARMER, WorkerProfession.MINER);

    private static ClaimGrowthConfig growth(List<WorkerProfession> allowed) {
        return new ClaimGrowthConfig(true, 0L, 16, allowed);
    }

    private static Map<WorkerProfession, Integer> counts(WorkerProfession p, int n) {
        Map<WorkerProfession, Integer> map = new EnumMap<>(WorkerProfession.class);
        map.put(p, n);
        return map;
    }

    @Test
    void deficitPicksUnstaffedProfessionEvenAtOddTotal() {
        // 1 FARMER, 0 MINER. Old round-robin (currentWorkerCount=1, list size 2) would pick MINER too,
        // but for currentWorkerCount=2 (even) it would have picked FARMER again — deficit must always pick MINER here.
        Decision afterOne = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 1, Long.MAX_VALUE, growth(FARMER_SMITH),
                counts(WorkerProfession.FARMER, 1));
        assertEquals(WorkerProfession.MINER, afterOne.profession());

        Map<WorkerProfession, Integer> twoFarmers = counts(WorkerProfession.FARMER, 2);
        Decision afterTwoFarmers = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 2, Long.MAX_VALUE, growth(FARMER_SMITH),
                twoFarmers);
        assertEquals(WorkerProfession.MINER, afterTwoFarmers.profession(),
                "deficit selection must refill the missing MINER even with 2 FARMERs already (round-robin would pick FARMER)");
    }

    @Test
    void deficitTiebreaksByDeclarationOrder() {
        // Both at 0 — must pick FARMER (first in list).
        Decision empty = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 0, Long.MAX_VALUE, growth(FARMER_SMITH),
                Map.of());
        assertEquals(WorkerProfession.FARMER, empty.profession());

        // Both at 3 — still picks FARMER (first in list).
        Map<WorkerProfession, Integer> tied = new EnumMap<>(WorkerProfession.class);
        tied.put(WorkerProfession.FARMER, 3);
        tied.put(WorkerProfession.MINER, 3);
        Decision tiedDecision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 6, Long.MAX_VALUE, growth(FARMER_SMITH), tied);
        assertEquals(WorkerProfession.FARMER, tiedDecision.profession());
    }

    @Test
    void capReachedDeniesBeforeProfessionPick() {
        ClaimGrowthConfig capOne = new ClaimGrowthConfig(true, 0L, 1, FARMER_SMITH);
        Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 1, Long.MAX_VALUE, capOne,
                counts(WorkerProfession.FARMER, 1));
        assertFalse(decision.allowed());
        assertEquals(DenialReason.WORKER_CAP_REACHED, decision.denialReason());
    }

    @Test
    void emptyAllowedProfessionsIsRejected() {
        Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 0, Long.MAX_VALUE,
                new ClaimGrowthConfig(true, 0L, 16, List.of()), Map.of());
        assertFalse(decision.allowed());
        assertEquals(DenialReason.NO_ALLOWED_PROFESSIONS, decision.denialReason());
    }

    @Test
    void nullOccupancyMapDefaultsToZeroEverywhere() {
        // With no occupancy snapshot, every count is 0 — first-in-list wins.
        Decision decision = WorkerSettlementSpawnRules.evaluateClaimWorkerGrowth(
                BannerModSettlementBinding.Status.FRIENDLY_CLAIM, 0, Long.MAX_VALUE, growth(FARMER_SMITH), null);
        assertTrue(decision.allowed());
        assertEquals(WorkerProfession.FARMER, decision.profession());
    }
}
