package com.talhanation.bannermod.society;

import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodsSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementMarketState;
import com.talhanation.bannermod.settlement.BannerModSettlementProjectCandidateSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentAssignmentState;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentMode;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRole;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRuntimeRoleSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentScheduleSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentScheduleWindowSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentServiceContract;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementStockpileSummary;
import com.talhanation.bannermod.settlement.BannerModSettlementSupplySignalState;
import com.talhanation.bannermod.settlement.BannerModSettlementTradeRouteHandoffSeed;
import com.talhanation.bannermod.settlement.goal.ResidentGoalContext;
import com.talhanation.bannermod.settlement.goal.impl.RestResidentGoal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcSocietyPhaseTwoIntentScorerTest {
    private static final long ACTIVE_TIME = 6000L;
    private static final long REST_TIME = 15000L;

    @Test
    void fearWeightedHideOutranksRestDuringActivePhase() {
        ResidentGoalContext ctx = context(
                villagerResident(),
                ACTIVE_TIME,
                null,
                NpcSocietyProfile.createDefault(uuid("00000000-0000-0000-0000-00000000a001"), ACTIVE_TIME)
                        .withNeedState(5, 5, 5, 12, ACTIVE_TIME)
                        .withSocialState(50, 42, 0, 0, 50, ACTIVE_TIME)
        );

        int hide = NpcSocietyPhaseTwoIntentScorer.scoreIntent(ctx, NpcIntent.HIDE);
        int rest = NpcSocietyPhaseTwoIntentScorer.scoreIntent(ctx, NpcIntent.REST);

        assertEquals(80, hide, "hide score should reflect danger + fear weighting exactly");
        assertEquals(7, rest, "rest should only receive the small residual fear term during active phase");
        assertTrue(hide > rest, "high fear during active phase must push villagers toward hiding over resting");
    }

    @Test
    void restGoalBasePriorityIsOnlyAppliedInRestPhase() {
        NpcSocietyProfile profile = NpcSocietyProfile.createDefault(uuid("00000000-0000-0000-0000-00000000a002"), ACTIVE_TIME)
                .withNeedState(5, 5, 5, 12, ACTIVE_TIME)
                .withSocialState(50, 42, 0, 0, 50, ACTIVE_TIME);
        RestResidentGoal goal = new RestResidentGoal();

        int activePriority = goal.computePriority(context(villagerResident(), ACTIVE_TIME, null, profile));
        int restPriority = goal.computePriority(context(villagerResident(), REST_TIME, null, profile));

        assertEquals(7, activePriority, "rest goal should not keep an always-on high base priority during active time");
        assertEquals(94, restPriority, "rest phase should still receive the intended overnight base priority");
        assertTrue(restPriority > activePriority, "rest priority must jump sharply once the resident is in rest phase");
    }

    @Test
    void adolescentSocialiseGetsExactBonusWeight() {
        UUID adultId = uuid("00000000-0000-0000-0000-00000000a003");
        UUID adolescentId = uuid("00000000-0000-0000-0000-00000000a004");
        ResidentGoalContext adult = context(
                villagerResident(),
                ACTIVE_TIME,
                null,
                NpcSocietyProfile.createSeeded(adultId, NpcLifeStage.ADULT, NpcSex.MALE, ACTIVE_TIME)
                        .withNeedState(10, 10, 60, 0, ACTIVE_TIME)
                        .withSocialState(50, 0, 0, 0, 50, ACTIVE_TIME)
        );
        ResidentGoalContext adolescent = context(
                villagerResident(adolescentId),
                ACTIVE_TIME,
                null,
                NpcSocietyProfile.createSeeded(adolescentId, NpcLifeStage.ADOLESCENT, NpcSex.MALE, ACTIVE_TIME)
                        .withNeedState(10, 10, 60, 0, ACTIVE_TIME)
                        .withSocialState(50, 0, 0, 0, 50, ACTIVE_TIME)
        );

        int adultScore = NpcSocietyPhaseTwoIntentScorer.scoreIntent(adult, NpcIntent.SOCIALISE);
        int adolescentScore = NpcSocietyPhaseTwoIntentScorer.scoreIntent(adolescent, NpcIntent.SOCIALISE);

        assertEquals(74, adultScore);
        assertEquals(82, adolescentScore);
        assertEquals(8, adolescentScore - adultScore,
                "adolescent socialise weight should add the exact +8 bonus defined by the scorer");
    }

    @Test
    void foodAccessEnablesEatAndSevereHungerBeatsWork() {
        BannerModSettlementResidentRecord worker = workerResident();
        NpcSocietyProfile profile = NpcSocietyProfile.createDefault(uuid("00000000-0000-0000-0000-00000000a005"), ACTIVE_TIME)
                .withNeedState(92, 10, 10, 10, ACTIVE_TIME)
                .withSocialState(50, 0, 0, 0, 50, ACTIVE_TIME);

        ResidentGoalContext noMarket = context(worker, ACTIVE_TIME, settlementWithOpenMarkets(worker, 0), profile);
        ResidentGoalContext openMarket = context(worker, ACTIVE_TIME, settlementWithOpenMarkets(worker, 1), profile);

        int eatWithoutMarket = NpcSocietyPhaseTwoIntentScorer.scoreIntent(noMarket, NpcIntent.EAT);
        int eatWithMarket = NpcSocietyPhaseTwoIntentScorer.scoreIntent(openMarket, NpcIntent.EAT);
        int workWithMarket = NpcSocietyPhaseTwoIntentScorer.scoreIntent(openMarket, NpcIntent.WORK);

        assertEquals(0, eatWithoutMarket, "eat should stay unavailable when the resident has no home and no market access");
        assertEquals(114, eatWithMarket, "severe hunger with food access should produce the exact eat pressure from the scorer");
        assertEquals(39, workWithMarket, "the same context should heavily penalize work under severe hunger");
        assertTrue(eatWithMarket > workWithMarket, "severe hunger should out-rank work once food is reachable");
    }

    @Test
    void governorAngerWeightLetsDefendBeatHide() {
        ResidentGoalContext ctx = context(
                governorResident(),
                ACTIVE_TIME,
                null,
                NpcSocietyProfile.createDefault(uuid("00000000-0000-0000-0000-00000000a006"), ACTIVE_TIME)
                        .withNeedState(10, 10, 10, 40, ACTIVE_TIME)
                        .withSocialState(50, 30, 80, 0, 60, ACTIVE_TIME)
        );

        int hide = NpcSocietyPhaseTwoIntentScorer.scoreIntent(ctx, NpcIntent.HIDE);
        int defend = NpcSocietyPhaseTwoIntentScorer.scoreIntent(ctx, NpcIntent.DEFEND);

        assertEquals(0, hide, "hide should be suppressed entirely when a defender's anger clearly exceeds fear");
        assertEquals(120, defend, "defend should clamp after the anger and loyalty weights push it over the cap");
        assertTrue(defend > hide, "armed governor recruits should defend rather than hide when anger dominates fear");
    }

    private static ResidentGoalContext context(BannerModSettlementResidentRecord resident,
                                               long gameTime,
                                               BannerModSettlementSnapshot settlement,
                                               NpcSocietyProfile profile) {
        return new ResidentGoalContext(resident, settlement, gameTime, profile);
    }

    private static BannerModSettlementResidentRecord villagerResident() {
        return villagerResident(uuid("00000000-0000-0000-0000-00000000b001"));
    }

    private static BannerModSettlementResidentRecord villagerResident(UUID residentId) {
        return new BannerModSettlementResidentRecord(
                residentId,
                BannerModSettlementResidentRole.VILLAGER,
                BannerModSettlementResidentScheduleSeed.SETTLEMENT_IDLE,
                BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX,
                BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE,
                BannerModSettlementResidentServiceContract.notServiceActor(),
                BannerModSettlementResidentMode.SETTLEMENT_RESIDENT,
                null,
                null,
                null,
                BannerModSettlementResidentAssignmentState.NOT_APPLICABLE
        );
    }

    private static BannerModSettlementResidentRecord workerResident() {
        return new BannerModSettlementResidentRecord(
                uuid("00000000-0000-0000-0000-00000000b002"),
                BannerModSettlementResidentRole.CONTROLLED_WORKER,
                BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK,
                BannerModSettlementResidentScheduleWindowSeed.LABOR_DAY,
                BannerModSettlementResidentRuntimeRoleSeed.LOCAL_LABOR,
                BannerModSettlementResidentServiceContract.notServiceActor(),
                BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER,
                uuid("00000000-0000-0000-0000-00000000b012"),
                "team-test",
                uuid("00000000-0000-0000-0000-00000000b022"),
                BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING
        );
    }

    private static BannerModSettlementResidentRecord governorResident() {
        return new BannerModSettlementResidentRecord(
                uuid("00000000-0000-0000-0000-00000000b003"),
                BannerModSettlementResidentRole.GOVERNOR_RECRUIT,
                BannerModSettlementResidentScheduleSeed.SETTLEMENT_IDLE,
                BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX,
                BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE,
                BannerModSettlementResidentServiceContract.notServiceActor(),
                BannerModSettlementResidentMode.SETTLEMENT_RESIDENT,
                null,
                null,
                null,
                BannerModSettlementResidentAssignmentState.NOT_APPLICABLE
        );
    }

    private static BannerModSettlementSnapshot settlementWithOpenMarkets(BannerModSettlementResidentRecord resident, int openMarketCount) {
        return new BannerModSettlementSnapshot(
                uuid("00000000-0000-0000-0000-00000000c001"),
                0,
                0,
                null,
                ACTIVE_TIME,
                4,
                4,
                1,
                1,
                0,
                0,
                BannerModSettlementStockpileSummary.empty(),
                new BannerModSettlementMarketState(Math.max(1, openMarketCount), openMarketCount, 0, 0, 0, 0, List.of(), List.of()),
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                List.of(resident),
                List.of()
        );
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }
}
