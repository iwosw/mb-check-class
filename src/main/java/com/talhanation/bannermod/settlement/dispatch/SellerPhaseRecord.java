package com.talhanation.bannermod.settlement.dispatch;

import java.util.UUID;

/**
 * Immutable snapshot of a seller's current dispatch phase. The runtime replaces
 * these wholesale on each transition — keeping the record immutable lets
 * callers hold onto references without worrying about in-place mutation.
 *
 * @param sellerResidentUuid resident UUID of the dispatched seller
 * @param marketRecordUuid   UUID of the market the seller is staffing
 * @param phase              current phase in the dispatch lifecycle
 * @param phaseStartGameTime absolute game time (ticks) when the phase entered
 * @param phaseTickCount     number of advance() ticks observed since entry
 */
public record SellerPhaseRecord(
        UUID sellerResidentUuid,
        UUID marketRecordUuid,
        SellerPhase phase,
        long phaseStartGameTime,
        int phaseTickCount
) {
    public SellerPhaseRecord {
        if (sellerResidentUuid == null) {
            throw new IllegalArgumentException("sellerResidentUuid must not be null");
        }
        if (marketRecordUuid == null) {
            throw new IllegalArgumentException("marketRecordUuid must not be null");
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase must not be null");
        }
        if (phaseTickCount < 0) {
            throw new IllegalArgumentException("phaseTickCount must be non-negative");
        }
    }

    /** Returns a copy whose tick counter has been incremented by one. */
    public SellerPhaseRecord withIncrementedTick() {
        return new SellerPhaseRecord(
                this.sellerResidentUuid,
                this.marketRecordUuid,
                this.phase,
                this.phaseStartGameTime,
                this.phaseTickCount + 1
        );
    }

    /** Returns a copy transitioned to {@code nextPhase} anchored at {@code gameTime}. */
    public SellerPhaseRecord transitionedTo(SellerPhase nextPhase, long gameTime) {
        return new SellerPhaseRecord(
                this.sellerResidentUuid,
                this.marketRecordUuid,
                nextPhase,
                gameTime,
                0
        );
    }
}
