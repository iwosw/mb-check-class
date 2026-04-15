package com.talhanation.recruits.client;

import com.talhanation.recruits.testsupport.RecruitsFixtures;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsRoute;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSyncStateTest {

    @Test
    void resetPreservesRoutes() {
        ClientSyncState syncState = new ClientSyncState();
        RecruitsRoute localRoute = new RecruitsRoute("Local Route");
        Map<String, RecruitsRoute> routes = new HashMap<>(Map.of(localRoute.getId().toString(), localRoute));

        com.talhanation.recruits.migration.StatePersistenceSeams.ClientSyncState reset = syncState.resetPreservingRoutes(routes);

        assertSame(routes, reset.routes());
        assertTrue(reset.claims().isEmpty());
        assertTrue(reset.activeSieges().isEmpty());
    }

    @Test
    void activeSiegesStayDerivedFromClaimState() {
        ClientSyncState syncState = new ClientSyncState();
        RecruitsClaim claim = RecruitsFixtures.sampleClaim();

        Map<java.util.UUID, RecruitsClaim> rebuilt = syncState.rebuild(List.of(claim));

        assertEquals(1, rebuilt.size());
        assertSame(claim, rebuilt.get(claim.getUUID()));

        claim.isUnderSiege = false;
        Map<java.util.UUID, RecruitsClaim> updated = syncState.update(rebuilt, claim);

        assertFalse(updated.containsKey(claim.getUUID()));
        assertNull(syncState.update(updated, null).get(claim.getUUID()));
    }
}
