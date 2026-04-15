package com.talhanation.recruits.world;

import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RecruitsSavedDataFacadeTest {

    @Test
    void saveTeamsMarksDirtyBeforeBroadcast() {
        RecruitsSavedDataFacade facade = new RecruitsSavedDataFacade();
        Map<String, RecruitsFaction> teams = Map.of("alpha", RecruitsFixtures.sampleFaction());
        AtomicInteger order = new AtomicInteger();
        AtomicInteger applyOrder = new AtomicInteger();
        AtomicInteger dirtyOrder = new AtomicInteger();
        AtomicInteger broadcastOrder = new AtomicInteger();

        facade.saveTeams(
                teams,
                savedTeams -> {
                    applyOrder.set(order.incrementAndGet());
                    assertSame(teams, savedTeams);
                },
                () -> dirtyOrder.set(order.incrementAndGet()),
                () -> broadcastOrder.set(order.incrementAndGet())
        );

        assertEquals(1, applyOrder.get());
        assertEquals(2, dirtyOrder.get());
        assertEquals(3, broadcastOrder.get());
    }

    @Test
    void loadTeamsClearsRepopulatesAndHydratesConfigs() {
        RecruitsSavedDataFacade facade = new RecruitsSavedDataFacade();
        RecruitsFaction loadedFaction = RecruitsFixtures.sampleFaction();
        Map<String, RecruitsFaction> target = new HashMap<>();
        target.put("stale", new RecruitsFaction());
        Map<String, RecruitsFaction> loaded = Map.of(loadedFaction.getStringID(), loadedFaction);
        AtomicInteger hydrateCalls = new AtomicInteger();

        facade.loadTeams(target, loaded, faction -> {
            hydrateCalls.incrementAndGet();
            assertSame(loadedFaction, faction);
        });

        assertEquals(1, target.size());
        assertSame(loadedFaction, target.get(loadedFaction.getStringID()));
        assertEquals(1, hydrateCalls.get());
    }
}
