package com.talhanation.bannermod.logistics;

import net.minecraft.core.BlockPos;

import java.util.UUID;

@Deprecated(forRemoval = false)
public record BannerModCourierTask(
        UUID reservationId,
        UUID sourceEndpointId,
        UUID destinationEndpointId,
        BlockPos destinationPos,
        String itemId,
        int itemCount,
        String blockedReasonToken
) {
    static BannerModCourierTask fromShared(com.talhanation.bannerlord.shared.logistics.BannerModCourierTask task) {
        return new BannerModCourierTask(task.reservationId(), task.sourceEndpointId(), task.destinationEndpointId(), task.destinationPos(), task.itemId(), task.itemCount(), task.blockedReasonToken());
    }

    com.talhanation.bannerlord.shared.logistics.BannerModCourierTask toShared() {
        return new com.talhanation.bannerlord.shared.logistics.BannerModCourierTask(this.reservationId, this.sourceEndpointId, this.destinationEndpointId, this.destinationPos, this.itemId, this.itemCount, this.blockedReasonToken);
    }
}
