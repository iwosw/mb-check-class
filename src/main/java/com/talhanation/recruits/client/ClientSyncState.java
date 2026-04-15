package com.talhanation.recruits.client;

import com.talhanation.recruits.migration.StatePersistenceSeams;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsRoute;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientSyncState implements StatePersistenceSeams.ClientSyncReset, StatePersistenceSeams.ActiveSiegeTracker {

    @Override
    public StatePersistenceSeams.ClientSyncState resetPreservingRoutes(Map<String, RecruitsRoute> routes) {
        return new StatePersistenceSeams.ClientSyncState(routes, List.of(), new HashMap<>());
    }

    @Override
    public Map<UUID, RecruitsClaim> rebuild(List<RecruitsClaim> claims) {
        Map<UUID, RecruitsClaim> activeSieges = new HashMap<>();
        for (RecruitsClaim claim : claims) {
            if (claim.isUnderSiege) {
                activeSieges.put(claim.getUUID(), claim);
            }
        }
        return activeSieges;
    }

    @Override
    public Map<UUID, RecruitsClaim> update(Map<UUID, RecruitsClaim> currentSieges, @Nullable RecruitsClaim claim) {
        Map<UUID, RecruitsClaim> updated = new HashMap<>(currentSieges);
        if (claim == null) return updated;

        if (claim.isUnderSiege) {
            updated.put(claim.getUUID(), claim);
        }
        else {
            updated.remove(claim.getUUID());
        }
        return updated;
    }
}
