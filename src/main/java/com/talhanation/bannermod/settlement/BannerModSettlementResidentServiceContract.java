package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.UUID;

public record BannerModSettlementResidentServiceContract(
        BannerModSettlementServiceActorState actorState,
        @Nullable UUID serviceBuildingUuid,
        @Nullable String serviceBuildingTypeId
) {
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ActorState", this.actorState.name());
        if (this.serviceBuildingUuid != null) {
            tag.putUUID("ServiceBuildingUuid", this.serviceBuildingUuid);
        }
        if (this.serviceBuildingTypeId != null && !this.serviceBuildingTypeId.isBlank()) {
            tag.putString("ServiceBuildingTypeId", this.serviceBuildingTypeId);
        }
        return tag;
    }

    public static BannerModSettlementResidentServiceContract fromTag(CompoundTag tag) {
        BannerModSettlementServiceActorState actorState = tag.contains("ActorState", Tag.TAG_STRING)
                ? BannerModSettlementServiceActorState.fromTagName(tag.getString("ActorState"))
                : BannerModSettlementServiceActorState.NOT_SERVICE_ACTOR;
        UUID serviceBuildingUuid = tag.hasUUID("ServiceBuildingUuid") ? tag.getUUID("ServiceBuildingUuid") : null;
        String serviceBuildingTypeId = tag.contains("ServiceBuildingTypeId", Tag.TAG_STRING)
                ? tag.getString("ServiceBuildingTypeId")
                : null;
        return new BannerModSettlementResidentServiceContract(actorState, serviceBuildingUuid, serviceBuildingTypeId);
    }

    public static BannerModSettlementResidentServiceContract defaultFor(BannerModSettlementResidentRole role,
                                                                        BannerModSettlementResidentMode residentMode,
                                                                        BannerModSettlementResidentAssignmentState assignmentState,
                                                                        @Nullable UUID boundWorkAreaUuid,
                                                                        @Nullable String serviceBuildingTypeId) {
        if (role != BannerModSettlementResidentRole.CONTROLLED_WORKER
                || residentMode != BannerModSettlementResidentMode.PROJECTED_CONTROLLED_WORKER) {
            return notServiceActor();
        }
        return switch (assignmentState) {
            case ASSIGNED_LOCAL_BUILDING -> new BannerModSettlementResidentServiceContract(
                    BannerModSettlementServiceActorState.LOCAL_BUILDING_SERVICE,
                    boundWorkAreaUuid,
                    serviceBuildingTypeId
            );
            case ASSIGNED_MISSING_BUILDING -> new BannerModSettlementResidentServiceContract(
                    BannerModSettlementServiceActorState.ORPHANED_SERVICE,
                    boundWorkAreaUuid,
                    null
            );
            case UNASSIGNED -> new BannerModSettlementResidentServiceContract(
                    BannerModSettlementServiceActorState.FLOATING_SERVICE,
                    null,
                    null
            );
            default -> notServiceActor();
        };
    }

    public static BannerModSettlementResidentServiceContract notServiceActor() {
        return new BannerModSettlementResidentServiceContract(BannerModSettlementServiceActorState.NOT_SERVICE_ACTOR, null, null);
    }
}
