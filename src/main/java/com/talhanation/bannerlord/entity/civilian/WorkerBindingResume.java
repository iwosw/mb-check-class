package com.talhanation.bannerlord.entity.civilian;

import com.talhanation.bannermod.citizen.CitizenCore;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

public final class WorkerBindingResume {
    public static final int BOUND_AREA_PRIORITY_BOOST = 1_000_000;

    private WorkerBindingResume() {
    }

    public static int priorityBoost(@Nullable UUID boundAreaUuid, @Nullable UUID candidateUuid) {
        if (boundAreaUuid == null || candidateUuid == null) {
            return 0;
        }
        return boundAreaUuid.equals(candidateUuid) ? BOUND_AREA_PRIORITY_BOOST : 0;
    }

    public static int priorityBoostFromCitizen(CitizenCore citizenCore, @Nullable UUID candidateUuid) {
        return priorityBoost(citizenCore == null ? null : citizenCore.getBoundWorkAreaUUID(), candidateUuid);
    }

    public static <T> void prioritizeBoundFirst(java.util.List<T> candidates, @Nullable UUID boundAreaUuid, Function<T, UUID> uuidGetter) {
        if (boundAreaUuid == null || candidates == null || candidates.size() < 2) {
            return;
        }

        candidates.sort((left, right) -> Integer.compare(
                priorityBoost(boundAreaUuid, uuidGetter.apply(right)),
                priorityBoost(boundAreaUuid, uuidGetter.apply(left))
        ));
    }
}
