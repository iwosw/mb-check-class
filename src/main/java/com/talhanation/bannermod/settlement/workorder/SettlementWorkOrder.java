package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;

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
