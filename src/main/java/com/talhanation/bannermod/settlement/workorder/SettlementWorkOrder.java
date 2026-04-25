package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One concrete piece of work a building wants a resident to perform.
 *
 * <p>Records are immutable. The runtime transitions between records by replacing the entry
 * in its internal map (copy-on-write) instead of mutating fields.</p>
 */
public record SettlementWorkOrder(
        UUID orderUuid,
        UUID claimUuid,
        UUID buildingUuid,
        SettlementWorkOrderType type,
        @Nullable BlockPos targetPos,
        @Nullable String resourceHintId,
        int priority,
        long createdGameTime,
        SettlementWorkOrderStatus status,
        @Nullable UUID claimedByResidentUuid,
        long claimExpiryGameTime
) {
    public SettlementWorkOrder {
        Objects.requireNonNull(orderUuid, "orderUuid");
        Objects.requireNonNull(claimUuid, "claimUuid");
        Objects.requireNonNull(buildingUuid, "buildingUuid");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(status, "status");
    }

    public Optional<BlockPos> target() {
        return Optional.ofNullable(targetPos);
    }

    public Optional<UUID> claimedBy() {
        return Optional.ofNullable(claimedByResidentUuid);
    }

    public SettlementWorkOrder withStatus(SettlementWorkOrderStatus newStatus,
                                           @Nullable UUID newClaimant,
                                           long newExpiryGameTime) {
        return new SettlementWorkOrder(
                orderUuid,
                claimUuid,
                buildingUuid,
                type,
                targetPos,
                resourceHintId,
                priority,
                createdGameTime,
                newStatus,
                newClaimant,
                newExpiryGameTime
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("OrderUuid", this.orderUuid);
        tag.putUUID("ClaimUuid", this.claimUuid);
        tag.putUUID("BuildingUuid", this.buildingUuid);
        tag.putString("Type", this.type.name());
        if (this.targetPos != null) {
            tag.putLong("TargetPos", this.targetPos.asLong());
        }
        if (this.resourceHintId != null) {
            tag.putString("ResourceHintId", this.resourceHintId);
        }
        tag.putInt("Priority", this.priority);
        tag.putLong("CreatedGameTime", this.createdGameTime);
        tag.putString("Status", this.status.name());
        if (this.claimedByResidentUuid != null) {
            tag.putUUID("ClaimedByResidentUuid", this.claimedByResidentUuid);
        }
        tag.putLong("ClaimExpiryGameTime", this.claimExpiryGameTime);
        return tag;
    }

    public static SettlementWorkOrder fromTag(CompoundTag tag) {
        return new SettlementWorkOrder(
                tag.getUUID("OrderUuid"),
                tag.getUUID("ClaimUuid"),
                tag.getUUID("BuildingUuid"),
                typeFromTagName(tag.getString("Type")),
                tag.contains("TargetPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("TargetPos")) : null,
                tag.contains("ResourceHintId", Tag.TAG_STRING) ? tag.getString("ResourceHintId") : null,
                tag.getInt("Priority"),
                tag.getLong("CreatedGameTime"),
                statusFromTagName(tag.getString("Status")),
                tag.hasUUID("ClaimedByResidentUuid") ? tag.getUUID("ClaimedByResidentUuid") : null,
                tag.getLong("ClaimExpiryGameTime")
        );
    }

    private static SettlementWorkOrderType typeFromTagName(String name) {
        try {
            return SettlementWorkOrderType.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return SettlementWorkOrderType.HAUL_RESOURCE;
        }
    }

    private static SettlementWorkOrderStatus statusFromTagName(String name) {
        try {
            return SettlementWorkOrderStatus.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return SettlementWorkOrderStatus.PENDING;
        }
    }

    public static SettlementWorkOrder pending(UUID claimUuid,
                                              UUID buildingUuid,
                                              SettlementWorkOrderType type,
                                              @Nullable BlockPos targetPos,
                                              @Nullable String resourceHintId,
                                              int priority,
                                              long createdGameTime) {
        return new SettlementWorkOrder(
                UUID.randomUUID(),
                claimUuid,
                buildingUuid,
                type,
                targetPos,
                resourceHintId,
                priority,
                createdGameTime,
                SettlementWorkOrderStatus.PENDING,
                null,
                0L
        );
    }
}
