package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.gametest.support.RecruitsFormationAsserts;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BattleHarnessGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_harness_field")
    public static void mixedSquadsSpawnAliveOnDedicatedPads(GameTestHelper helper) {
        RecruitsBattleGameTestSupport.BattleSquad westSquad = RecruitsBattleGameTestSupport.spawnWestMixedSquad(helper, UUID.fromString("00000000-0000-0000-0000-000000000301"));
        RecruitsBattleGameTestSupport.BattleSquad eastSquad = RecruitsBattleGameTestSupport.spawnEastMixedSquad(helper, UUID.fromString("00000000-0000-0000-0000-000000000302"));

        RecruitsBattleGameTestSupport.assertAfterDelay(helper, 5, () -> {
            for (AbstractRecruitEntity recruit : westSquad.recruits()) {
                if (!recruit.isAlive()) {
                    throw new IllegalArgumentException("Expected west-side recruit to be alive");
                }
            }
            for (AbstractRecruitEntity recruit : eastSquad.recruits()) {
                if (!recruit.isAlive()) {
                    throw new IllegalArgumentException("Expected east-side recruit to be alive");
                }
            }

            RecruitsFormationAsserts.assertFormationAnchorIntent(helper, westSquad, RecruitsBattleGameTestSupport.SquadAnchor.WEST, 0.1D);
            RecruitsFormationAsserts.assertFormationAnchorIntent(helper, eastSquad, RecruitsBattleGameTestSupport.SquadAnchor.EAST, 0.1D);
            RecruitsFormationAsserts.assertStableSpacing(westSquad.recruits(), 1.25D, 3.5D, RecruitsBattleGameTestSupport.formationAnchor(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST));
            RecruitsFormationAsserts.assertStableSpacing(eastSquad.recruits(), 1.25D, 3.5D, RecruitsBattleGameTestSupport.formationAnchor(helper, RecruitsBattleGameTestSupport.SquadAnchor.EAST));
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field")
    public static void recoveryHarnessExposesHoldAndReturnAnchors(GameTestHelper helper) {
        RecruitsBattleGameTestSupport.BattleSquad westPair = RecruitsBattleGameTestSupport.spawnRecoveryPair(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST, UUID.fromString("00000000-0000-0000-0000-000000000303"), "Recovery West");

        RecruitsBattleGameTestSupport.assertAfterDelay(helper, 5, () -> {
            AbstractRecruitEntity leftRecruit = westPair.recruits().get(0);
            AbstractRecruitEntity rightRecruit = westPair.recruits().get(1);
            Vec3 leftHoldPos = Vec3.atCenterOf(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.recoveryLeftPos()));
            Vec3 rightHoldPos = Vec3.atCenterOf(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.recoveryRightPos()));

            RecruitsFormationAsserts.assertHoldPositionIntent(leftRecruit, leftHoldPos, 0.1D);
            RecruitsFormationAsserts.assertHoldPositionIntent(rightRecruit, rightHoldPos, 0.1D);

            leftRecruit.teleportTo(leftRecruit.getX() + 1.0D, leftRecruit.getY(), leftRecruit.getZ());
            rightRecruit.teleportTo(rightRecruit.getX() + 1.0D, rightRecruit.getY(), rightRecruit.getZ());
            leftRecruit.setHoldPos(leftHoldPos);
            rightRecruit.setHoldPos(rightHoldPos);
            leftRecruit.setFollowState(3);
            rightRecruit.setFollowState(3);

            helper.runAfterDelay(5, () -> {
                RecruitsFormationAsserts.assertReturnToPositionIntent(leftRecruit, leftHoldPos, 0.1D);
                RecruitsFormationAsserts.assertReturnToPositionIntent(rightRecruit, rightHoldPos, 0.1D);
                RecruitsFormationAsserts.assertStableSpacing(westPair.recruits(), 1.5D, 4.0D, RecruitsBattleGameTestSupport.formationAnchor(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST));
                helper.succeed();
            });
        });
    }
}
