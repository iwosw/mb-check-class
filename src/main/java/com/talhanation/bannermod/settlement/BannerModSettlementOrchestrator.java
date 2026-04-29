package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchRuntime;
import com.talhanation.bannermod.settlement.dispatch.BannerModSellerDispatchSavedData;
import com.talhanation.bannermod.settlement.dispatch.SellerPhase;
import com.talhanation.bannermod.settlement.dispatch.SellerPhaseRecord;
import com.talhanation.bannermod.settlement.goal.BannerModResidentGoalScheduler;
import com.talhanation.bannermod.settlement.goal.ResidentGoalContext;
import com.talhanation.bannermod.settlement.goal.ResidentTask;
import com.talhanation.bannermod.settlement.goal.impl.WorkResidentGoal;
import com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthContext;
import com.talhanation.bannermod.settlement.growth.BannerModSettlementGrowthManager;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentAdvisor;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentRuntime;
import com.talhanation.bannermod.settlement.household.BannerModHomeAssignmentSavedData;
import com.talhanation.bannermod.settlement.household.HomePreference;
import com.talhanation.bannermod.settlement.job.JobExecutionContext;
import com.talhanation.bannermod.settlement.job.JobHandlerRegistry;
import com.talhanation.bannermod.settlement.project.BannerModSettlementProjectRuntime;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublishContext;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderPublisherRegistry;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntime;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderSavedData;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        tickBatch(level, settlementManager, governorManager, 0, Integer.MAX_VALUE);
    }

    public static BatchResult tickBatch(ServerLevel level,
                                        BannerModSettlementManager settlementManager,
                                        @Nullable BannerModGovernorManager governorManager,
                                        int startIndex,
                                        int maxSnapshots) {
        if (level == null || settlementManager == null) {
            return BatchResult.completedResult();
        }
        long startNanos = System.nanoTime();
        LevelRuntimeState state = runtimeState(level);
        long gameTime = level.getGameTime();
        state.workOrderRuntime.reclaimAbandoned(gameTime);
        List<UUID> snapshotOrder = state.snapshotOrderForBatch(settlementManager, startIndex);
        int total = snapshotOrder.size();
        if (total == 0 || maxSnapshots <= 0) {
            return recordBatchResult("settlement.heartbeat.orchestrator_batch", new BatchResult(0, total == 0 ? 0 : Math.max(0, Math.min(startIndex, total)), total, total == 0), startNanos);
        }

        int clampedStart = Math.max(0, Math.min(startIndex, total));
        int endIndex = Math.min(total, clampedStart + maxSnapshots);
        for (int i = clampedStart; i < endIndex; i++) {
            BannerModSettlementSnapshot snapshot = settlementManager.getSnapshot(snapshotOrder.get(i));
            if (snapshot == null) {
                continue;
            }
            BannerModGovernorSnapshot governorSnapshot = governorManager == null
                    ? null
                    : governorManager.getSnapshot(snapshot.claimUuid());
            tickSnapshot(state, snapshot, governorSnapshot, level, gameTime);
        }
        return recordBatchResult("settlement.heartbeat.orchestrator_batch", new BatchResult(clampedStart, endIndex, total, endIndex >= total), startNanos);
    }

    private static BatchResult recordBatchResult(String keyPrefix, BatchResult result, long startNanos) {
        RuntimeProfilingCounters.recordBatch(keyPrefix, Math.max(0, result.nextIndex() - result.startIndex()), result.totalItems(), System.nanoTime() - startNanos, result.completed());
        return result;
    }

    public record BatchResult(int startIndex,
                              int nextIndex,
                              int totalItems,
                              boolean completed) {
        private static BatchResult completedResult() {
            return new BatchResult(0, 0, 0, true);
        }
    }

    /** Accessor for the per-level work-order runtime, used by worker AI goals. */
    public static SettlementWorkOrderRuntime workOrderRuntime(ServerLevel level) {
        if (level == null) {
            return null;
        }
        return runtimeState(level).workOrderRuntime;
    }

    static LevelRuntimeState detachedStateForTests(JobHandlerRegistry jobHandlerRegistry) {
        return LevelRuntimeState.create(BannerModSettlementProjectRuntime.detachedForTests(), jobHandlerRegistry);
    }

    static void tickSnapshot(LevelRuntimeState state,
                             BannerModSettlementSnapshot snapshot,
                             @Nullable BannerModGovernorSnapshot governorSnapshot,
                             long gameTime) {
        tickSnapshot(state, snapshot, governorSnapshot, null, gameTime);
    }

    static void tickSnapshot(LevelRuntimeState state,
                             BannerModSettlementSnapshot snapshot,
                             @Nullable BannerModGovernorSnapshot governorSnapshot,
                             @Nullable ServerLevel level,
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
                level,
                snapshot.claimUuid(),
                growthQueue,
                BannerModSettlementProjectRuntime.buildAreaResolver(level),
                gameTime
        );

        assignHomes(state.homeRuntime, snapshot, gameTime);
        state.marketStateSupplier.set(snapshot.marketState());
        tickSellerDispatches(state.sellerRuntime, snapshot.marketState(), gameTime);
        publishBuildingWorkOrders(state, snapshot, level, gameTime);

        for (BannerModSettlementResidentRecord resident : snapshot.residents()) {
            if (resident == null || resident.residentUuid() == null) {
                continue;
            }
            ResidentGoalContext goalContext = new ResidentGoalContext(resident, snapshot, gameTime);
            state.goalScheduler.tick(goalContext);
            runResidentJobStep(state, goalContext);
        }
    }

    private static synchronized LevelRuntimeState runtimeState(ServerLevel level) {
        return PER_LEVEL.computeIfAbsent(level,
                ignored -> LevelRuntimeState.create(
                        BannerModSettlementProjectRuntime.forServer(level),
                        JobHandlerRegistry.defaults(),
                        SettlementWorkOrderSavedData.get(level).runtime(),
                        BannerModHomeAssignmentSavedData.get(level).runtime(),
                        BannerModSellerDispatchSavedData.get(level).runtime()
                ));
    }

    private static void publishBuildingWorkOrders(LevelRuntimeState state,
                                                  BannerModSettlementSnapshot snapshot,
                                                  @Nullable ServerLevel level,
                                                  long gameTime) {
        if (state.publisherRegistry.size() == 0) {
            return;
        }
        for (BannerModSettlementBuildingRecord building : snapshot.buildings()) {
            if (building == null || building.buildingUuid() == null) {
                continue;
            }
            SettlementWorkOrderPublishContext ctx = new SettlementWorkOrderPublishContext(
                    state.workOrderRuntime,
                    snapshot.claimUuid(),
                    building,
                    snapshot,
                    level,
                    gameTime
            );
            state.publisherRegistry.publishAll(ctx);
        }
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
        java.util.Map<UUID, UUID> seededMarketsBySeller = new java.util.LinkedHashMap<>();
        for (BannerModSettlementMarketRecord market : marketState.markets()) {
            if (market != null && market.open() && market.buildingUuid() != null) {
                openMarkets.add(market.buildingUuid());
            }
        }
        for (BannerModSettlementSellerDispatchRecord seed : marketState.sellerDispatches()) {
            if (seed != null && seed.residentUuid() != null && seed.marketUuid() != null) {
                seededMarketsBySeller.put(seed.residentUuid(), seed.marketUuid());
            }
        }

        for (SellerPhaseRecord dispatch : sellerRuntime.activeDispatches()) {
            if (dispatch != null && dispatch.sellerResidentUuid() != null) {
                UUID seededMarketUuid = seededMarketsBySeller.get(dispatch.sellerResidentUuid());
                if (seededMarketUuid == null || !seededMarketUuid.equals(dispatch.marketRecordUuid())) {
                    sellerRuntime.advance(dispatch.sellerResidentUuid(), SellerPhase.CANCELLED, gameTime);
                    continue;
                }
            }
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

    private static void runResidentJobStep(LevelRuntimeState state, ResidentGoalContext goalContext) {
        BannerModSettlementResidentRecord resident = goalContext.resident();
        if (resident.jobDefinition() == null) {
            return;
        }
        Optional<ResidentTask> task = state.goalScheduler.currentTask(resident.residentUuid());
        if (task.isEmpty() || task.get().isDone() || !WorkResidentGoal.ID.equals(task.get().goalId())) {
            return;
        }
        Long cooldownExpiry = state.jobCooldownExpiries.get(resident.residentUuid());
        if (cooldownExpiry != null && goalContext.gameTime() < cooldownExpiry) {
            return;
        }

        JobExecutionContext context = jobContext(state, resident, goalContext.gameTime());
        state.jobHandlerRegistry.lookup(resident.jobDefinition().handlerSeed())
                .filter(handler -> handler.canHandle(context))
                .ifPresent(handler -> {
                    handler.runOneStep(context);
                    if (handler.cooldownTicks() > 0) {
                        state.jobCooldownExpiries.put(resident.residentUuid(), goalContext.gameTime() + handler.cooldownTicks());
                    }
                });
    }

    private static JobExecutionContext jobContext(LevelRuntimeState state,
                                                  BannerModSettlementResidentRecord resident,
                                                  long gameTime) {
        UUID workplaceUuid = resident.jobDefinition() == null || resident.jobDefinition().targetBuildingUuid() == null
                ? resident.boundWorkAreaUuid()
                : resident.jobDefinition().targetBuildingUuid();
        return new JobExecutionContext(
                resident,
                gameTime,
                resident.residentUuid(),
                workplaceUuid,
                state.workOrderRuntime
        );
    }

    static final class LevelRuntimeState {
        final BannerModSettlementProjectRuntime projectRuntime;
        final BannerModHomeAssignmentRuntime homeRuntime;
        final BannerModSellerDispatchRuntime sellerRuntime;
        final MutableMarketStateSupplier marketStateSupplier;
        final BannerModResidentGoalScheduler goalScheduler;
        final JobHandlerRegistry jobHandlerRegistry;
        final Map<UUID, Long> jobCooldownExpiries;
        final SettlementWorkOrderRuntime workOrderRuntime;
        final SettlementWorkOrderPublisherRegistry publisherRegistry;
        private final List<UUID> orchestratorSnapshotOrder = new ArrayList<>();

        private LevelRuntimeState(BannerModSettlementProjectRuntime projectRuntime,
                                  BannerModHomeAssignmentRuntime homeRuntime,
                                  BannerModSellerDispatchRuntime sellerRuntime,
                                  MutableMarketStateSupplier marketStateSupplier,
                                  BannerModResidentGoalScheduler goalScheduler,
                                  JobHandlerRegistry jobHandlerRegistry,
                                  Map<UUID, Long> jobCooldownExpiries,
                                  SettlementWorkOrderRuntime workOrderRuntime,
                                  SettlementWorkOrderPublisherRegistry publisherRegistry) {
            this.projectRuntime = projectRuntime;
            this.homeRuntime = homeRuntime;
            this.sellerRuntime = sellerRuntime;
            this.marketStateSupplier = marketStateSupplier;
            this.goalScheduler = goalScheduler;
            this.jobHandlerRegistry = jobHandlerRegistry;
            this.jobCooldownExpiries = jobCooldownExpiries;
            this.workOrderRuntime = workOrderRuntime;
            this.publisherRegistry = publisherRegistry;
        }

        List<UUID> snapshotOrderForBatch(BannerModSettlementManager settlementManager, int startIndex) {
            if (startIndex <= 0 || this.orchestratorSnapshotOrder.isEmpty()) {
                this.orchestratorSnapshotOrder.clear();
                for (BannerModSettlementSnapshot snapshot : settlementManager.getAllSnapshots()) {
                    if (snapshot != null && snapshot.claimUuid() != null) {
                        this.orchestratorSnapshotOrder.add(snapshot.claimUuid());
                    }
                }
                this.orchestratorSnapshotOrder.sort(Comparator.naturalOrder());
            }
            return this.orchestratorSnapshotOrder;
        }

        private static LevelRuntimeState create(BannerModSettlementProjectRuntime projectRuntime,
                                                  JobHandlerRegistry jobHandlerRegistry) {
            return create(projectRuntime, jobHandlerRegistry, new SettlementWorkOrderRuntime(),
                    new BannerModHomeAssignmentRuntime(), new BannerModSellerDispatchRuntime());
        }

        private static LevelRuntimeState create(BannerModSettlementProjectRuntime projectRuntime,
                                                JobHandlerRegistry jobHandlerRegistry,
                                                SettlementWorkOrderRuntime workOrderRuntime) {
            return create(projectRuntime, jobHandlerRegistry, workOrderRuntime,
                    new BannerModHomeAssignmentRuntime(), new BannerModSellerDispatchRuntime());
        }

        private static LevelRuntimeState create(BannerModSettlementProjectRuntime projectRuntime,
                                                JobHandlerRegistry jobHandlerRegistry,
                                                SettlementWorkOrderRuntime workOrderRuntime,
                                                BannerModHomeAssignmentRuntime homeRuntime,
                                                BannerModSellerDispatchRuntime sellerRuntime) {
            BannerModHomeAssignmentRuntime effectiveHomeRuntime = homeRuntime == null
                    ? new BannerModHomeAssignmentRuntime()
                    : homeRuntime;
            BannerModSellerDispatchRuntime effectiveSellerRuntime = sellerRuntime == null
                    ? new BannerModSellerDispatchRuntime()
                    : sellerRuntime;
            MutableMarketStateSupplier marketStateSupplier = new MutableMarketStateSupplier();
            return new LevelRuntimeState(
                    projectRuntime,
                    effectiveHomeRuntime,
                    effectiveSellerRuntime,
                    marketStateSupplier,
                    BannerModResidentGoalScheduler.withDefaultGoals(effectiveHomeRuntime, marketStateSupplier, effectiveSellerRuntime),
                    jobHandlerRegistry == null ? JobHandlerRegistry.defaults() : jobHandlerRegistry,
                    new HashMap<>(),
                    workOrderRuntime == null ? new SettlementWorkOrderRuntime() : workOrderRuntime,
                    SettlementWorkOrderPublisherRegistry.defaults()
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
