package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntime;
import com.talhanation.bannermod.settlement.dispatch.SellerPhaseRecord;
import com.talhanation.bannermod.settlement.goal.BannerModResidentGoalScheduler;
import com.talhanation.bannermod.settlement.goal.ResidentGoalContext;
import com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthContext;
import com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManager;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentAdvisor;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntime;
import com.talhanation.bannermod.settlement.household.HomePreference;
import com.talhanation.bannermod.settlement.job.JobExecutionContext;
import com.talhanation.bannermod.settlement.job.JobHandlerRegistry;
import com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridge;
import com.talhanation.bannermod.settlement.project.BannerModSettlementProjectRuntime;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class BannerModSettlementOrchestrator {
    private static final int MAX_GROWTH_QUEUE_SIZE = 3;
    private static final WeakHashMap<ServerLevel, LevelRuntimeState> PER_LEVEL = new WeakHashMap<>();

    private BannerModSettlementOrchestrator() {
    }

    public static void tick(ServerLevel level,
                            BannerModSettlementManager settlementManager,
                            @Nullable BannerModGovernorManager governorManager) {
        if (level == null || settlementManager == null) {
            return;
        }
        LevelRuntimeState state = runtimeState(level);
        long gameTime = level.getGameTime();
        for (BannerModSettlementSnapshot snapshot : settlementManager.getAllSnapshots()) {
            if (snapshot == null) {
                continue;
            }
            BannerModGovernorSnapshot governorSnapshot = governorManager == null
                    ? null
                    : governorManager.getSnapshot(snapshot.claimUuid());
            tickSnapshot(state, snapshot, governorSnapshot, gameTime);
        }
    }

    static LevelRuntimeState detachedStateForTests(JobHandlerRegistry jobHandlerRegistry) {
        return LevelRuntimeState.create(BannerModSettlementProjectRuntime.detachedForTests(), jobHandlerRegistry);
    }

    static void tickSnapshot(LevelRuntimeState state,
                             BannerModSettlementSnapshot snapshot,
                             @Nullable BannerModGovernorSnapshot governorSnapshot,
                             long gameTime) {
        if (state == null || snapshot == null) {
            return;
        }

        BannerModSettlementGrowthContext growthContext = BannerModSettlementGrowthContext.fromSnapshot(
                snapshot,
                governorSnapshot,
                gameTime
        );
        List<PendingProject> growthQueue = BannerModSettlementGrowthManager.evaluateGrowthQueue(
                growthContext,
                MAX_GROWTH_QUEUE_SIZE
        );
        state.projectRuntime.tickClaim(
                null,
                snapshot.claimUuid(),
                growthQueue,
                new BannerModBuildAreaProjectBridge.NoopBuildAreaResolver(),
                gameTime
        );

        assignHomes(state.homeRuntime, snapshot, gameTime);
        state.marketStateSupplier.set(snapshot.marketState());
        tickSellerDispatches(state.sellerRuntime, snapshot.marketState(), gameTime);

        for (BannerModSettlementResidentRecord resident : snapshot.residents()) {
            if (resident == null || resident.residentUuid() == null) {
                continue;
            }
            state.goalScheduler.tick(new ResidentGoalContext(resident, snapshot, gameTime));
            runResidentJobStep(state.jobHandlerRegistry, resident, gameTime);
        }
    }

    private static synchronized LevelRuntimeState runtimeState(ServerLevel level) {
        return PER_LEVEL.computeIfAbsent(level,
                ignored -> LevelRuntimeState.create(BannerModSettlementProjectRuntime.forServer(level), JobHandlerRegistry.defaults()));
    }

    private static void assignHomes(BannerModHomeAssignmentRuntime homeRuntime,
                                    BannerModSettlementSnapshot snapshot,
                                    long gameTime) {
        for (BannerModSettlementResidentRecord resident : snapshot.residents()) {
            if (resident == null || resident.residentUuid() == null || homeRuntime.homeFor(resident.residentUuid()).isPresent()) {
                continue;
            }
            BannerModHomeAssignmentAdvisor.pickHomeBuilding(resident.residentUuid(), snapshot, homeRuntime)
                    .ifPresent(homeBuildingUuid -> homeRuntime.assign(
                            resident.residentUuid(),
                            homeBuildingUuid,
                            HomePreference.ASSIGNED,
                            gameTime
                    ));
        }
    }

    private static void tickSellerDispatches(BannerModSellerDispatchRuntime sellerRuntime,
                                             BannerModSettlementMarketState marketState,
                                             long gameTime) {
        Set<UUID> openMarkets = new HashSet<>();
        for (BannerModSettlementMarketRecord market : marketState.markets()) {
            if (market != null && market.open() && market.buildingUuid() != null) {
                openMarkets.add(market.buildingUuid());
            }
        }

        for (SellerPhaseRecord dispatch : sellerRuntime.activeDispatches()) {
            if (dispatch != null && dispatch.marketRecordUuid() != null && !openMarkets.contains(dispatch.marketRecordUuid())) {
                sellerRuntime.forceMarketClose(dispatch.marketRecordUuid(), gameTime);
            }
        }

        for (BannerModSettlementSellerDispatchRecord seed : marketState.sellerDispatches()) {
            if (seed == null
                    || seed.dispatchState() != BannerModSettlementSellerDispatchState.READY
                    || seed.residentUuid() == null
                    || seed.marketUuid() == null
                    || !openMarkets.contains(seed.marketUuid())
                    || sellerRuntime.isActive(seed.residentUuid())) {
                continue;
            }
            try {
                sellerRuntime.beginDispatch(seed.residentUuid(), seed.marketUuid(), gameTime);
            } catch (IllegalStateException ignored) {
                // Another claim tick may have started the dispatch already; keep this seam additive.
            }
        }

        for (SellerPhaseRecord dispatch : sellerRuntime.activeDispatches()) {
            if (dispatch != null && dispatch.sellerResidentUuid() != null) {
                sellerRuntime.tickPhase(dispatch.sellerResidentUuid(), gameTime);
            }
        }
    }

    private static void runResidentJobStep(JobHandlerRegistry jobHandlerRegistry,
                                           BannerModSettlementResidentRecord resident,
                                           long gameTime) {
        if (resident.jobDefinition() == null) {
            return;
        }
        JobExecutionContext context = jobContext(resident, gameTime);
        jobHandlerRegistry.lookup(resident.jobDefinition().handlerSeed())
                .filter(handler -> handler.canHandle(context))
                .ifPresent(handler -> handler.runOneStep(context));
    }

    private static JobExecutionContext jobContext(BannerModSettlementResidentRecord resident, long gameTime) {
        UUID workplaceUuid = resident.jobDefinition() == null || resident.jobDefinition().targetBuildingUuid() == null
                ? resident.boundWorkAreaUuid()
                : resident.jobDefinition().targetBuildingUuid();
        return new JobExecutionContext(resident, gameTime, resident.residentUuid(), workplaceUuid);
    }

    static final class LevelRuntimeState {
        final BannerModSettlementProjectRuntime projectRuntime;
        final BannerModHomeAssignmentRuntime homeRuntime;
        final BannerModSellerDispatchRuntime sellerRuntime;
        final MutableMarketStateSupplier marketStateSupplier;
        final BannerModResidentGoalScheduler goalScheduler;
        final JobHandlerRegistry jobHandlerRegistry;

        private LevelRuntimeState(BannerModSettlementProjectRuntime projectRuntime,
                                  BannerModHomeAssignmentRuntime homeRuntime,
                                  BannerModSellerDispatchRuntime sellerRuntime,
                                  MutableMarketStateSupplier marketStateSupplier,
                                  BannerModResidentGoalScheduler goalScheduler,
                                  JobHandlerRegistry jobHandlerRegistry) {
            this.projectRuntime = projectRuntime;
            this.homeRuntime = homeRuntime;
            this.sellerRuntime = sellerRuntime;
            this.marketStateSupplier = marketStateSupplier;
            this.goalScheduler = goalScheduler;
            this.jobHandlerRegistry = jobHandlerRegistry;
        }

        private static LevelRuntimeState create(BannerModSettlementProjectRuntime projectRuntime,
                                                JobHandlerRegistry jobHandlerRegistry) {
            BannerModHomeAssignmentRuntime homeRuntime = new BannerModHomeAssignmentRuntime();
            BannerModSellerDispatchRuntime sellerRuntime = new BannerModSellerDispatchRuntime();
            MutableMarketStateSupplier marketStateSupplier = new MutableMarketStateSupplier();
            return new LevelRuntimeState(
                    projectRuntime,
                    homeRuntime,
                    sellerRuntime,
                    marketStateSupplier,
                    BannerModResidentGoalScheduler.withDefaultGoals(homeRuntime, marketStateSupplier, sellerRuntime),
                    jobHandlerRegistry == null ? JobHandlerRegistry.defaults() : jobHandlerRegistry
            );
        }
    }

    static final class MutableMarketStateSupplier implements java.util.function.Supplier<BannerModSettlementMarketState> {
        private BannerModSettlementMarketState marketState = BannerModSettlementMarketState.empty();

        @Override
        public BannerModSettlementMarketState get() {
            return this.marketState;
        }

        void set(@Nullable BannerModSettlementMarketState marketState) {
            this.marketState = marketState == null ? BannerModSettlementMarketState.empty() : marketState;
        }
    }
}
