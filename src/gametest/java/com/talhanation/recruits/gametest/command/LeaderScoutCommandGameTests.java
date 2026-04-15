package com.talhanation.recruits.gametest.command;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.entities.ScoutEntity;
import com.talhanation.recruits.gametest.support.RecruitsLeaderScoutGameTestSupport;
import com.talhanation.recruits.network.MessagePatrolLeaderSetPatrolState;
import com.talhanation.recruits.network.MessagePatrolLeaderSetRoute;
import com.talhanation.recruits.network.MessageScoutTask;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class LeaderScoutCommandGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void validLeaderPacketsOnlyMutateTargetedLeader(GameTestHelper helper) {
        RecruitsLeaderScoutGameTestSupport.LeaderScoutScenario scenario = RecruitsLeaderScoutGameTestSupport.spawnScenario(helper);
        UUID routeId = UUID.fromString("00000000-0000-0000-0000-000000000702");
        byte otherLeaderState = scenario.otherLeader().getPatrollingState();
        UUID otherLeaderRouteId = scenario.otherLeader().getRouteID();

        MessagePatrolLeaderSetPatrolState.dispatchToServer(scenario.player(), scenario.leader().getUUID(), (byte) 1);
        MessagePatrolLeaderSetRoute.dispatchToServer(
                scenario.player(),
                scenario.leader().getUUID(),
                routeId,
                List.of(new BlockPos(8, 2, 3), new BlockPos(10, 2, 3)),
                List.of(0, 3)
        );

        helper.runAfterDelay(5, () -> {
            if (scenario.leader().getPatrollingState() != AbstractLeaderEntity.State.PATROLLING.getIndex()) {
                throw new IllegalArgumentException("Expected patrol state packet to update targeted leader");
            }
            if (!routeId.equals(scenario.leader().getRouteID()) || scenario.leader().currentWaypoint == null) {
                throw new IllegalArgumentException("Expected route packet to populate targeted leader route data");
            }
            if (scenario.otherLeader().getPatrollingState() != otherLeaderState || !java.util.Objects.equals(scenario.otherLeader().getRouteID(), otherLeaderRouteId)) {
                throw new IllegalArgumentException("Expected unrelated leader to remain unchanged");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void validScoutTaskOnlyStartsRequestedMissionOnTargetScout(GameTestHelper helper) {
        RecruitsLeaderScoutGameTestSupport.LeaderScoutScenario scenario = RecruitsLeaderScoutGameTestSupport.spawnScenario(helper);

        MessageScoutTask.dispatchToServer(scenario.player(), scenario.scout().getUUID(), ScoutEntity.State.SCOUTING.getIndex());

        helper.runAfterDelay(5, () -> {
            if (scenario.scout().getTaskState() != ScoutEntity.State.SCOUTING.getIndex()) {
                throw new IllegalArgumentException("Expected targeted scout to enter requested mission state");
            }
            if (scenario.otherScout().getTaskState() != ScoutEntity.State.IDLE.getIndex()) {
                throw new IllegalArgumentException("Expected unrelated scout to remain idle");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180, required = false)
    public static void invalidLeaderAndScoutPacketsFailSafely(GameTestHelper helper) {
        RecruitsLeaderScoutGameTestSupport.LeaderScoutScenario scenario = RecruitsLeaderScoutGameTestSupport.spawnScenario(helper);
        byte leaderState = scenario.leader().getPatrollingState();
        UUID leaderRouteId = scenario.leader().getRouteID();
        byte otherLeaderState = scenario.otherLeader().getPatrollingState();
        byte farLeaderState = scenario.farLeader().getPatrollingState();
        int scoutState = scenario.scout().getTaskState();
        int otherScoutState = scenario.otherScout().getTaskState();

        MessagePatrolLeaderSetPatrolState.dispatchToServer(scenario.player(), scenario.otherLeader().getUUID(), (byte) 1);
        MessagePatrolLeaderSetRoute.dispatchToServer(scenario.player(), scenario.leader().getUUID(), UUID.randomUUID(), List.of(new BlockPos(3, 2, 3)), List.of());
        MessageScoutTask.dispatchToServer(scenario.player(), scenario.otherScout().getUUID(), ScoutEntity.State.SCOUTING.getIndex());
        MessagePatrolLeaderSetPatrolState.dispatchToServer(scenario.player(), scenario.farLeader().getUUID(), (byte) 1);

        helper.runAfterDelay(5, () -> {
            if (scenario.leader().getPatrollingState() != leaderState || !java.util.Objects.equals(scenario.leader().getRouteID(), leaderRouteId)) {
                throw new IllegalArgumentException("Expected invalid leader packets to leave the targeted leader unchanged");
            }
            if (scenario.otherLeader().getPatrollingState() != otherLeaderState) {
                throw new IllegalArgumentException("Expected foreign leader packet to degrade safely");
            }
            if (scenario.farLeader().getPatrollingState() != farLeaderState) {
                throw new IllegalArgumentException("Expected out-of-radius leader packet to degrade safely");
            }
            if (scenario.scout().getTaskState() != scoutState || scenario.otherScout().getTaskState() != otherScoutState) {
                throw new IllegalArgumentException("Expected invalid scout packet to leave scout state unchanged");
            }
            helper.succeed();
        });
    }
}
