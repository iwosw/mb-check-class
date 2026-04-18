package com.talhanation.bannermod.shared.logistics;

public enum BannerModLogisticsPriority {
    HIGH(0),
    NORMAL(1),
    LOW(2);

    private final int sortOrder;

    BannerModLogisticsPriority(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int sortOrder() {
        return this.sortOrder;
    }
}
