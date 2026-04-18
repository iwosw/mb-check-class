package com.talhanation.bannermod.shared.logistics;

public enum BannerModLogisticsBlockedReason {
    SOURCE_SHORTAGE("logistics_source_shortage"),
    SOURCE_CONTAINER_MISSING("logistics_source_container_missing"),
    DESTINATION_CONTAINER_MISSING("logistics_destination_container_missing"),
    DESTINATION_FULL("logistics_destination_full"),
    RESERVATION_TIMEOUT("logistics_reservation_timeout");

    private final String reasonToken;

    BannerModLogisticsBlockedReason(String reasonToken) {
        this.reasonToken = reasonToken;
    }

    public String reasonToken() {
        return this.reasonToken;
    }
}
