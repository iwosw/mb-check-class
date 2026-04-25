package com.talhanation.bannermod.settlement;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerIndex;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsReservation;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRoute;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsRuntime;
import com.talhanation.bannermod.shared.logistics.BannerModSeaTradeEntrypoint;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BannerModSettlementService {
    private BannerModSettlementService() {
    }

    public static void refreshAllClaims(ServerLevel level,
                                        RecruitsClaimManager claimManager,
                                        BannerModSettlementManager settlementManager,
                                        BannerModGovernorManager governorManager) {
        refreshClaimsBatch(level, claimManager, settlementManager, governorManager, 0, Integer.MAX_VALUE);
    }

    public static BatchResult refreshClaimsBatch(ServerLevel level,
                                                 RecruitsClaimManager claimManager,
                                                 BannerModSettlementManager settlementManager,
                                                 BannerModGovernorManager governorManager,
                                                 int startIndex,
                                                 int maxClaims) {
        if (level == null || claimManager == null || settlementManager == null) {
            return BatchResult.completedResult();
        }
        long startNanos = System.nanoTime();

        List<RecruitsClaim> claims = new ArrayList<>(claimManager.getAllClaims());
        claims.removeIf(claim -> claim == null || claim.getUUID() == null);
        claims.sort(Comparator.comparing(RecruitsClaim::getUUID));
        int total = claims.size();
        if (total == 0 || maxClaims <= 0) {
            if (total == 0) {
                settlementManager.pruneMissingClaims(Set.of());
            }
            return recordBatchResult("settlement.heartbeat.refresh_batch", new BatchResult(0, total == 0 ? 0 : Math.max(0, Math.min(startIndex, total)), total, total == 0), startNanos);
        }

        int clampedStart = Math.max(0, Math.min(startIndex, total));
        int endIndex = Math.min(total, clampedStart + maxClaims);
        for (int i = clampedStart; i < endIndex; i++) {
            settlementManager.putSnapshot(buildSnapshot(level, claims.get(i), governorManager));
        }

        if (endIndex >= total) {
            Set<UUID> activeClaimUuids = new LinkedHashSet<>();
            for (RecruitsClaim claim : claims) {
                activeClaimUuids.add(claim.getUUID());
            }
            settlementManager.pruneMissingClaims(activeClaimUuids);
        }

        return recordBatchResult("settlement.heartbeat.refresh_batch", new BatchResult(clampedStart, endIndex, total, endIndex >= total), startNanos);
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

    public static void refreshClaimAt(ServerLevel level,
                                      RecruitsClaimManager claimManager,
                                      BannerModSettlementManager settlementManager,
                                      BannerModGovernorManager governorManager,
                                      BlockPos pos) {
        if (level == null || claimManager == null || settlementManager == null || pos == null) {
            return;
        }
        refreshClaim(level, claimManager, settlementManager, governorManager, claimManager.getClaim(new ChunkPos(pos)));
    }

    public static void refreshClaim(ServerLevel level,
                                    RecruitsClaimManager claimManager,
                                    BannerModSettlementManager settlementManager,
                                    @Nullable BannerModGovernorManager governorManager,
                                    @Nullable RecruitsClaim claim) {
        if (level == null || claimManager == null || settlementManager == null) {
            return;
        }
        if (claim == null) {
            return;
        }
        settlementManager.putSnapshot(buildSnapshot(level, claim, governorManager));
    }

    static BannerModSettlementSnapshot buildSnapshot(ServerLevel level,
                                                     RecruitsClaim claim,
                                                     @Nullable BannerModGovernorManager governorManager) {
        ChunkPos anchorChunk = resolveAnchorChunk(claim);
        BannerModGovernorSnapshot governorSnapshot = governorManager == null ? null : governorManager.getSnapshot(claim.getUUID());
        String settlementFactionId = claim.getOwnerFaction() != null
                ? claim.getOwnerFactionStringID()
                : governorSnapshot == null ? null : governorSnapshot.settlementFactionId();

        List<BannerModSettlementResidentRecord> residents = collectResidents(level, claim, governorSnapshot, settlementFactionId);
        List<BannerModSettlementBuildingRecord> buildings = collectBuildings(level, claim);
        BannerModSettlementMarketState marketState = collectMarketState(level, claim);
        List<StorageArea> storageAreas = collectStorageAreas(level, claim);
        List<BannerModSeaTradeEntrypoint> liveSeaTradeEntrypoints = collectLiveSeaTradeEntrypoints(storageAreas);
        ReservationSignalSeed reservationSignalSeed = summarizeReservationSignalSeed(
                buildings,
                collectLocalLogisticsRoutes(storageAreas),
                BannerModLogisticsRuntime.service().listReservations()
        );
        Set<UUID> localBuildingUuids = new LinkedHashSet<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            localBuildingUuids.add(building.buildingUuid());
        }
        residents = applyResidentAssignmentSemantics(residents, localBuildingUuids);
        residents = applyResidentServiceContracts(residents, buildings);
        residents = applyResidentJobDefinitions(residents, buildings);
        buildings = applyAssignedResidents(buildings, residents);
        marketState = applySellerDispatchSeed(marketState, residents, buildings);
        residents = applyResidentJobTargetSelectionSeeds(residents, marketState);
        BannerModSettlementStockpileSummary stockpileSummary = summarizeStockpiles(buildings, liveSeaTradeEntrypoints);
        BannerModSettlementDesiredGoodsSeed desiredGoodsSeed = summarizeDesiredGoods(buildings, stockpileSummary, marketState);
        BannerModSettlementProjectCandidateSeed projectCandidateSeed = summarizeProjectCandidate(
                buildings,
                stockpileSummary,
                desiredGoodsSeed,
                marketState,
                governorSnapshot != null && governorSnapshot.governorRecruitUuid() != null,
                settlementFactionId != null && !settlementFactionId.isBlank()
        );
        BannerModSettlementTradeRouteHandoffSeed tradeRouteHandoffSeed = summarizeTradeRouteHandoffSeed(stockpileSummary, marketState, desiredGoodsSeed, reservationSignalSeed);
        BannerModSettlementSupplySignalState supplySignalState = summarizeSupplySignals(desiredGoodsSeed, stockpileSummary, marketState, residents, buildings, reservationSignalSeed);
        int residentCapacity = 0;
        int workplaceCapacity = 0;
        int assignedWorkerCount = 0;
        int assignedResidentCount = 0;
        int unassignedWorkerCount = 0;
        int missingWorkAreaAssignmentCount = 0;
        for (BannerModSettlementBuildingRecord building : buildings) {
            residentCapacity += Math.max(0, building.residentCapacity());
            workplaceCapacity += Math.max(0, building.workplaceSlots());
            assignedWorkerCount += Math.max(0, building.assignedWorkerCount());
        }
        for (BannerModSettlementResidentRecord resident : residents) {
            if (resident.role() != BannerModSettlementResidentRole.CONTROLLED_WORKER) {
                continue;
            }
            switch (resident.assignmentState()) {
                case ASSIGNED_LOCAL_BUILDING -> assignedResidentCount++;
                case UNASSIGNED -> unassignedWorkerCount++;
                case ASSIGNED_MISSING_BUILDING -> missingWorkAreaAssignmentCount++;
                default -> {
                }
            }
        }

        return new BannerModSettlementSnapshot(
                claim.getUUID(),
                anchorChunk.x,
                anchorChunk.z,
                settlementFactionId,
                level.getGameTime(),
                residentCapacity,
                workplaceCapacity,
                assignedWorkerCount,
                assignedResidentCount,
                unassignedWorkerCount,
                missingWorkAreaAssignmentCount,
                stockpileSummary,
                marketState,
                desiredGoodsSeed,
                projectCandidateSeed,
                tradeRouteHandoffSeed,
                supplySignalState,
                residents,
                buildings
        );
    }

    private static List<BannerModSettlementResidentRecord> collectResidents(ServerLevel level,
                                                                            RecruitsClaim claim,
                                                                            @Nullable BannerModGovernorSnapshot governorSnapshot,
                                                                            @Nullable String settlementFactionId) {
        Map<UUID, BannerModSettlementResidentRecord> residents = new LinkedHashMap<>();
        for (Villager villager : level.getEntitiesOfClass(Villager.class, claimBounds(level, claim), entity -> entity.isAlive() && claim.containsChunk(entity.chunkPosition()))) {
            residents.put(villager.getUUID(), new BannerModSettlementResidentRecord(
                    villager.getUUID(),
                    BannerModSettlementResidentRole.VILLAGER,
                    BannerModSettlementResidentScheduleSeed.SETTLEMENT_IDLE,
                    BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX,
                    BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE,
                    BannerModSettlementResidentServiceContract.notServiceActor(),
                    BannerModSettlementResidentJobDefinition.defaultFor(
                            BannerModSettlementResidentRole.VILLAGER,
                            BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE,
                            BannerModSettlementResidentServiceContract.notServiceActor(),
                            null
                    ),
                    BannerModSettlementResidentMode.SETTLEMENT_RESIDENT,
                    null,
                    villager.getTeam() == null ? settlementFactionId : villager.getTeam().getName(),
                    null,
                    BannerModSettlementResidentAssignmentState.NOT_APPLICABLE
            ));
        }
        for (AbstractWorkerEntity worker : workersInClaim(level, claim)) {
            BannerModSettlementResidentScheduleSeed scheduleSeed = BannerModSettlementResidentScheduleSeed.defaultFor(BannerModSettlementResidentRole.CONTROLLED_WORKER, worker.getBoundWorkAreaUUID());
            BannerModSettlementResidentMode residentMode = BannerModSettlementResidentMode.defaultFor(BannerModSettlementResidentRole.CONTROLLED_WORKER, worker.getOwnerUUID());
            BannerModSettlementResidentAssignmentState assignmentState = worker.getBoundWorkAreaUUID() == null
                    ? BannerModSettlementResidentAssignmentState.UNASSIGNED
                    : BannerModSettlementResidentAssignmentState.ASSIGNED_MISSING_BUILDING;
            BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed = BannerModSettlementResidentRuntimeRoleSeed.defaultFor(
                    BannerModSettlementResidentRole.CONTROLLED_WORKER,
                    scheduleSeed,
                    residentMode,
                    assignmentState
            );
            residents.put(worker.getUUID(), new BannerModSettlementResidentRecord(
                    worker.getUUID(),
                    BannerModSettlementResidentRole.CONTROLLED_WORKER,
                    scheduleSeed,
                    BannerModSettlementResidentScheduleWindowSeed.defaultFor(scheduleSeed, runtimeRoleSeed),
                    runtimeRoleSeed,
                    BannerModSettlementResidentServiceContract.defaultFor(BannerModSettlementResidentRole.CONTROLLED_WORKER, residentMode, assignmentState, worker.getBoundWorkAreaUUID(), null),
                    BannerModSettlementResidentJobDefinition.defaultFor(
                            BannerModSettlementResidentRole.CONTROLLED_WORKER,
                            runtimeRoleSeed,
                            BannerModSettlementResidentServiceContract.defaultFor(BannerModSettlementResidentRole.CONTROLLED_WORKER, residentMode, assignmentState, worker.getBoundWorkAreaUUID(), null),
                            null
                    ),
                    residentMode,
                    worker.getOwnerUUID(),
                    worker.getTeam() == null ? null : worker.getTeam().getName(),
                    worker.getBoundWorkAreaUUID(),
                    assignmentState
            ));
        }
        if (governorSnapshot != null && governorSnapshot.governorRecruitUuid() != null) {
            residents.put(governorSnapshot.governorRecruitUuid(), new BannerModSettlementResidentRecord(
                    governorSnapshot.governorRecruitUuid(),
                    BannerModSettlementResidentRole.GOVERNOR_RECRUIT,
                    BannerModSettlementResidentScheduleSeed.GOVERNING,
                    BannerModSettlementResidentScheduleWindowSeed.CIVIC_DAY,
                    BannerModSettlementResidentRuntimeRoleSeed.GOVERNANCE,
                    BannerModSettlementResidentServiceContract.notServiceActor(),
                    BannerModSettlementResidentJobDefinition.defaultFor(
                            BannerModSettlementResidentRole.GOVERNOR_RECRUIT,
                            BannerModSettlementResidentRuntimeRoleSeed.GOVERNANCE,
                            BannerModSettlementResidentServiceContract.notServiceActor(),
                            null
                    ),
                    BannerModSettlementResidentMode.SETTLEMENT_RESIDENT,
                    governorSnapshot.governorOwnerUuid(),
                    settlementFactionId,
                    null,
                    BannerModSettlementResidentAssignmentState.NOT_APPLICABLE
            ));
        }
        return new ArrayList<>(residents.values());
    }

    private static List<AbstractWorkerEntity> workersInClaim(ServerLevel level, RecruitsClaim claim) {
        return WorkerIndex.instance()
                .queryInClaim(level, claim)
                .orElseGet(() -> {
                    RuntimeProfilingCounters.increment("worker.index.fallback_scans");
                    return level.getEntitiesOfClass(AbstractWorkerEntity.class, claimBounds(level, claim), entity -> entity.isAlive() && claim.containsChunk(entity.chunkPosition()));
                });
    }

    static List<BannerModSettlementResidentRecord> applyResidentAssignmentSemantics(List<BannerModSettlementResidentRecord> residents,
                                                                                    Set<UUID> localBuildingUuids) {
        if (residents.isEmpty()) {
            return List.of();
        }

        List<BannerModSettlementResidentRecord> updatedResidents = new ArrayList<>(residents.size());
        for (BannerModSettlementResidentRecord resident : residents) {
            if (resident.role() != BannerModSettlementResidentRole.CONTROLLED_WORKER) {
                updatedResidents.add(resident);
                continue;
            }

            BannerModSettlementResidentAssignmentState assignmentState;
            if (resident.boundWorkAreaUuid() == null) {
                assignmentState = BannerModSettlementResidentAssignmentState.UNASSIGNED;
            } else if (localBuildingUuids.contains(resident.boundWorkAreaUuid())) {
                assignmentState = BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING;
            } else {
                assignmentState = BannerModSettlementResidentAssignmentState.ASSIGNED_MISSING_BUILDING;
            }

            BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed = BannerModSettlementResidentRuntimeRoleSeed.defaultFor(
                    resident.role(),
                    resident.scheduleSeed(),
                    resident.residentMode(),
                    assignmentState
            );
            BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed = BannerModSettlementResidentScheduleWindowSeed.defaultFor(
                    resident.scheduleSeed(),
                    runtimeRoleSeed
            );

            updatedResidents.add(new BannerModSettlementResidentRecord(
                    resident.residentUuid(),
                    resident.role(),
                    resident.scheduleSeed(),
                    scheduleWindowSeed,
                    runtimeRoleSeed,
                    resident.serviceContract(),
                    resident.jobDefinition(),
                    resident.jobTargetSelectionSeed(),
                    resident.residentMode(),
                    resident.ownerUuid(),
                    resident.teamId(),
                    resident.boundWorkAreaUuid(),
                    assignmentState,
                    BannerModSettlementResidentRoleProfile.defaultFor(
                            resident.role(),
                            runtimeRoleSeed,
                            resident.residentMode(),
                            assignmentState
                    )
            ));
        }
        return updatedResidents;
    }

    static List<BannerModSettlementResidentRecord> applyResidentServiceContracts(List<BannerModSettlementResidentRecord> residents,
                                                                                 List<BannerModSettlementBuildingRecord> buildings) {
        if (residents.isEmpty()) {
            return List.of();
        }

        Map<UUID, BannerModSettlementBuildingRecord> buildingsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            buildingsByUuid.put(building.buildingUuid(), building);
        }

        List<BannerModSettlementResidentRecord> updatedResidents = new ArrayList<>(residents.size());
        for (BannerModSettlementResidentRecord resident : residents) {
            BannerModSettlementBuildingRecord serviceBuilding = resident.boundWorkAreaUuid() == null
                    ? null
                    : buildingsByUuid.get(resident.boundWorkAreaUuid());
            BannerModSettlementResidentServiceContract serviceContract = BannerModSettlementResidentServiceContract.defaultFor(
                    resident.role(),
                    resident.residentMode(),
                    resident.assignmentState(),
                    resident.boundWorkAreaUuid(),
                    serviceBuilding == null ? null : serviceBuilding.buildingTypeId()
            );
            updatedResidents.add(new BannerModSettlementResidentRecord(
                    resident.residentUuid(),
                    resident.role(),
                    resident.scheduleSeed(),
                    resident.scheduleWindowSeed(),
                    resident.runtimeRoleSeed(),
                    serviceContract,
                    resident.jobDefinition(),
                    resident.jobTargetSelectionSeed(),
                    resident.residentMode(),
                    resident.ownerUuid(),
                    resident.teamId(),
                    resident.boundWorkAreaUuid(),
                    resident.assignmentState(),
                    resident.roleProfile()
            ));
        }
        return updatedResidents;
    }

    static List<BannerModSettlementResidentRecord> applyResidentJobDefinitions(List<BannerModSettlementResidentRecord> residents,
                                                                               List<BannerModSettlementBuildingRecord> buildings) {
        if (residents.isEmpty()) {
            return List.of();
        }

        Map<UUID, BannerModSettlementBuildingRecord> buildingsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            buildingsByUuid.put(building.buildingUuid(), building);
        }

        List<BannerModSettlementResidentRecord> updatedResidents = new ArrayList<>(residents.size());
        for (BannerModSettlementResidentRecord resident : residents) {
            BannerModSettlementBuildingRecord targetBuilding = resident.serviceContract().serviceBuildingUuid() == null
                    ? null
                    : buildingsByUuid.get(resident.serviceContract().serviceBuildingUuid());
            BannerModSettlementResidentJobDefinition jobDefinition = BannerModSettlementResidentJobDefinition.defaultFor(
                    resident.role(),
                    resident.runtimeRoleSeed(),
                    resident.serviceContract(),
                    targetBuilding
            );
            updatedResidents.add(new BannerModSettlementResidentRecord(
                    resident.residentUuid(),
                    resident.role(),
                    resident.scheduleSeed(),
                    resident.scheduleWindowSeed(),
                    resident.runtimeRoleSeed(),
                    resident.serviceContract(),
                    jobDefinition,
                    resident.jobTargetSelectionSeed(),
                    resident.residentMode(),
                    resident.ownerUuid(),
                    resident.teamId(),
                    resident.boundWorkAreaUuid(),
                    resident.assignmentState(),
                    resident.roleProfile()
            ));
        }
        return updatedResidents;
    }

    static List<BannerModSettlementResidentRecord> applyResidentJobTargetSelectionSeeds(List<BannerModSettlementResidentRecord> residents,
                                                                                        BannerModSettlementMarketState marketState) {
        if (residents.isEmpty()) {
            return List.of();
        }

        List<BannerModSettlementResidentRecord> updatedResidents = new ArrayList<>(residents.size());
        for (BannerModSettlementResidentRecord resident : residents) {
            BannerModSettlementResidentJobTargetSelectionSeed jobTargetSelectionSeed = BannerModSettlementResidentJobTargetSelectionSeed.defaultFor(
                    resident.residentUuid(),
                    resident.jobDefinition(),
                    resident.serviceContract(),
                    marketState
            );
            updatedResidents.add(new BannerModSettlementResidentRecord(
                    resident.residentUuid(),
                    resident.role(),
                    resident.scheduleSeed(),
                    resident.scheduleWindowSeed(),
                    resident.runtimeRoleSeed(),
                    resident.serviceContract(),
                    resident.jobDefinition(),
                    jobTargetSelectionSeed,
                    resident.residentMode(),
                    resident.ownerUuid(),
                    resident.teamId(),
                    resident.boundWorkAreaUuid(),
                    resident.assignmentState(),
                    resident.roleProfile(),
                    resident.schedulePolicy()
            ));
        }
        return updatedResidents;
    }

    private static List<BannerModSettlementBuildingRecord> collectBuildings(ServerLevel level,
                                                                            RecruitsClaim claim) {
        List<BannerModSettlementBuildingRecord> buildings = new ArrayList<>();
        for (AbstractWorkAreaEntity workArea : collectWorkAreas(level, claim, AbstractWorkAreaEntity.class)) {
            StockpileSeed stockpileSeed = resolveStockpileSeed(workArea);
            BannerModSettlementBuildingProfileSeed profileSeed = BannerModSettlementBuildingProfileSeed.fromWorkArea(workArea);
            buildings.add(new BannerModSettlementBuildingRecord(
                    workArea.getUUID(),
                    resolveBuildingTypeId(workArea),
                    workArea.getOriginPos(),
                    workArea.getPlayerUUID(),
                    workArea.getTeamStringID(),
                    0,
                    1,
                    0,
                    List.of(),
                    stockpileSeed.stockpileBuilding(),
                    stockpileSeed.containerCount(),
                    stockpileSeed.slotCapacity(),
                    stockpileSeed.routeAuthored(),
                    stockpileSeed.portEntrypoint(),
                    stockpileSeed.typeIds(),
                    profileSeed.category(),
                    profileSeed
            ));
        }
        return buildings;
    }

    static List<BannerModSettlementBuildingRecord> applyAssignedResidents(List<BannerModSettlementBuildingRecord> buildings,
                                                                          List<BannerModSettlementResidentRecord> residents) {
        if (buildings.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<UUID>> assignedResidentsByBuilding = new LinkedHashMap<>();
        for (BannerModSettlementResidentRecord resident : residents) {
            if (resident.role() != BannerModSettlementResidentRole.CONTROLLED_WORKER
                    || resident.assignmentState() != BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING
                    || resident.boundWorkAreaUuid() == null) {
                continue;
            }
            assignedResidentsByBuilding.computeIfAbsent(resident.boundWorkAreaUuid(), ignored -> new ArrayList<>())
                    .add(resident.residentUuid());
        }

        List<BannerModSettlementBuildingRecord> updatedBuildings = new ArrayList<>(buildings.size());
        for (BannerModSettlementBuildingRecord building : buildings) {
            List<UUID> assignedResidents = assignedResidentsByBuilding.getOrDefault(building.buildingUuid(), List.of());
            updatedBuildings.add(new BannerModSettlementBuildingRecord(
                    building.buildingUuid(),
                    building.buildingTypeId(),
                    building.originPos(),
                    building.ownerUuid(),
                    building.teamId(),
                    building.residentCapacity(),
                    building.workplaceSlots(),
                    assignedResidents.size(),
                    assignedResidents,
                    building.stockpileBuilding(),
                    building.stockpileContainerCount(),
                    building.stockpileSlotCapacity(),
                    building.stockpileRouteAuthored(),
                    building.stockpilePortEntrypoint(),
                    building.stockpileTypeIds(),
                    building.buildingCategory(),
                    building.buildingProfileSeed()
            ));
        }
        return updatedBuildings;
    }

    static BannerModSettlementStockpileSummary summarizeStockpiles(List<BannerModSettlementBuildingRecord> buildings) {
        return summarizeStockpiles(buildings, List.of());
    }

    static BannerModSettlementStockpileSummary summarizeStockpiles(List<BannerModSettlementBuildingRecord> buildings,
                                                                   List<BannerModSeaTradeEntrypoint> liveSeaTradeEntrypoints) {
        if (buildings.isEmpty()) {
            return BannerModSettlementStockpileSummary.empty();
        }

        int storageBuildingCount = 0;
        int containerCount = 0;
        int slotCapacity = 0;
        int routedStorageCount = 0;
        int portEntrypointCount = 0;
        Set<String> authoredStorageTypeIds = new LinkedHashSet<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            if (!building.stockpileBuilding()) {
                continue;
            }
            storageBuildingCount++;
            containerCount += Math.max(0, building.stockpileContainerCount());
            slotCapacity += Math.max(0, building.stockpileSlotCapacity());
            if (building.stockpileRouteAuthored()) {
                routedStorageCount++;
            }
            if (building.stockpilePortEntrypoint()) {
                portEntrypointCount++;
            }
            authoredStorageTypeIds.addAll(building.stockpileTypeIds());
        }

        Set<UUID> routedStorageIds = new LinkedHashSet<>();
        Set<UUID> portStorageIds = new LinkedHashSet<>();
        for (BannerModSeaTradeEntrypoint entrypoint : liveSeaTradeEntrypoints) {
            routedStorageIds.add(entrypoint.settlementStorageAreaId());
            portStorageIds.add(entrypoint.portStorageAreaId());
        }

        return new BannerModSettlementStockpileSummary(
                storageBuildingCount,
                containerCount,
                slotCapacity,
                routedStorageIds.isEmpty() ? routedStorageCount : routedStorageIds.size(),
                portStorageIds.isEmpty() ? portEntrypointCount : portStorageIds.size(),
                new ArrayList<>(authoredStorageTypeIds)
        );
    }

    static BannerModSettlementMarketState summarizeMarketState(List<BannerModSettlementMarketRecord> markets) {
        if (markets.isEmpty()) {
            return BannerModSettlementMarketState.empty();
        }

        int openMarketCount = 0;
        int totalStorageSlots = 0;
        int freeStorageSlots = 0;
        for (BannerModSettlementMarketRecord market : markets) {
            if (market.open()) {
                openMarketCount++;
            }
            totalStorageSlots += Math.max(0, market.totalStorageSlots());
            freeStorageSlots += Math.max(0, market.freeStorageSlots());
        }

        return new BannerModSettlementMarketState(markets.size(), openMarketCount, totalStorageSlots, freeStorageSlots, 0, 0, markets, List.of());
    }

    static BannerModSettlementDesiredGoodsSeed summarizeDesiredGoods(List<BannerModSettlementBuildingRecord> buildings,
                                                                     BannerModSettlementStockpileSummary stockpileSummary,
                                                                     BannerModSettlementMarketState marketState) {
        Map<String, Integer> desiredGoods = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            String desiredGoodId = switch (building.buildingProfileSeed()) {
                case FOOD_PRODUCTION -> "food";
                case MATERIAL_PRODUCTION -> "materials";
                case CONSTRUCTION -> "construction_materials";
                case MARKET -> "market_goods";
                default -> "";
            };
            addDesiredGoodDriver(desiredGoods, desiredGoodId, 1);
        }
        for (String storageTypeId : stockpileSummary.authoredStorageTypeIds()) {
            addDesiredGoodDriver(desiredGoods, "storage_type:" + storageTypeId, 1);
        }
        addDesiredGoodDriver(desiredGoods, "market_goods", marketState.marketCount());
        addDesiredGoodDriver(desiredGoods, "trade_stock", marketState.openMarketCount());

        List<BannerModSettlementDesiredGoodSeed> desiredGoodSeeds = new ArrayList<>(desiredGoods.size());
        for (Map.Entry<String, Integer> entry : desiredGoods.entrySet()) {
            desiredGoodSeeds.add(new BannerModSettlementDesiredGoodSeed(entry.getKey(), entry.getValue()));
        }
        return new BannerModSettlementDesiredGoodsSeed(desiredGoodSeeds);
    }

    static BannerModSettlementTradeRouteHandoffSeed summarizeTradeRouteHandoffSeed(BannerModSettlementStockpileSummary stockpileSummary,
                                                                                    BannerModSettlementMarketState marketState,
                                                                                    BannerModSettlementDesiredGoodsSeed desiredGoodsSeed,
                                                                                    ReservationSignalSeed reservationSignalSeed) {
        return new BannerModSettlementTradeRouteHandoffSeed(
                marketState.sellerDispatchCount(),
                marketState.readySellerDispatchCount(),
                stockpileSummary.routedStorageCount(),
                stockpileSummary.portEntrypointCount(),
                reservationSignalSeed.activeReservationCount(),
                reservationSignalSeed.reservedUnitCount(),
                desiredGoodsSeed.desiredGoods(),
                marketState.sellerDispatches()
        );
    }

    static BannerModSettlementSupplySignalState summarizeSupplySignals(BannerModSettlementDesiredGoodsSeed desiredGoodsSeed,
                                                                       BannerModSettlementStockpileSummary stockpileSummary,
                                                                       BannerModSettlementMarketState marketState,
                                                                       List<BannerModSettlementResidentRecord> residents,
                                                                       List<BannerModSettlementBuildingRecord> buildings,
                                                                       ReservationSignalSeed reservationSignalSeed) {
        if (desiredGoodsSeed.desiredGoods().isEmpty()) {
            return BannerModSettlementSupplySignalState.empty();
        }

        Map<UUID, BannerModSettlementBuildingRecord> buildingsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            buildingsByUuid.put(building.buildingUuid(), building);
        }

        Map<String, Integer> serviceCoverageByGood = new LinkedHashMap<>();
        for (BannerModSettlementResidentRecord resident : residents) {
            BannerModSettlementResidentServiceContract serviceContract = resident.serviceContract();
            if (serviceContract.actorState() != BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE
                    || serviceContract.serviceBuildingUuid() == null) {
                continue;
            }

            BannerModSettlementBuildingRecord serviceBuilding = buildingsByUuid.get(serviceContract.serviceBuildingUuid());
            if (serviceBuilding == null) {
                continue;
            }

            String goodId = desiredGoodIdForProfile(serviceBuilding.buildingProfileSeed());
            if (!goodId.isBlank()) {
                serviceCoverageByGood.merge(goodId, 1, Integer::sum);
            }
        }

        List<BannerModSettlementSupplySignal> signals = new ArrayList<>();
        int shortageSignalCount = 0;
        int shortageUnitCount = 0;
        int reservationHintUnitCount = 0;
        for (BannerModSettlementDesiredGoodSeed desiredGood : desiredGoodsSeed.desiredGoods()) {
            int coverageUnits = resolveSupplyCoverageUnits(desiredGood.desiredGoodId(), stockpileSummary, marketState, serviceCoverageByGood);
            int shortageUnits = Math.max(0, desiredGood.driverCount() - coverageUnits);
            int reservationHintUnits = reservationSignalSeed.reservationHintUnitsByGood().getOrDefault(desiredGood.desiredGoodId(), 0);
            if (shortageUnits > 0) {
                shortageSignalCount++;
                shortageUnitCount += shortageUnits;
            }
            reservationHintUnitCount += reservationHintUnits;
            signals.add(new BannerModSettlementSupplySignal(
                    desiredGood.desiredGoodId(),
                    desiredGood.driverCount(),
                    coverageUnits,
                    shortageUnits,
                    reservationHintUnits
            ));
        }

        return new BannerModSettlementSupplySignalState(
                signals.size(),
                shortageSignalCount,
                shortageUnitCount,
                reservationHintUnitCount,
                signals
        );
    }

    static BannerModSettlementProjectCandidateSeed summarizeProjectCandidate(List<BannerModSettlementBuildingRecord> buildings,
                                                                            BannerModSettlementStockpileSummary stockpileSummary,
                                                                            BannerModSettlementDesiredGoodsSeed desiredGoodsSeed,
                                                                            BannerModSettlementMarketState marketState,
                                                                            boolean governedSettlement,
                                                                            boolean claimedSettlement) {
        Map<BannerModSettlementBuildingProfileSeed, Integer> profileCounts = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            profileCounts.merge(building.buildingProfileSeed(), 1, Integer::sum);
        }

        Map<String, Integer> desiredGoodsById = new LinkedHashMap<>();
        for (BannerModSettlementDesiredGoodSeed desiredGood : desiredGoodsSeed.desiredGoods()) {
            desiredGoodsById.merge(desiredGood.desiredGoodId(), desiredGood.driverCount(), Integer::sum);
        }

        int governanceBoost = (governedSettlement ? 1 : 0) + (claimedSettlement ? 1 : 0);
        if (stockpileSummary.storageBuildingCount() <= 0 && (!buildings.isEmpty() || !desiredGoodsById.isEmpty())) {
            return new BannerModSettlementProjectCandidateSeed(
                    "storage_foundation",
                    BannerModSettlementBuildingProfileSeed.STORAGE,
                    1 + governanceBoost + Math.min(2, desiredGoodsById.size()),
                    governedSettlement,
                    claimedSettlement,
                    List.of("storage_missing", "goods_pressure", marketState.marketCount() > 0 ? "market_access_present" : "market_access_absent")
            );
        }
        if (marketState.marketCount() <= 0 && desiredGoodsById.getOrDefault("market_goods", 0) > 0) {
            return new BannerModSettlementProjectCandidateSeed(
                    "market_foundation",
                    BannerModSettlementBuildingProfileSeed.MARKET,
                    1 + governanceBoost + Math.min(2, desiredGoodsById.getOrDefault("market_goods", 0)),
                    governedSettlement,
                    claimedSettlement,
                    List.of("market_missing", "market_goods_demand", stockpileSummary.slotCapacity() > 0 ? "stockpile_ready" : "stockpile_thin")
            );
        }
        if (marketState.marketCount() > marketState.openMarketCount()) {
            return new BannerModSettlementProjectCandidateSeed(
                    "market_recovery",
                    BannerModSettlementBuildingProfileSeed.MARKET,
                    1 + governanceBoost + (marketState.marketCount() - marketState.openMarketCount()),
                    governedSettlement,
                    claimedSettlement,
                    List.of("closed_market_capacity", marketState.readySellerDispatchCount() > 0 ? "seller_ready" : "seller_missing")
            );
        }

        BannerModSettlementProjectCandidateSeed foodCandidate = buildProfilePressureCandidate(
                "food_capacity_growth",
                BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION,
                desiredGoodsById.getOrDefault("food", 0),
                profileCounts.getOrDefault(BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION, 0),
                governedSettlement,
                claimedSettlement,
                governanceBoost,
                List.of("food_demand", stockpileSummary.authoredStorageTypeIds().contains("farmers") ? "storage_type:farmers" : "storage_type:generic")
        );
        if (foodCandidate.priority() > 0) {
            return foodCandidate;
        }

        BannerModSettlementProjectCandidateSeed materialCandidate = buildProfilePressureCandidate(
                "material_capacity_growth",
                BannerModSettlementBuildingProfileSeed.MATERIAL_PRODUCTION,
                desiredGoodsById.getOrDefault("materials", 0),
                profileCounts.getOrDefault(BannerModSettlementBuildingProfileSeed.MATERIAL_PRODUCTION, 0),
                governedSettlement,
                claimedSettlement,
                governanceBoost,
                List.of("materials_demand")
        );
        if (materialCandidate.priority() > 0) {
            return materialCandidate;
        }

        BannerModSettlementProjectCandidateSeed constructionCandidate = buildProfilePressureCandidate(
                "construction_capacity_growth",
                BannerModSettlementBuildingProfileSeed.CONSTRUCTION,
                desiredGoodsById.getOrDefault("construction_materials", 0),
                profileCounts.getOrDefault(BannerModSettlementBuildingProfileSeed.CONSTRUCTION, 0),
                governedSettlement,
                claimedSettlement,
                governanceBoost,
                List.of("construction_demand")
        );
        if (constructionCandidate.priority() > 0) {
            return constructionCandidate;
        }

        return new BannerModSettlementProjectCandidateSeed(
                "none",
                null,
                0,
                governedSettlement,
                claimedSettlement,
                List.of()
        );
    }

    static BannerModSettlementMarketState applySellerDispatchSeed(BannerModSettlementMarketState marketState,
                                                                  List<BannerModSettlementResidentRecord> residents,
                                                                  List<BannerModSettlementBuildingRecord> buildings) {
        if (marketState.markets().isEmpty() || residents.isEmpty() || buildings.isEmpty()) {
            return new BannerModSettlementMarketState(
                    marketState.marketCount(),
                    marketState.openMarketCount(),
                    marketState.totalStorageSlots(),
                    marketState.freeStorageSlots(),
                    0,
                    0,
                    marketState.markets(),
                    List.of()
            );
        }

        Map<UUID, BannerModSettlementBuildingRecord> buildingsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            buildingsByUuid.put(building.buildingUuid(), building);
        }
        Map<UUID, BannerModSettlementMarketRecord> marketsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementMarketRecord market : marketState.markets()) {
            marketsByUuid.put(market.buildingUuid(), market);
        }

        List<BannerModSettlementSellerDispatchRecord> sellerDispatches = new ArrayList<>();
        int readySellerDispatchCount = 0;
        for (BannerModSettlementResidentRecord resident : residents) {
            BannerModSettlementResidentServiceContract serviceContract = resident.serviceContract();
            if (serviceContract.actorState() != BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE
                    || serviceContract.serviceBuildingUuid() == null) {
                continue;
            }

            BannerModSettlementBuildingRecord serviceBuilding = buildingsByUuid.get(serviceContract.serviceBuildingUuid());
            if (serviceBuilding == null || serviceBuilding.buildingProfileSeed() != BannerModSettlementBuildingProfileSeed.MARKET) {
                continue;
            }

            BannerModSettlementMarketRecord market = marketsByUuid.get(serviceBuilding.buildingUuid());
            if (market == null) {
                continue;
            }

            BannerModSettlementSellerDispatchState dispatchState = market.open()
                    ? BannerModSettlementSellerDispatchState.READY
                    : BannerModSettlementSellerDispatchState.MARKET_CLOSED;
            if (dispatchState == BannerModSettlementSellerDispatchState.READY) {
                readySellerDispatchCount++;
            }
            sellerDispatches.add(new BannerModSettlementSellerDispatchRecord(
                    resident.residentUuid(),
                    market.buildingUuid(),
                    market.marketName(),
                    dispatchState
            ));
        }

        return new BannerModSettlementMarketState(
                marketState.marketCount(),
                marketState.openMarketCount(),
                marketState.totalStorageSlots(),
                marketState.freeStorageSlots(),
                sellerDispatches.size(),
                readySellerDispatchCount,
                marketState.markets(),
                sellerDispatches
        );
    }

    private static BannerModSettlementMarketState collectMarketState(ServerLevel level,
                                                                     RecruitsClaim claim) {
        List<BannerModSettlementMarketRecord> markets = new ArrayList<>();
        for (MarketArea marketArea : collectWorkAreas(level, claim, MarketArea.class)) {
            marketArea.scanContainers();
            markets.add(new BannerModSettlementMarketRecord(
                    marketArea.getUUID(),
                    marketArea.getMarketName(),
                    marketArea.isOpen(),
                    marketArea.getTotalSlots(),
                    marketArea.getFreeSlots()
            ));
        }
        return summarizeMarketState(markets);
    }

    private static List<StorageArea> collectStorageAreas(ServerLevel level,
                                                          RecruitsClaim claim) {
        return collectWorkAreas(level, claim, StorageArea.class);
    }

    private static <T extends AbstractWorkAreaEntity> List<T> collectWorkAreas(ServerLevel level,
                                                                               RecruitsClaim claim,
                                                                               Class<T> type) {
        WorkAreaIndex index = WorkAreaIndex.instance();
        if (index.sizeFor(level.dimension()) > 0) {
            return index.queryInChunks(level, claim.getClaimedChunks(), type).stream()
                    .filter(entity -> claim.containsChunk(entity.chunkPosition()))
                    .toList();
        }
        RuntimeProfilingCounters.increment("work_area.index.fallback_scans");
        return level.getEntitiesOfClass(type, claimBounds(level, claim), entity -> entity.isAlive() && claim.containsChunk(entity.chunkPosition()));
    }

    private static List<BannerModSeaTradeEntrypoint> collectLiveSeaTradeEntrypoints(List<StorageArea> storageAreas) {
        return BannerModLogisticsRuntime.listSeaTradeEntrypoints(storageAreas);
    }

    private static List<BannerModLogisticsRoute> collectLocalLogisticsRoutes(List<StorageArea> storageAreas) {
        return storageAreas.stream()
                .map(StorageArea::getAuthoredLogisticsRoute)
                .flatMap(Optional::stream)
                .toList();
    }

    private static StockpileSeed resolveStockpileSeed(AbstractWorkAreaEntity workArea) {
        if (!(workArea instanceof StorageArea storageArea)) {
            return StockpileSeed.empty();
        }

        storageArea.scanStorageBlocks();
        int slotCapacity = 0;
        for (var container : storageArea.storageMap.values()) {
            slotCapacity += Math.max(0, container.getContainerSize());
        }
        List<String> typeIds = storageArea.getStorageTypes().stream()
                .map(type -> type.name().toLowerCase(Locale.ROOT))
                .sorted()
                .toList();
        return new StockpileSeed(
                true,
                storageArea.storageMap.size(),
                slotCapacity,
                storageArea.getAuthoredLogisticsRoute().isPresent(),
                storageArea.isPortEntrypoint(),
                typeIds
        );
    }

    private static void addDesiredGoodDriver(Map<String, Integer> desiredGoods, String desiredGoodId, int driverCount) {
        if (desiredGoodId == null || desiredGoodId.isBlank() || driverCount <= 0) {
            return;
        }
        desiredGoods.merge(desiredGoodId, driverCount, Integer::sum);
    }

    private static int resolveSupplyCoverageUnits(String goodId,
                                                  BannerModSettlementStockpileSummary stockpileSummary,
                                                  BannerModSettlementMarketState marketState,
                                                  Map<String, Integer> serviceCoverageByGood) {
        int coverageUnits = serviceCoverageByGood.getOrDefault(goodId, 0);
        if (goodId == null || goodId.isBlank()) {
            return coverageUnits;
        }
        if (goodId.startsWith("storage_type:")) {
            String storageTypeId = goodId.substring("storage_type:".length());
            if (stockpileSummary.authoredStorageTypeIds().contains(storageTypeId)) {
                coverageUnits++;
            }
            return coverageUnits;
        }
        return switch (goodId) {
            case "market_goods" -> coverageUnits + marketState.readySellerDispatchCount();
            case "trade_stock" -> coverageUnits + marketState.openMarketCount() + stockpileSummary.portEntrypointCount();
            default -> coverageUnits;
        };
    }

    private static String desiredGoodIdForProfile(BannerModSettlementBuildingProfileSeed profileSeed) {
        return switch (profileSeed) {
            case FOOD_PRODUCTION -> "food";
            case MATERIAL_PRODUCTION -> "materials";
            case CONSTRUCTION -> "construction_materials";
            case MARKET -> "market_goods";
            default -> "";
        };
    }

    private static BannerModSettlementProjectCandidateSeed buildProfilePressureCandidate(String candidateId,
                                                                                          BannerModSettlementBuildingProfileSeed targetProfileSeed,
                                                                                          int desiredCount,
                                                                                         int currentCount,
                                                                                         boolean governedSettlement,
                                                                                         boolean claimedSettlement,
                                                                                         int governanceBoost,
                                                                                         List<String> driverIds) {
        int pressure = desiredCount - currentCount;
        if (pressure <= 0) {
            return BannerModSettlementProjectCandidateSeed.empty();
        }
        return new BannerModSettlementProjectCandidateSeed(
                candidateId,
                targetProfileSeed,
                Math.min(5, governanceBoost + pressure),
                governedSettlement,
                claimedSettlement,
                driverIds
        );
    }

    static ReservationSignalSeed summarizeReservationSignalSeed(List<BannerModSettlementBuildingRecord> buildings,
                                                                List<BannerModLogisticsRoute> localRoutes,
                                                                List<BannerModLogisticsReservation> reservations) {
        if (buildings.isEmpty() || localRoutes.isEmpty() || reservations.isEmpty()) {
            return ReservationSignalSeed.empty();
        }

        Map<UUID, BannerModSettlementBuildingRecord> buildingsByUuid = new LinkedHashMap<>();
        for (BannerModSettlementBuildingRecord building : buildings) {
            buildingsByUuid.put(building.buildingUuid(), building);
        }

        Map<UUID, BannerModLogisticsRoute> routesById = new LinkedHashMap<>();
        for (BannerModLogisticsRoute route : localRoutes) {
            routesById.put(route.routeId(), route);
        }

        Map<String, Integer> reservationHintUnitsByGood = new LinkedHashMap<>();
        int activeReservationCount = 0;
        int reservedUnitCount = 0;
        for (BannerModLogisticsReservation reservation : reservations) {
            BannerModLogisticsRoute route = routesById.get(reservation.routeId());
            if (route == null) {
                continue;
            }

            BannerModSettlementBuildingRecord sourceBuilding = buildingsByUuid.get(route.source().storageAreaId());
            BannerModSettlementBuildingRecord destinationBuilding = buildingsByUuid.get(route.destination().storageAreaId());
            activeReservationCount++;
            reservedUnitCount += reservation.reservedCount();

            Set<String> goodIds = new LinkedHashSet<>();
            collectReservationGoodIds(goodIds, sourceBuilding);
            collectReservationGoodIds(goodIds, destinationBuilding);
            if (isMerchantStockpile(sourceBuilding) || isMerchantStockpile(destinationBuilding)) {
                goodIds.add("market_goods");
            }
            if (isPortEntrypoint(sourceBuilding) || isPortEntrypoint(destinationBuilding)) {
                goodIds.add("trade_stock");
            }

            for (String goodId : goodIds) {
                reservationHintUnitsByGood.merge(goodId, reservation.reservedCount(), Integer::sum);
            }
        }

        return new ReservationSignalSeed(activeReservationCount, reservedUnitCount, reservationHintUnitsByGood);
    }

    private static void collectReservationGoodIds(Set<String> goodIds,
                                                  @Nullable BannerModSettlementBuildingRecord building) {
        if (building == null) {
            return;
        }
        for (String stockpileTypeId : building.stockpileTypeIds()) {
            if (stockpileTypeId != null && !stockpileTypeId.isBlank()) {
                goodIds.add("storage_type:" + stockpileTypeId);
            }
        }
    }

    private static boolean isMerchantStockpile(@Nullable BannerModSettlementBuildingRecord building) {
        return building != null && building.stockpileTypeIds().contains("merchants");
    }

    private static boolean isPortEntrypoint(@Nullable BannerModSettlementBuildingRecord building) {
        return building != null && building.stockpilePortEntrypoint();
    }

    private record StockpileSeed(
            boolean stockpileBuilding,
            int containerCount,
            int slotCapacity,
            boolean routeAuthored,
            boolean portEntrypoint,
            List<String> typeIds
    ) {
        private static StockpileSeed empty() {
            return new StockpileSeed(false, 0, 0, false, false, List.of());
        }
    }

    record ReservationSignalSeed(
            int activeReservationCount,
            int reservedUnitCount,
            Map<String, Integer> reservationHintUnitsByGood
    ) {
        ReservationSignalSeed {
            activeReservationCount = Math.max(0, activeReservationCount);
            reservedUnitCount = Math.max(0, reservedUnitCount);
            reservationHintUnitsByGood = Map.copyOf(reservationHintUnitsByGood == null ? Map.of() : reservationHintUnitsByGood);
        }

        static ReservationSignalSeed empty() {
            return new ReservationSignalSeed(0, 0, Map.of());
        }
    }

    private static String resolveBuildingTypeId(AbstractWorkAreaEntity workArea) {
        ResourceLocation typeKey = ForgeRegistries.ENTITY_TYPES.getKey(workArea.getType());
        return typeKey == null ? workArea.getType().toString() : typeKey.toString();
    }

    private static ChunkPos resolveAnchorChunk(RecruitsClaim claim) {
        if (claim.getCenter() != null) {
            return claim.getCenter();
        }
        if (!claim.getClaimedChunks().isEmpty()) {
            return claim.getClaimedChunks().get(0);
        }
        return new ChunkPos(0, 0);
    }

    private static AABB claimBounds(ServerLevel level, RecruitsClaim claim) {
        ChunkPos anchor = resolveAnchorChunk(claim);
        int minChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).min().orElse(anchor.x);
        int maxChunkX = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.x).max().orElse(anchor.x);
        int minChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).min().orElse(anchor.z);
        int maxChunkZ = claim.getClaimedChunks().stream().mapToInt(chunkPos -> chunkPos.z).max().orElse(anchor.z);
        return new AABB(
                minChunkX * 16.0D,
                level.getMinBuildHeight(),
                minChunkZ * 16.0D,
                (maxChunkX + 1) * 16.0D,
                level.getMaxBuildHeight(),
                (maxChunkZ + 1) * 16.0D
        );
    }
}
