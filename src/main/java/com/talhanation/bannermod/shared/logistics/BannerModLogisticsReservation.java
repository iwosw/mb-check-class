package com.talhanation.bannermod.shared.logistics;

import java.util.Objects;
import java.util.UUID;

public record BannerModLogisticsReservation(UUID reservationId,
                                            UUID routeId,
                                            UUID workerId,
                                            BannerModLogisticsItemFilter filter,
                                            int reservedCount,
                                            long expiresAtGameTime) {

    public BannerModLogisticsReservation {
        Objects.requireNonNull(reservationId, "reservationId");
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(workerId, "workerId");
        Objects.requireNonNull(filter, "filter");
        if (reservedCount <= 0) {
            throw new IllegalArgumentException("reservedCount must be > 0");
        }
    }

    public boolean isExpired(long gameTime) {
        return gameTime >= this.expiresAtGameTime;
    }
}
