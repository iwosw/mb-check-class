package com.talhanation.bannermod.client.military;

import com.talhanation.bannermod.migration.StatePersistenceSeams;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;

import java.util.List;
import java.util.Map;

public class ClientSyncState implements StatePersistenceSeams.ClientSyncReset {

    @Override
    public StatePersistenceSeams.ClientSyncState resetPreservingRoutes(Map<String, RecruitsRoute> routes) {
        return new StatePersistenceSeams.ClientSyncState(routes, List.of());
    }
}
