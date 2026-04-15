package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.FormationTargetSelectionController;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.stream.Stream;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class FormationTargetSelectionGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "battle_harness_field", timeoutTicks = 220)
    public static void formationCohortFocusesFireOnOneSharedTarget(GameTestHelper helper) {
        Scenario scenario = createScenario(helper, "00000000-0000-0000-0000-000000000421", "00000000-0000-0000-0000-000000000422");

        helper.runAfterDelay(5, () -> {
            refreshTargets(scenario.west().recruits());
            List<AbstractRecruitEntity> westAlive = scenario.west().recruits().stream().filter(AbstractRecruitEntity::isAlive).toList();
            if (westAlive.size() < 2) {
                throw new IllegalArgumentException("Expected at least two west recruits alive while validating focus fire");
            }

            LivingEntity sharedTarget = westAlive.get(0).getTarget();
            if (sharedTarget == null || !sharedTarget.isAlive()) {
                throw new IllegalArgumentException("Expected west cohort to acquire a living shared target");
            }
            if (westAlive.stream().anyMatch(recruit -> recruit.getTarget() != sharedTarget)) {
                throw new IllegalArgumentException("Expected living west formation recruits to focus fire the same shared target");
            }
            if (FormationTargetSelectionController.profilingSnapshot().formationSelectionReuses() <= 0) {
                throw new IllegalArgumentException("Expected focus-fire scenario to record formation target reuse");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 260, required = false)
    public static void formationCohortRetargetsAfterSharedEnemyDies(GameTestHelper helper) {
        RecoveryScenario scenario = createRecoveryScenario(helper, "00000000-0000-0000-0000-000000000423", "00000000-0000-0000-0000-000000000424");

        helper.runAfterDelay(5, () -> {
            LivingEntity currentSharedTarget = firstLivingTarget(scenario.west().recruits());
            if (currentSharedTarget == null) {
                throw new IllegalArgumentException("Expected west formation cohort to acquire an initial shared target before retargeting");
            }

            currentSharedTarget.kill();

            helper.runAfterDelay(5, () -> {
                refreshTargets(scenario.west().recruits());
                List<AbstractRecruitEntity> westAlive = scenario.west().recruits().stream().filter(AbstractRecruitEntity::isAlive).toList();
                List<AbstractRecruitEntity> eastAlive = scenario.east().recruits().stream().filter(AbstractRecruitEntity::isAlive).toList();
                if (eastAlive.isEmpty()) {
                    throw new IllegalArgumentException("Expected at least one east recruit to survive long enough for retarget validation");
                }

                LivingEntity replacementTarget = westAlive.get(0).getTarget();
                if (replacementTarget == null || !replacementTarget.isAlive()) {
                    throw new IllegalArgumentException("Expected west formation cohort to retarget onto a surviving enemy");
                }

                if (westAlive.stream().anyMatch(recruit -> recruit.getTarget() != replacementTarget)) {
                    throw new IllegalArgumentException("Expected surviving west formation recruits to converge on the replacement target");
                }

                if (FormationTargetSelectionController.profilingSnapshot().formationSelectionInvalidations() <= 0) {
                    throw new IllegalArgumentException("Expected retarget scenario to record a formation assignment invalidation");
                }

                helper.succeed();
            });
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 220, required = false)
    public static void formationCohortClearsDeadTargetsWhenNoReplacementExists(GameTestHelper helper) {
        RecoveryScenario scenario = createRecoveryScenario(helper, "00000000-0000-0000-0000-000000000425", "00000000-0000-0000-0000-000000000426");

        helper.runAfterDelay(5, () -> {
            for (AbstractRecruitEntity recruit : scenario.east().recruits()) {
                if (recruit.isAlive()) {
                    recruit.kill();
                }
            }

            helper.runAfterDelay(5, () -> {
                refreshTargets(scenario.west().recruits());
                List<AbstractRecruitEntity> westAlive = scenario.west().recruits().stream().filter(AbstractRecruitEntity::isAlive).toList();
                if (westAlive.stream().anyMatch(recruit -> recruit.getTarget() != null)) {
                    throw new IllegalArgumentException("Expected west formation recruits to clear stale targets once no enemy remains");
                }

                if (FormationTargetSelectionController.profilingSnapshot().formationSelectionInvalidations() <= 0) {
                    throw new IllegalArgumentException("Expected loss-of-target scenario to invalidate the shared assignment");
                }

                helper.succeed();
            });
        });
    }

    private static Scenario createScenario(GameTestHelper helper, String westOwnerId, String eastOwnerId) {
        FormationTargetSelectionController.resetProfiling();
        prepareMixedSquadPads(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST);
        prepareMixedSquadPads(helper, RecruitsBattleGameTestSupport.SquadAnchor.EAST);
        RecruitsBattleGameTestSupport.BattleSquad west = RecruitsBattleGameTestSupport.spawnWestMixedSquad(helper, UUID.fromString(westOwnerId));
        RecruitsBattleGameTestSupport.BattleSquad east = RecruitsBattleGameTestSupport.spawnEastMixedSquad(helper, UUID.fromString(eastOwnerId));

        restageMixedSquad(helper, west, RecruitsBattleGameTestSupport.SquadAnchor.WEST);
        restageMixedSquad(helper, east, RecruitsBattleGameTestSupport.SquadAnchor.EAST);

        RecruitsBattleGameTestSupport.assignFormationCohort(west.recruits(), UUID.fromString("00000000-0000-0000-0000-000000000429"));
        RecruitsBattleGameTestSupport.assignFormationCohort(east.recruits(), UUID.fromString("00000000-0000-0000-0000-000000000430"));
        RecruitsBattleGameTestSupport.setMutualTargets(east.recruits(), west.recruits());
        return new Scenario(west, east);
    }

    private static RecoveryScenario createRecoveryScenario(GameTestHelper helper, String westOwnerId, String eastOwnerId) {
        FormationTargetSelectionController.resetProfiling();
        prepareRecoveryPads(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST);
        prepareRecoveryPads(helper, RecruitsBattleGameTestSupport.SquadAnchor.EAST);
        RecruitsBattleGameTestSupport.BattleSquad west = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.WEST,
                UUID.fromString(westOwnerId),
                "West Recovery"
        );
        RecruitsBattleGameTestSupport.BattleSquad east = RecruitsBattleGameTestSupport.spawnRecoveryPair(
                helper,
                RecruitsBattleGameTestSupport.SquadAnchor.EAST,
                UUID.fromString(eastOwnerId),
                "East Recovery"
        );

        restageRecoveryPair(helper, west, RecruitsBattleGameTestSupport.SquadAnchor.WEST);
        restageRecoveryPair(helper, east, RecruitsBattleGameTestSupport.SquadAnchor.EAST);

        RecruitsBattleGameTestSupport.assignFormationCohort(west.recruits(), UUID.fromString("00000000-0000-0000-0000-000000000431"));
        RecruitsBattleGameTestSupport.assignFormationCohort(east.recruits(), UUID.fromString("00000000-0000-0000-0000-000000000432"));
        RecruitsBattleGameTestSupport.removeOtherRecruits(helper, Stream.concat(west.recruits().stream(), east.recruits().stream()).toList());
        RecruitsBattleGameTestSupport.setHoldFormation(west);
        RecruitsBattleGameTestSupport.setHoldFormation(east);
        RecruitsBattleGameTestSupport.setSharedTarget(west.recruits(), east.recruits().get(0));
        RecruitsBattleGameTestSupport.setMutualTargets(east.recruits(), west.recruits());
        return new RecoveryScenario(west, east);
    }

    private static LivingEntity firstLivingTarget(List<AbstractRecruitEntity> recruits) {
        return recruits.stream()
                .filter(AbstractRecruitEntity::isAlive)
                .map(AbstractRecruitEntity::getTarget)
                .filter(target -> target != null && target.isAlive())
                .findFirst()
                .orElse(null);
    }

    private static void refreshTargets(List<AbstractRecruitEntity> recruits) {
        for (AbstractRecruitEntity recruit : recruits) {
            if (recruit.isAlive()) {
                recruit.setTarget(null);
                recruit.searchForTargets();
            }
        }
    }

    private static void restageMixedSquad(
            GameTestHelper helper,
            RecruitsBattleGameTestSupport.BattleSquad squad,
            RecruitsBattleGameTestSupport.SquadAnchor anchor
    ) {
        restageRecruit(helper, squad.recruits().get(0), anchor.frontlinePos());
        restageRecruit(helper, squad.recruits().get(1), anchor.flankPos());
        restageRecruit(helper, squad.recruits().get(2), anchor.rangedLeftPos());
        restageRecruit(helper, squad.recruits().get(3), anchor.rangedRightPos());
    }

    private static void restageRecoveryPair(
            GameTestHelper helper,
            RecruitsBattleGameTestSupport.BattleSquad squad,
            RecruitsBattleGameTestSupport.SquadAnchor anchor
    ) {
        restageRecruit(helper, squad.recruits().get(0), anchor.recoveryLeftPos());
        restageRecruit(helper, squad.recruits().get(1), anchor.recoveryRightPos());
    }

    private static void restageRecruit(GameTestHelper helper, AbstractRecruitEntity recruit, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        Vec3 safePadCenter = new Vec3(absolutePos.getX() + 0.5D, absolutePos.getY() + 1.0D, absolutePos.getZ() + 0.5D);
        recruit.moveTo(safePadCenter.x(), safePadCenter.y(), safePadCenter.z(), 0.0F, 0.0F);
        recruit.setHoldPos(safePadCenter);
    }

    private static void prepareMixedSquadPads(GameTestHelper helper, RecruitsBattleGameTestSupport.SquadAnchor anchor) {
        prepareSafePad(helper, anchor.frontlinePos());
        prepareSafePad(helper, anchor.flankPos());
        prepareSafePad(helper, anchor.rangedLeftPos());
        prepareSafePad(helper, anchor.rangedRightPos());
    }

    private static void prepareRecoveryPads(GameTestHelper helper, RecruitsBattleGameTestSupport.SquadAnchor anchor) {
        prepareSafePad(helper, anchor.recoveryLeftPos());
        prepareSafePad(helper, anchor.recoveryRightPos());
    }

    private static void prepareSafePad(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, Blocks.STONE);
        helper.setBlock(relativePos.above(), Blocks.AIR);
        helper.setBlock(relativePos.above(2), Blocks.AIR);
    }

    private record Scenario(
            RecruitsBattleGameTestSupport.BattleSquad west,
            RecruitsBattleGameTestSupport.BattleSquad east
    ) {
    }

    private record RecoveryScenario(
            RecruitsBattleGameTestSupport.BattleSquad west,
            RecruitsBattleGameTestSupport.BattleSquad east
    ) {
    }
}
