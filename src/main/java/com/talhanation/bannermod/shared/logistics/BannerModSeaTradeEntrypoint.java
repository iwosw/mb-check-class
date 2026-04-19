package com.talhanation.bannermod.shared.logistics;

import java.util.Objects;
import java.util.UUID;

public record BannerModSeaTradeEntrypoint(UUID routeId,
                                          UUID portStorageAreaId,
                                          UUID settlementStorageAreaId,
                                          BannerModSeaTradeDirection direction,
                                          BannerModLogisticsItemFilter filter,
                                          int requestedCount,
                                          BannerModLogisticsPriority priority) {

    public BannerModSeaTradeEntrypoint {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(portStorageAreaId, "portStorageAreaId");
        Objects.requireNonNull(settlementStorageAreaId, "settlementStorageAreaId");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(priority, "priority");
        if (requestedCount <= 0) {
            throw new IllegalArgumentException("requestedCount must be > 0");
        }
    }
}
