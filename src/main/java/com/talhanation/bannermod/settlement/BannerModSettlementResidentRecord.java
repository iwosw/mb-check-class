package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.UUID;

public record BannerModSettlementResidentRecord(
        UUID residentUuid,
        BannerModSettlementResidentRole role,
        BannerModSettlementResidentScheduleSeed scheduleSeed,
        BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed,
        BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
        BannerModSettlementResidentServiceContract serviceContract,
        BannerModSettlementResidentJobDefinition jobDefinition,
        BannerModSettlementResidentJobTargetSelectionSeed jobTargetSelectionSeed,
        BannerModSettlementResidentMode residentMode,
        @Nullable UUID ownerUuid,
        @Nullable String teamId,
        @Nullable UUID boundWorkAreaUuid,
        BannerModSettlementResidentAssignmentState assignmentState,
        BannerModSettlementResidentRoleProfile roleProfile,
        BannerModSettlementResidentSchedulePolicy schedulePolicy
) {
    public BannerModSettlementResidentRecord(UUID residentUuid,
                                             BannerModSettlementResidentRole role,
                                             BannerModSettlementResidentScheduleSeed scheduleSeed,
                                             BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed,
                                             BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
                                             BannerModSettlementResidentServiceContract serviceContract,
                                             BannerModSettlementResidentJobDefinition jobDefinition,
                                             BannerModSettlementResidentJobTargetSelectionSeed jobTargetSelectionSeed,
                                             BannerModSettlementResidentMode residentMode,
                                             @Nullable UUID ownerUuid,
                                             @Nullable String teamId,
                                             @Nullable UUID boundWorkAreaUuid,
                                             BannerModSettlementResidentAssignmentState assignmentState,
                                             BannerModSettlementResidentRoleProfile roleProfile) {
        this(
                residentUuid,
                role,
                scheduleSeed,
                scheduleWindowSeed,
                runtimeRoleSeed,
                serviceContract,
                jobDefinition,
                jobTargetSelectionSeed,
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                roleProfile,
                BannerModSettlementResidentSchedulePolicy.defaultFor(scheduleSeed, scheduleWindowSeed, runtimeRoleSeed, roleProfile)
        );
    }

    public BannerModSettlementResidentRecord(UUID residentUuid,
                                             BannerModSettlementResidentRole role,
                                             BannerModSettlementResidentScheduleSeed scheduleSeed,
                                             BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed,
                                             BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
                                             BannerModSettlementResidentServiceContract serviceContract,
                                             BannerModSettlementResidentJobDefinition jobDefinition,
                                             BannerModSettlementResidentJobTargetSelectionSeed jobTargetSelectionSeed,
                                             BannerModSettlementResidentMode residentMode,
                                             @Nullable UUID ownerUuid,
                                             @Nullable String teamId,
                                             @Nullable UUID boundWorkAreaUuid,
                                             BannerModSettlementResidentAssignmentState assignmentState) {
        this(
                residentUuid,
                role,
                scheduleSeed,
                scheduleWindowSeed,
                runtimeRoleSeed,
                serviceContract,
                jobDefinition,
                jobTargetSelectionSeed,
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                BannerModSettlementResidentRoleProfile.defaultFor(role, runtimeRoleSeed, residentMode, assignmentState)
        );
    }

    public BannerModSettlementResidentRecord(UUID residentUuid,
                                             BannerModSettlementResidentRole role,
                                             BannerModSettlementResidentScheduleSeed scheduleSeed,
                                             BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed,
                                             BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
                                             BannerModSettlementResidentServiceContract serviceContract,
                                             BannerModSettlementResidentJobDefinition jobDefinition,
                                             BannerModSettlementResidentMode residentMode,
                                             @Nullable UUID ownerUuid,
                                             @Nullable String teamId,
                                             @Nullable UUID boundWorkAreaUuid,
                                             BannerModSettlementResidentAssignmentState assignmentState) {
        this(
                residentUuid,
                role,
                scheduleSeed,
                scheduleWindowSeed,
                runtimeRoleSeed,
                serviceContract,
                jobDefinition,
                BannerModSettlementResidentJobTargetSelectionSeed.defaultFor(residentUuid, jobDefinition, serviceContract, BannerModSettlementMarketState.empty()),
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                BannerModSettlementResidentRoleProfile.defaultFor(role, runtimeRoleSeed, residentMode, assignmentState)
        );
    }

    public BannerModSettlementResidentRecord(UUID residentUuid,
                                             BannerModSettlementResidentRole role,
                                             BannerModSettlementResidentScheduleSeed scheduleSeed,
                                             BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
                                             BannerModSettlementResidentServiceContract serviceContract,
                                             BannerModSettlementResidentMode residentMode,
                                             @Nullable UUID ownerUuid,
                                             @Nullable String teamId,
                                             @Nullable UUID boundWorkAreaUuid,
                                             BannerModSettlementResidentAssignmentState assignmentState) {
        this(
                residentUuid,
                role,
                scheduleSeed,
                BannerModSettlementResidentScheduleWindowSeed.defaultFor(scheduleSeed, runtimeRoleSeed),
                runtimeRoleSeed,
                serviceContract,
                BannerModSettlementResidentJobDefinition.defaultFor(role, runtimeRoleSeed, serviceContract, null),
                BannerModSettlementResidentJobTargetSelectionSeed.defaultFor(
                        residentUuid,
                        BannerModSettlementResidentJobDefinition.defaultFor(role, runtimeRoleSeed, serviceContract, null),
                        serviceContract,
                        BannerModSettlementMarketState.empty()
                ),
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                BannerModSettlementResidentRoleProfile.defaultFor(role, runtimeRoleSeed, residentMode, assignmentState)
        );
    }

    public BannerModSettlementResidentRecord(UUID residentUuid,
                                             BannerModSettlementResidentRole role,
                                             BannerModSettlementResidentScheduleSeed scheduleSeed,
                                             BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed,
                                             BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed,
                                             BannerModSettlementResidentServiceContract serviceContract,
                                             BannerModSettlementResidentMode residentMode,
                                             @Nullable UUID ownerUuid,
                                             @Nullable String teamId,
                                             @Nullable UUID boundWorkAreaUuid,
                                             BannerModSettlementResidentAssignmentState assignmentState) {
        this(
                residentUuid,
                role,
                scheduleSeed,
                scheduleWindowSeed,
                runtimeRoleSeed,
                serviceContract,
                BannerModSettlementResidentJobDefinition.defaultFor(role, runtimeRoleSeed, serviceContract, null),
                BannerModSettlementResidentJobTargetSelectionSeed.defaultFor(
                        residentUuid,
                        BannerModSettlementResidentJobDefinition.defaultFor(role, runtimeRoleSeed, serviceContract, null),
                        serviceContract,
                        BannerModSettlementMarketState.empty()
                ),
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                BannerModSettlementResidentRoleProfile.defaultFor(role, runtimeRoleSeed, residentMode, assignmentState)
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ResidentUuid", this.residentUuid);
        tag.putString("Role", this.role.name());
        tag.putString("ScheduleSeed", this.scheduleSeed.name());
        tag.putString("ScheduleWindowSeed", this.scheduleWindowSeed.name());
        tag.putString("RuntimeRoleSeed", this.runtimeRoleSeed.name());
        tag.put("ServiceContract", this.serviceContract.toTag());
        tag.put("JobDefinition", this.jobDefinition.toTag());
        tag.put("JobTargetSelectionSeed", this.jobTargetSelectionSeed.toTag());
        tag.putString("ResidentMode", this.residentMode.name());
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUuid", this.ownerUuid);
        }
        if (this.teamId != null && !this.teamId.isBlank()) {
            tag.putString("TeamId", this.teamId);
        }
        if (this.boundWorkAreaUuid != null) {
            tag.putUUID("BoundWorkAreaUuid", this.boundWorkAreaUuid);
        }
        tag.putString("AssignmentState", this.assignmentState.name());
        tag.put("RoleProfile", this.roleProfile.toTag());
        tag.put("SchedulePolicy", this.schedulePolicy.toTag());
        return tag;
    }

    public static BannerModSettlementResidentRecord fromTag(CompoundTag tag) {
        BannerModSettlementResidentRole role = BannerModSettlementResidentRole.fromTagName(tag.getString("Role"));
        UUID ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        String teamId = tag.contains("TeamId", Tag.TAG_STRING) ? tag.getString("TeamId") : null;
        UUID boundWorkAreaUuid = tag.hasUUID("BoundWorkAreaUuid") ? tag.getUUID("BoundWorkAreaUuid") : null;
        BannerModSettlementResidentScheduleSeed scheduleSeed = tag.contains("ScheduleSeed", Tag.TAG_STRING)
                ? BannerModSettlementResidentScheduleSeed.valueOf(tag.getString("ScheduleSeed"))
                : BannerModSettlementResidentScheduleSeed.defaultFor(role, boundWorkAreaUuid);
        BannerModSettlementResidentMode residentMode = tag.contains("ResidentMode", Tag.TAG_STRING)
                ? BannerModSettlementResidentMode.fromTagName(tag.getString("ResidentMode"))
                : BannerModSettlementResidentMode.defaultFor(role, ownerUuid);
        BannerModSettlementResidentAssignmentState assignmentState = tag.contains("AssignmentState", Tag.TAG_STRING)
                ? BannerModSettlementResidentAssignmentState.fromTagName(tag.getString("AssignmentState"))
                : defaultAssignmentState(role, boundWorkAreaUuid);
        BannerModSettlementResidentRuntimeRoleSeed runtimeRoleSeed = tag.contains("RuntimeRoleSeed", Tag.TAG_STRING)
                ? BannerModSettlementResidentRuntimeRoleSeed.fromTagName(tag.getString("RuntimeRoleSeed"))
                : BannerModSettlementResidentRuntimeRoleSeed.defaultFor(role, scheduleSeed, residentMode, assignmentState);
        BannerModSettlementResidentScheduleWindowSeed scheduleWindowSeed = tag.contains("ScheduleWindowSeed", Tag.TAG_STRING)
                ? BannerModSettlementResidentScheduleWindowSeed.fromTagName(tag.getString("ScheduleWindowSeed"))
                : BannerModSettlementResidentScheduleWindowSeed.defaultFor(scheduleSeed, runtimeRoleSeed);
        BannerModSettlementResidentServiceContract serviceContract = tag.contains("ServiceContract", Tag.TAG_COMPOUND)
                ? BannerModSettlementResidentServiceContract.fromTag(tag.getCompound("ServiceContract"))
                : BannerModSettlementResidentServiceContract.defaultFor(role, residentMode, assignmentState, boundWorkAreaUuid, null);
        BannerModSettlementResidentJobDefinition jobDefinition = tag.contains("JobDefinition", Tag.TAG_COMPOUND)
                ? BannerModSettlementResidentJobDefinition.fromTag(tag.getCompound("JobDefinition"))
                : BannerModSettlementResidentJobDefinition.defaultFor(role, runtimeRoleSeed, serviceContract, null);
        BannerModSettlementResidentJobTargetSelectionSeed jobTargetSelectionSeed = tag.contains("JobTargetSelectionSeed", Tag.TAG_COMPOUND)
                ? BannerModSettlementResidentJobTargetSelectionSeed.fromTag(tag.getCompound("JobTargetSelectionSeed"))
                : BannerModSettlementResidentJobTargetSelectionSeed.defaultFor(tag.getUUID("ResidentUuid"), jobDefinition, serviceContract, BannerModSettlementMarketState.empty());
        BannerModSettlementResidentRoleProfile roleProfile = tag.contains("RoleProfile", Tag.TAG_COMPOUND)
                ? BannerModSettlementResidentRoleProfile.fromTag(tag.getCompound("RoleProfile"))
                : BannerModSettlementResidentRoleProfile.defaultFor(role, runtimeRoleSeed, residentMode, assignmentState);
        BannerModSettlementResidentSchedulePolicy schedulePolicy = tag.contains("SchedulePolicy", Tag.TAG_COMPOUND)
                ? BannerModSettlementResidentSchedulePolicy.fromTag(tag.getCompound("SchedulePolicy"))
                : BannerModSettlementResidentSchedulePolicy.defaultFor(scheduleSeed, scheduleWindowSeed, runtimeRoleSeed, roleProfile);
        return new BannerModSettlementResidentRecord(
                tag.getUUID("ResidentUuid"),
                role,
                scheduleSeed,
                scheduleWindowSeed,
                runtimeRoleSeed,
                serviceContract,
                jobDefinition,
                jobTargetSelectionSeed,
                residentMode,
                ownerUuid,
                teamId,
                boundWorkAreaUuid,
                assignmentState,
                roleProfile,
                schedulePolicy
        );
    }

    private static BannerModSettlementResidentAssignmentState defaultAssignmentState(BannerModSettlementResidentRole role,
                                                                                     @Nullable UUID boundWorkAreaUuid) {
        if (role != BannerModSettlementResidentRole.CONTROLLED_WORKER) {
            return BannerModSettlementResidentAssignmentState.NOT_APPLICABLE;
        }
        return boundWorkAreaUuid == null
                ? BannerModSettlementResidentAssignmentState.UNASSIGNED
                : BannerModSettlementResidentAssignmentState.ASSIGNED_LOCAL_BUILDING;
    }
}
