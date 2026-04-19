package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerModSettlementResidentRecordTest {

    @Test
    void residentRecordRoundTripsScheduleSeed() {
        BannerModSettlementResidentRecord original = new BannerModSettlementResidentRecord(
                UUID.randomUUID(),
                BannerModSettlementResidentRole.CONTROLLED_WORKER,
                BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK,
                BannerModSettlementResidentScheduleWindowSeed.LABOR_DAY,
                BannerModSettlementResidentRuntimeRoleSeed.LOCAL_LABOR,
                new BannerModSettlementResidentServiceContract(BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE, UUID.randomUUID(), "bannermod:crop_area"),
                new BannerModSettlementResidentJobDefinition(BannerModSettlementJobHandlerSeed.LOCAL_BUILDING_LABOR, UUID.randomUUID(), "bannermod:crop_area", BannerModSettlementBuildingCategory.FOOD, BannerModSettlementBuildingProfileSeed.FOOD_PRODUCTION),
                new BannerModSettlementResidentJobTargetSelectionSeed(BannerModSettlementJobTargetSelectionMode.SERVICE_BUILDING, null, null),
                BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER,
                UUID.randomUUID(),
                "blueguild",
                UUID.randomUUID(),
                BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING,
                BannerModSettlementResidentRoleProfile.defaultFor(
                        BannerModSettlementResidentRole.CONTROLLED_WORKER,
                        BannerModSettlementResidentRuntimeRoleSeed.LOCAL_LABOR,
                        BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER,
                        BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING
                )
        );

        BannerModSettlementResidentRecord restored = BannerModSettlementResidentRecord.fromTag(original.toTag());

        assertEquals(original, restored);
    }

    @Test
    void residentRecordDefaultsLegacyScheduleSeedsWhenMissing() {
        UUID workerUuid = UUID.randomUUID();
        UUID workAreaUuid = UUID.randomUUID();
        CompoundTag workerTag = new CompoundTag();
        workerTag.putUUID("ResidentUuid", workerUuid);
        workerTag.putString("Role", BannerModSettlementResidentRole.CONTROLLED_WORKER.name());
        workerTag.putUUID("OwnerUuid", UUID.randomUUID());
        workerTag.putUUID("BoundWorkAreaUuid", workAreaUuid);

        BannerModSettlementResidentRecord worker = BannerModSettlementResidentRecord.fromTag(workerTag);

        assertEquals(BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK, worker.scheduleSeed());
        assertEquals(BannerModSettlementResidentScheduleWindowSeed.LABOR_DAY, worker.scheduleWindowSeed());
        assertEquals(BannerModSettlementResidentRuntimeRoleSeed.LOCAL_LABOR, worker.runtimeRoleSeed());
        assertEquals("projected_local_labor", worker.roleProfile().profileId());
        assertEquals("labor", worker.roleProfile().goalDomainId());
        assertEquals(true, worker.roleProfile().prefersLocalBuilding());
        assertEquals(BannerModSettlementResidentSchedulePolicySeed.LOCAL_LABOR_DAY, worker.schedulePolicy().policySeed());
        assertEquals(BannerModSettlementResidentScheduleSeed.ASSIGNED_WORK, worker.schedulePolicy().scheduleSeed());
        assertEquals(BannerModSettlementResidentScheduleWindowSeed.LABOR_DAY, worker.schedulePolicy().scheduleWindowSeed());
        assertEquals("labor", worker.schedulePolicy().goalDomainId());
        assertEquals(true, worker.schedulePolicy().prefersLocalBuilding());
        assertEquals(BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE, worker.serviceContract().actorState());
        assertEquals(workAreaUuid, worker.serviceContract().serviceBuildingUuid());
        assertEquals(BannerModSettlementJobHandlerSeed.LOCAL_BUILDING_LABOR, worker.jobDefinition().handlerSeed());
        assertEquals(workAreaUuid, worker.jobDefinition().targetBuildingUuid());
        assertEquals(BannerModSettlementJobTargetSelectionMode.SERVICE_BUILDING, worker.jobTargetSelectionSeed().selectionMode());
        assertEquals(BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER, worker.residentMode());
        assertEquals(BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING, worker.assignmentState());

        CompoundTag governorTag = new CompoundTag();
        governorTag.putUUID("ResidentUuid", UUID.randomUUID());
        governorTag.putString("Role", BannerModSettlementResidentRole.GOVERNOR_RECRUIT.name());

        BannerModSettlementResidentRecord governor = BannerModSettlementResidentRecord.fromTag(governorTag);

        assertEquals(BannerModSettlementResidentScheduleSeed.GOVERNING, governor.scheduleSeed());
        assertEquals(BannerModSettlementResidentScheduleWindowSeed.CIVIC_DAY, governor.scheduleWindowSeed());
        assertEquals(BannerModSettlementResidentRuntimeRoleSeed.GOVERNANCE, governor.runtimeRoleSeed());
        assertEquals("governance", governor.roleProfile().profileId());
        assertEquals("governance", governor.roleProfile().goalDomainId());
        assertEquals(BannerModSettlementResidentSchedulePolicySeed.GOVERNANCE_CIVIC, governor.schedulePolicy().policySeed());
        assertEquals(BannerModSettlementResidentScheduleWindowSeed.CIVIC_DAY, governor.schedulePolicy().scheduleWindowSeed());
        assertEquals(BannerModSettlementServiceActorState.NOT_SERVICE_ACTOR, governor.serviceContract().actorState());
        assertEquals(BannerModSettlementJobHandlerSeed.GOVERNANCE, governor.jobDefinition().handlerSeed());
        assertEquals(BannerModSettlementJobTargetSelectionMode.NONE, governor.jobTargetSelectionSeed().selectionMode());
        assertEquals(BannerModSettlementResidentMode.SETTLEMENT_RESIDENT, governor.residentMode());
        assertEquals(BannerModSettlementResidentAssignmentState.NOT_APPLICABLE, governor.assignmentState());

        CompoundTag unownedWorkerTag = new CompoundTag();
        unownedWorkerTag.putUUID("ResidentUuid", UUID.randomUUID());
        unownedWorkerTag.putString("Role", BannerModSettlementResidentRole.CONTROLLED_WORKER.name());

        BannerModSettlementResidentRecord unownedWorker = BannerModSettlementResidentRecord.fromTag(unownedWorkerTag);

        assertEquals(BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX, unownedWorker.scheduleWindowSeed());
        assertEquals(BannerModSettlementResidentRuntimeRoleSeed.FLOATING_LABOR, unownedWorker.runtimeRoleSeed());
        assertEquals("projected_floating_labor", unownedWorker.roleProfile().profileId());
        assertEquals("labor", unownedWorker.roleProfile().goalDomainId());
        assertEquals(BannerModSettlementResidentSchedulePolicySeed.FLOATING_LABOR_FLEX, unownedWorker.schedulePolicy().policySeed());
        assertEquals(BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX, unownedWorker.schedulePolicy().scheduleWindowSeed());
        assertEquals(BannerModSettlementServiceActorState.FLOATING_SERVICE, unownedWorker.serviceContract().actorState());
        assertEquals(BannerModSettlementJobHandlerSeed.FLOATING_LABOR_POOL, unownedWorker.jobDefinition().handlerSeed());
        assertEquals(BannerModSettlementJobTargetSelectionMode.FLOATING_LABOR_POOL, unownedWorker.jobTargetSelectionSeed().selectionMode());
        assertEquals(BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER, unownedWorker.residentMode());
        assertEquals(BannerModSettlementResidentAssignmentState.UNASSIGNED, unownedWorker.assignmentState());
    }

    @Test
    void residentRecordFallsBackForUnknownScheduleWindowSeed() {
        CompoundTag residentTag = new CompoundTag();
        residentTag.putUUID("ResidentUuid", UUID.randomUUID());
        residentTag.putString("Role", BannerModSettlementResidentRole.VILLAGER.name());
        residentTag.putString("ScheduleSeed", BannerModSettlementResidentScheduleSeed.SETTLEMENT_IDLE.name());
        residentTag.putString("RuntimeRoleSeed", BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE.name());
        residentTag.putString("ScheduleWindowSeed", "NOT_A_REAL_WINDOW");

        BannerModSettlementResidentRecord resident = BannerModSettlementResidentRecord.fromTag(residentTag);

        assertEquals(BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX, resident.scheduleWindowSeed());
        assertEquals(BannerModSettlementResidentSchedulePolicySeed.VILLAGE_LIFE_FLEX, resident.schedulePolicy().policySeed());
    }
}
