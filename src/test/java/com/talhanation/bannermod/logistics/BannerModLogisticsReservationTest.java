package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.shared.logistics.BannerModLogisticsItemFilter;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsReservation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BannerModLogisticsReservationTest {

    @Test
    void reservationExpiresAtConfiguredGameTime() {
        BannerModLogisticsReservation reservation = new BannerModLogisticsReservation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BannerModLogisticsItemFilter.any(),
                16,
                120L
        );

        assertFalse(reservation.isExpired(119L));
        assertTrue(reservation.isExpired(120L));
        assertTrue(reservation.isExpired(121L));
    }
}
