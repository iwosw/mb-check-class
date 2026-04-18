package com.talhanation.bannermod.shared.logistics;

import java.util.Objects;
import java.util.UUID;

public record BannerModLogisticsRoute(UUID routeId,
                                      BannerModLogisticsNodeRef source,
                                      BannerModLogisticsNodeRef destination,
                                      BannerModLogisticsItemFilter filter,
                                      int requestedCount,
                                      BannerModLogisticsPriority priority) {

    public BannerModLogisticsRoute {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(priority, "priority");
        if (requestedCount <= 0) {
            throw new IllegalArgumentException("requestedCount must be > 0");
        }
    }
}
