package com.talhanation.bannermod.settlement.workorder;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.UUID;

public record SettlementWorkOrderExecutionReceipt(
        UUID orderUuid,
        UUID claimUuid,
        UUID buildingUuid,
        SettlementWorkOrderType type,
        @Nullable BlockPos targetPos,
        @Nullable UUID claimedByResidentUuid,
        long completedGameTime
) {
    public SettlementWorkOrderExecutionReceipt {
        if (orderUuid == null) {
            throw new IllegalArgumentException("orderUuid must not be null");
        }
        if (claimUuid == null) {
            throw new IllegalArgumentException("claimUuid must not be null");
        }
        if (buildingUuid == null) {
            throw new IllegalArgumentException("buildingUuid must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        completedGameTime = Math.max(0L, completedGameTime);
    }

    static SettlementWorkOrderExecutionReceipt from(SettlementWorkOrder order, long completedGameTime) {
        return new SettlementWorkOrderExecutionReceipt(
                order.orderUuid(),
                order.claimUuid(),
                order.buildingUuid(),
                order.type(),
                order.targetPos(),
                order.claimedByResidentUuid(),
                completedGameTime
        );
    }
}
