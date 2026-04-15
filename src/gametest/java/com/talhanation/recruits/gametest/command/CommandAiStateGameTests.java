package com.talhanation.recruits.gametest.command;

import com.talhanation.recruits.CommandEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractLeaderEntity;
import com.talhanation.recruits.entities.MessengerEntity;
import com.talhanation.recruits.entities.ScoutEntity;
import com.talhanation.recruits.gametest.support.RecruitsAiStateGameTestSupport;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class CommandAiStateGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 160)
    public static void moveArrivalProducesBoundedHoldBehavior(GameTestHelper helper) {
        RecruitsAiStateGameTestSupport.AiStateScenario scenario = RecruitsAiStateGameTestSupport.spawnScenario(helper);
        scenario.recruit().setMovePos(helper.absolutePos(RecruitsAiStateGameTestSupport.RECRUIT_POS).offset(2, 0, 0));
        scenario.recruit().setShouldMovePos(true);
        scenario.recruit().reachedMovePos = true;

        helper.runAfterDelay(5, () -> {
            if (scenario.recruit().getFollowState() != 2 || scenario.recruit().getHoldPos() == null) {
                throw new IllegalArgumentException("Expected move arrival to settle into stable hold behavior");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 160)
    public static void patrolLeadersRecoverPredictablyFromManualInterruptions(GameTestHelper helper) {
        RecruitsAiStateGameTestSupport.AiStateScenario scenario = RecruitsAiStateGameTestSupport.spawnScenario(helper);
        scenario.leader().currentWaypoint = scenario.leader().blockPosition();
        scenario.leader().setPatrolState(AbstractLeaderEntity.State.WAITING);
        CommandEvents.checkPatrolLeaderState(scenario.leader());

        if (scenario.leader().getPatrollingState() != AbstractLeaderEntity.State.PAUSED.getIndex()) {
            throw new IllegalArgumentException("Expected patrol leader to pause when manual movement interrupts patrolling");
        }

        scenario.leader().setPatrolState(AbstractLeaderEntity.State.UPKEEP);
        CommandEvents.checkPatrolLeaderState(scenario.leader());

        if (scenario.leader().getPatrollingState() != AbstractLeaderEntity.State.IDLE.getIndex()) {
            throw new IllegalArgumentException("Expected retreating patrol leader to reset to idle after manual interruption");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 220)
    public static void scoutAndMessengerCyclesRecoverWithoutGettingStuck(GameTestHelper helper) {
        RecruitsAiStateGameTestSupport.AiStateScenario scenario = RecruitsAiStateGameTestSupport.spawnScenario(helper);
        scenario.scout().startTask(ScoutEntity.State.SCOUTING);
        scenario.scout().startTask(ScoutEntity.State.IDLE);

        if (scenario.scout().getTaskState() != ScoutEntity.State.IDLE.getIndex()) {
            throw new IllegalArgumentException("Expected scout mission recovery to return to idle");
        }

        scenario.messenger().setListen(false);
        scenario.messenger().setMessengerState(MessengerEntity.MessengerState.TELEPORT_BACK);
        scenario.messenger().teleportWaitTimer = 0;

        helper.runAfterDelay(20, () -> {
            if (scenario.messenger().getMessengerState() != MessengerEntity.MessengerState.IDLE || !scenario.messenger().getListen()) {
                throw new IllegalArgumentException("Expected messenger recovery cycle to return to an idle listening state");
            }
            helper.succeed();
        });
    }
}
