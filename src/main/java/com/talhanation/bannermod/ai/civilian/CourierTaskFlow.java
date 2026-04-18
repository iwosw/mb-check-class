package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.shared.logistics.BannerModCourierTask;

import java.util.Objects;
import java.util.UUID;

public final class CourierTaskFlow {

    private CourierTaskFlow() {
    }

    public static int missingPickupCount(BannerModCourierTask task, int carriedCount) {
        Objects.requireNonNull(task, "task");
        return Math.max(0, task.reservation().reservedCount() - Math.max(0, carriedCount));
    }

    public static boolean pickupPending(BannerModCourierTask task, int carriedCount) {
        return missingPickupCount(task, carriedCount) > 0;
    }

    public static UUID targetStorageAreaId(BannerModCourierTask task, int carriedCount) {
        Objects.requireNonNull(task, "task");
        if (pickupPending(task, carriedCount)) {
            return task.route().source().storageAreaId();
        }
        return task.route().destination().storageAreaId();
    }
}
