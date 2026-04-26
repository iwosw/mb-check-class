package com.talhanation.bannermod.settlement.bootstrap;

import java.util.Optional;

public record BootstrapResult(boolean success, String message, SettlementRecord settlement) {
    public BootstrapResult {
        message = message == null ? "" : message;
    }

    public Optional<SettlementRecord> settlementOptional() {
        return Optional.ofNullable(settlement);
    }

    public static BootstrapResult failure(String message) {
        return new BootstrapResult(false, message, null);
    }

    public static BootstrapResult success(String message, SettlementRecord settlement) {
        return new BootstrapResult(true, message, settlement);
    }
}
