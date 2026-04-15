package com.talhanation.recruits.gametest.persistence;

import com.talhanation.recruits.ClaimEvents;
import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.gametest.support.RecruitsPersistenceGameTestSupport;
import com.talhanation.recruits.network.MessagePatrolLeaderSetRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class PersistenceSyncGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 200)
    public static void leaderRouteAssignmentSurvivesRepresentativeSaveDataRoundTrip(GameTestHelper helper) {
        RecruitsPersistenceGameTestSupport.PersistenceScenario scenario = RecruitsPersistenceGameTestSupport.spawnLeaderScenario(helper);
        UUID routeId = UUID.fromString("00000000-0000-0000-0000-000000000851");
        List<BlockPos> waypoints = List.of(RecruitsPersistenceGameTestSupport.WAYPOINT_ONE, RecruitsPersistenceGameTestSupport.WAYPOINT_TWO);
        List<Integer> waits = List.of(0, 5);

        MessagePatrolLeaderSetRoute.dispatchToServer(scenario.player(), scenario.leader().getUUID(), routeId, waypoints, waits);

        helper.runAfterDelay(5, () -> {
            RecruitsPersistenceGameTestSupport.assertLeaderRouteState(scenario.leader(), routeId, waypoints, waits);
            RecruitsPersistenceGameTestSupport.assertSavedRouteState(scenario.leader(), routeId, 2, 2);
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 200)
    public static void invalidLeaderRoutePayloadsRemainSafeNoOps(GameTestHelper helper) {
        RecruitsPersistenceGameTestSupport.PersistenceScenario scenario = RecruitsPersistenceGameTestSupport.spawnLeaderScenario(helper);

        MessagePatrolLeaderSetRoute.dispatchToServer(scenario.player(), scenario.leader().getUUID(), UUID.randomUUID(), List.of(RecruitsPersistenceGameTestSupport.WAYPOINT_ONE), List.of());
        MessagePatrolLeaderSetRoute.dispatchToServer(scenario.player(), UUID.randomUUID(), UUID.randomUUID(), List.of(RecruitsPersistenceGameTestSupport.WAYPOINT_ONE), List.of(1));

        helper.runAfterDelay(5, () -> {
            if (scenario.leader().getRouteID() != null || !scenario.leader().WAYPOINTS.isEmpty()) {
                throw new IllegalArgumentException("Expected invalid route payloads to leave the leader unchanged");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 200)
    public static void joinSyncHandlersRunAgainstPreparedPersistenceBaseline(GameTestHelper helper) {
        RecruitsPersistenceGameTestSupport.PersistenceScenario scenario = RecruitsPersistenceGameTestSupport.spawnLeaderScenario(helper);
        RecruitsPersistenceGameTestSupport.seedJoinSyncBaseline(scenario.level(), scenario.player());

        EntityJoinLevelEvent joinEvent = new EntityJoinLevelEvent(scenario.player(), scenario.level());
        FactionEvents factionEvents = new FactionEvents();
        factionEvents.server = scenario.level().getServer();
        RecruitEvents.server = scenario.level().getServer();
        ClaimEvents.server = scenario.level().getServer();

        factionEvents.onPlayerJoin(joinEvent);
        new RecruitEvents().onPlayerJoin(joinEvent);
        new ClaimEvents().onPlayerJoin(joinEvent);

        helper.runAfterDelay(5, () -> {
            if (FactionEvents.recruitsFactionManager.getFactionByStringID("phase5-sync") == null) {
                throw new IllegalArgumentException("Expected join sync baseline faction to remain available");
            }
            if (ClaimEvents.recruitsClaimManager.getAllClaims().isEmpty()) {
                throw new IllegalArgumentException("Expected join sync baseline claims to remain available");
            }
            if (RecruitEvents.recruitsGroupsManager.getPlayerGroups(scenario.player()).isEmpty()) {
                throw new IllegalArgumentException("Expected join sync baseline groups to remain available");
            }
            if (RecruitEvents.recruitsPlayerUnitManager.getRecruitCount(scenario.player().getUUID()) != 2) {
                throw new IllegalArgumentException("Expected join sync baseline unit info to remain available");
            }
            helper.succeed();
        });
    }
}
