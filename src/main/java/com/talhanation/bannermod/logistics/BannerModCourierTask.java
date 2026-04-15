package com.talhanation.bannermod.logistics;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public record BannerModCourierTask(
        UUID reservationId,
        UUID sourceEndpointId,
        UUID destinationEndpointId,
        BlockPos destinationPos,
        String itemId,
        int itemCount,
        String blockedReasonToken
) {
}
