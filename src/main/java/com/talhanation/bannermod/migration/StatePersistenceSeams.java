package com.talhanation.bannermod.migration;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contract-only persistence and synchronized-client-state seams for Phase 7 migration prep.
 *
 * <p>Source anchors are documented in
 * {@code .planning/phases/07-migration-ready-internal-seams/07-SEAM-INVENTORY.md} and currently
 * live in {@code ClientManager} synchronized-cache mutation plus faction/team {@code SavedData}
 * orchestration in the world managers.</p>
 */
public final class StatePersistenceSeams {

    private StatePersistenceSeams() {
    }

    /**
     * Minimal synchronized client-state snapshot needed by the reset and siege-rebuild seams.
     */
    public record ClientSyncState(
            Map<String, RecruitsRoute> routes,
            List<RecruitsClaim> claims,
            Map<UUID, RecruitsClaim> activeSieges
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

    /**
     * Rebuilds or updates the derived active-siege index from claim state rather than manual map mutation.
     */
    public interface ActiveSiegeTracker {
        Map<UUID, RecruitsClaim> rebuild(List<RecruitsClaim> claims);

        Map<UUID, RecruitsClaim> update(Map<UUID, RecruitsClaim> currentSieges, @Nullable RecruitsClaim claim);
    }
}
