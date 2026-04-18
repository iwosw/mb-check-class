package com.talhanation.bannermod.shared.logistics;

import java.util.Objects;

public record BannerModCourierTask(BannerModLogisticsRoute route,
                                   BannerModLogisticsReservation reservation) {

    public BannerModCourierTask {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(reservation, "reservation");
    }
}
