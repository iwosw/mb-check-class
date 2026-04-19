package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.settlement.goal.ResidentTask;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.household.GoHomeResidentGoal;
import com.talhanation.bannermod.settlement.job.JobExecutionContext;
import com.talhanation.bannermod.settlement.job.JobExecutionResult;
import com.talhanation.bannermod.settlement.job.JobHandler;
import com.talhanation.bannermod.settlement.job.JobHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModSettlementOrchestratorTest {

    private static final UUID CLAIM = UUID.fromString("00000000-0000-0000-0000-0000000000f1");
    private static final UUID RESIDENT = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID HOME = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID MARKET = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final long NIGHT_TICK = 15000L;

    @Test
    void tickSnapshotComposesGrowthProjectsHomesSellerDispatchGoalsAndJobs() {
        RecordingJobHandler handler = new RecordingJobHandler();
        JobHandlerRegistry registry = new JobHandlerRegistry();
        registry.register(handler);
        BannerModSettlementOrchestrator.LevelRuntimeState state = BannerModSettlementOrchestrator.detachedStateForTests(registry);
        BannerModSettlementSnapshot snapshot = settlementSnapshot();

        BannerModSettlementOrchestrator.tickSnapshot(state, snapshot, null, NIGHT_TICK);

        assertEquals(HOME, state.homeRuntime.homeFor(RESIDENT).orElseThrow().homeBuildingUuid());
        assertTrue(state.sellerRuntime.phase(RESIDENT).isPresent(), "ready seller seed should start a live dispatch");

        List<PendingProject> queuedProjects = state.projectRuntime.snapshot(CLAIM);
        assertFalse(queuedProjects.isEmpty(), "growth scoring should feed the project runtime queue");
        assertEquals(BannerModSettlementBuildingProfileSeed.GENERAL, queuedProjects.get(0).profileSeed());

        Optional<ResidentTask> task = state.goalScheduler.currentTask(RESIDENT);
        assertTrue(task.isPresent(), "resident should receive a scheduled task");
        assertEquals(GoHomeResidentGoal.ID, task.get().goalId());

        assertEquals(1, handler.invocationCount);
        assertEquals(RESIDENT, handler.lastResidentUuid);
        assertEquals(MARKET, handler.lastWorkplaceUuid);
    }

    @Test
    void tickSnapshotCancelsStaleLiveDispatchesAndRebindsSellerToCurrentSeed() {
        BannerModSettlementOrchestrator.LevelRuntimeState state = BannerModSettlementOrchestrator.detachedStateForTests(JobHandlerRegistry.defaults());

        BannerModSettlementOrchestrator.tickSnapshot(state, settlementSnapshot(), null, NIGHT_TICK);

        UUID otherMarket = UUID.fromString("00000000-0000-0000-0000-0000000000c2");
        BannerModSettlementMarketState reboundMarketState = new BannerModSettlementMarketState(
                1,
                1,
                16,
                8,
                1,
                1,
                List.of(new BannerModSettlementMarketRecord(otherMarket, "Other Market", true, 16, 8)),
                List.of(new BannerModSettlementSellerDispatchRecord(
                        RESIDENT,
                        otherMarket,
                        "Other Market",
                        BannerModSettlementSellerDispatchState.READY
                ))
        );
        BannerModSettlementSnapshot reboundSnapshot = new BannerModSettlementSnapshot(
                CLAIM,
                0,
                0,
                "teamA",
                NIGHT_TICK + 1,
                1,
                1,
                1,
                1,
                1,
                0,
                BannerModSettlementStockpileSummary.empty(),
                reboundMarketState,
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                settlementSnapshot().residents(),
                settlementSnapshot().buildings()
        );

        BannerModSettlementOrchestrator.tickSnapshot(state, reboundSnapshot, null, NIGHT_TICK + 1);

        assertEquals(otherMarket, state.sellerRuntime.phase(RESIDENT).orElseThrow().marketRecordUuid());
        assertEquals(com.talhanation.bannermod.settlement.dispatch.SellerPhase.MOVING_TO_STALL, state.sellerRuntime.phase(RESIDENT).orElseThrow().phase());
    }

    private static BannerModSettlementSnapshot settlementSnapshot() {
        BannerModSettlementResidentServiceContract serviceContract = new BannerModSettlementResidentServiceContract(
                BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE,
                MARKET,
                "market_area"
        );
        BannerModSettlementResidentRecord resident = new BannerModSettlementResidentRecord(
                RESIDENT,
                BannerModSettlementResidentRole.CONTROLLED_WORKER,
                BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK,
                BannerModSettlementResidentRuntimeRoleSeed.LOCAL_LABOR,
                serviceContract,
                BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER,
                UUID.fromString("00000000-0000-0000-0000-0000000000d1"),
                "teamA",
                MARKET,
                BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING
        );

        BannerModSettlementBuildingRecord home = new BannerModSettlementBuildingRecord(
                HOME,
                "house",
                BlockPos.ZERO,
                null,
                null,
                2,
                0,
                0,
                List.of()
        );
        BannerModSettlementBuildingRecord market = new BannerModSettlementBuildingRecord(
                MARKET,
                "market_area",
                new BlockPos(4, 64, 4),
                null,
                null,
                0,
                1,
                1,
                List.of()
        );

        BannerModSettlementMarketState marketState = new BannerModSettlementMarketState(
                1,
                1,
                16,
                8,
                1,
                1,
                List.of(new BannerModSettlementMarketRecord(MARKET, "Market", true, 16, 8)),
                List.of(new BannerModSettlementSellerDispatchRecord(
                        RESIDENT,
                        MARKET,
                        "Market",
                        BannerModSettlementSellerDispatchState.READY
                ))
        );

        return new BannerModSettlementSnapshot(
                CLAIM,
                0,
                0,
                "teamA",
                NIGHT_TICK,
                1,
                1,
                1,
                1,
                1,
                0,
                BannerModSettlementStockpileSummary.empty(),
                marketState,
                BannerModSettlementDesiredGoodsSeed.empty(),
                BannerModSettlementProjectCandidateSeed.empty(),
                BannerModSettlementTradeRouteHandoffSeed.empty(),
                BannerModSettlementSupplySignalState.empty(),
                List.of(resident),
                List.of(home, market)
        );
    }

    private static final class RecordingJobHandler implements JobHandler {
        int invocationCount;
        UUID lastResidentUuid;
        UUID lastWorkplaceUuid;

        @Override
        public ResourceLocation id() {
            return new ResourceLocation("bannermod", "test_orchestrator_job");
        }

        @Override
        public BannerModSettlementJobHandlerSeed handles() {
            return BannerModSettlementJobHandlerSeed.LOCAL_BUILDING_LABOR;
        }

        @Override
        public boolean canHandle(JobExecutionContext ctx) {
            return true;
        }

        @Override
        public JobExecutionResult runOneStep(JobExecutionContext ctx) {
            this.invocationCount++;
            this.lastResidentUuid = ctx.resident().residentUuid();
            this.lastWorkplaceUuid = ctx.workplace().orElse(null);
            return JobExecutionResult.COMPLETED;
        }
    }
}
