package com.talhanation.bannermod.migration;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;

import java.util.List;
import java.util.Map;

/**
 * Contract-only persistence and synchronized-client-state seams for Phase 7 migration prep.
 *
 * <p>Source anchors are documented in
 * {@code .planning/phases/07-migration-ready-internal-seams/07-SEAM-INVENTORY.md} and currently
 * live in {@code ClientManager} synchronized-cache mutation plus political-entity {@code SavedData}
 * orchestration in the world managers.</p>
 */
public final class StatePersistenceSeams {

    private StatePersistenceSeams() {
    }

    /**
     * Minimal synchronized client-state snapshot needed by the reset seam.
     */
    public record ClientSyncState(
            Map<String, RecruitsRoute> routes,
            List<RecruitsClaim> claims
    ) {
    }

    /**
     * Describes one persistence mutation plus the dirty-marking and broadcast work that must follow it.
     */
    public record SavedDataMutation(String name, Runnable apply, Runnable markDirty, Runnable broadcast) {
    }

    /**
     * Resets synchronized client caches while preserving route-library state.
     */
    public interface ClientSyncReset {
        ClientSyncState resetPreservingRoutes(Map<String, RecruitsRoute> routes);
    }
}
