package com.talhanation.bannermod.shared.logistics;

import java.util.Objects;
import java.util.UUID;

public record BannerModLogisticsNodeRef(UUID storageAreaId) {

    public BannerModLogisticsNodeRef {
        Objects.requireNonNull(storageAreaId, "storageAreaId");
    }
}
