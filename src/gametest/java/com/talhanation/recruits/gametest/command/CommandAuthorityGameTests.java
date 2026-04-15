package com.talhanation.recruits.gametest.command;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.recruits.network.MessageAttack;
import com.talhanation.recruits.network.MessageFormationFollowMovement;
import com.talhanation.recruits.network.MessageMovement;
import com.talhanation.recruits.network.MessageShields;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class CommandAuthorityGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void movementOnlyAffectsNearbyOwnedGroupRecruits(GameTestHelper helper) {
        RecruitsCommandGameTestSupport.CommandScenario scenario = RecruitsCommandGameTestSupport.spawnCommandScenario(helper);

        MessageMovement.dispatchToServer(scenario.player(), scenario.player().getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, 1, 0, false);

        helper.runAfterDelay(5, () -> {
            for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
                if (recruit.getFollowState() != 1) {
                    throw new IllegalArgumentException("Expected targeted recruit to follow after movement command");
                }
            }
            RecruitsCommandGameTestSupport.assertUnchanged(scenario.otherGroupRecruit(), 0, false);
            RecruitsCommandGameTestSupport.assertUnchanged(scenario.foreignRecruit(), 0, false);
            RecruitsCommandGameTestSupport.assertUnchanged(scenario.farRecruit(), 0, false);
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void formationOnlyAffectsNearbyOwnedGroupRecruits(GameTestHelper helper) {
        RecruitsCommandGameTestSupport.CommandScenario scenario = RecruitsCommandGameTestSupport.spawnCommandScenario(helper);

        MessageFormationFollowMovement.dispatchToServer(scenario.player(), scenario.player().getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, 1);

        helper.runAfterDelay(10, () -> {
            for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
                if (!recruit.isInFormation) {
                    throw new IllegalArgumentException("Expected targeted recruit to enter formation via packet path");
                }
            }
            if (scenario.otherGroupRecruit().isInFormation || scenario.foreignRecruit().isInFormation || scenario.farRecruit().isInFormation) {
                throw new IllegalArgumentException("Expected only nearby owned recruits in the addressed group to enter formation");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void attackAndShieldCommandsIgnoreInvalidOrEmptySelections(GameTestHelper helper) {
        RecruitsCommandGameTestSupport.CommandScenario scenario = RecruitsCommandGameTestSupport.spawnCommandScenario(helper);
        for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
            recruit.setFollowState(1);
        }

        MessageShields.dispatchToServer(scenario.player(), scenario.player().getUUID(), RecruitsCommandGameTestSupport.OTHER_GROUP_UUID, true);
        MessageAttack.dispatchToServer(scenario.player(), RecruitsCommandGameTestSupport.FOREIGN_OWNER_UUID, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);

        helper.runAfterDelay(10, () -> {
            for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
                RecruitsCommandGameTestSupport.assertUnchanged(recruit, 1, false);
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 180)
    public static void outOfRadiusRecruitsRemainUnchanged(GameTestHelper helper) {
        RecruitsCommandGameTestSupport.CommandScenario scenario = RecruitsCommandGameTestSupport.spawnCommandScenario(helper);

        MessageMovement.dispatchToServer(scenario.player(), scenario.player().getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, 1, 0, false);
        MessageShields.dispatchToServer(scenario.player(), scenario.player().getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true);

        helper.runAfterDelay(10, () -> {
            for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
                if (recruit.getFollowState() != 1 || !recruit.getShouldBlock()) {
                    throw new IllegalArgumentException("Expected nearby targeted recruits to react to movement and shield commands");
                }
            }
            RecruitsCommandGameTestSupport.assertUnchanged(scenario.farRecruit(), 0, false);
            helper.succeed();
        });
    }
}
