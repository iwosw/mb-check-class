package com.talhanation.bannermod.settlement.job;

import com.talhanation.bannermod.settlement.BannerModSettlementResidentRecord;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable context passed to a {@link JobHandler} for a single decision/step.
 *
 * <p>Intentionally record-shaped. The registry layer stays server-authoritative and does
 * <em>not</em> couple to a live entity reference; later slices may add projections (e.g. a
 * snapshot of position / inventory) but the contract starts with identifiers only.</p>
 */
public record JobExecutionContext(
        BannerModSettlementResidentRecord resident,
        long gameTime,
        @Nullable UUID boundEntityUuid,
        @Nullable UUID workplaceUuid
) {
    public JobExecutionContext {
        Objects.requireNonNull(resident, "resident");
    }

    public Optional<UUID> boundEntity() {
        return Optional.ofNullable(boundEntityUuid);
    }

    public Optional<UUID> workplace() {
        return Optional.ofNullable(workplaceUuid);
    }
}
