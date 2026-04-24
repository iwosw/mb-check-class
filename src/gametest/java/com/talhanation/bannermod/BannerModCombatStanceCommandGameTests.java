package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.ai.military.RecruitRangedBowAttackGoal;
import com.talhanation.bannermod.ai.military.RecruitRangedCrossbowAttackGoal;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.BowmanEntity;
import com.talhanation.bannermod.entity.military.CrossBowmanEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.network.messages.military.MessageCombatStance;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModCombatStanceCommandGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void combatStanceCommandTargetsOnlyTheRequestedOwnedGroup(GameTestHelper helper) {
        RecruitsCommandGameTestSupport.CommandScenario scenario = RecruitsCommandGameTestSupport.spawnCommandScenario(helper);

        MessageCombatStance.dispatchToServer(
                scenario.player(),
                scenario.player().getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                CombatStance.SHIELD_WALL
        );

        for (AbstractRecruitEntity recruit : scenario.targetedSquad()) {
            helper.assertTrue(recruit.getCombatStance() == CombatStance.SHIELD_WALL,
                    "Expected targeted owned recruits to receive the shield-wall stance command");
        }
        helper.assertTrue(scenario.otherGroupRecruit().getCombatStance() == CombatStance.LOOSE,
                "Expected other-group recruits to ignore the combat stance command");
        helper.assertTrue(scenario.foreignRecruit().getCombatStance() == CombatStance.LOOSE,
                "Expected foreign-owned recruits to ignore the combat stance command");
        helper.assertTrue(scenario.farRecruit().getCombatStance() == CombatStance.LOOSE,
                "Expected out-of-radius recruits to ignore the combat stance command");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void combatStanceCommandDrivesRangedFormationLeash(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer();
        owner.moveTo(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D,
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(),
                helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D,
                -90.0F,
                0.0F);
        owner.setYRot(-90.0F);

        RecruitsBattleGameTestSupport.BattleSquad squad = RecruitsBattleGameTestSupport.spawnWestMixedSquad(helper, owner.getUUID());
        RecruitsBattleGameTestSupport.assignFormationCohort(squad.recruits(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);

        AbstractRecruitEntity bowEnemy = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.EAST_RANGED_LEFT_POS,
                "Bow Enemy",
                RecruitsCommandGameTestSupport.FOREIGN_OWNER_UUID
        );
        AbstractRecruitEntity crossbowEnemy = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.EAST_RANGED_RIGHT_POS,
                "Crossbow Enemy",
                RecruitsCommandGameTestSupport.FOREIGN_OWNER_UUID
        );

        BowmanEntity bowman = null;
        CrossBowmanEntity crossBowman = null;
        for (AbstractRecruitEntity recruit : squad.recruits()) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
            recruit.isInFormation = true;
            recruit.setFollowState(2);
            recruit.setAggroState(1);
            if (recruit instanceof BowmanEntity foundBowman) {
                recruit.setShouldRanged(true);
                recruit.setTarget(bowEnemy);
                bowman = foundBowman;
            } else if (recruit instanceof CrossBowmanEntity foundCrossBowman) {
                recruit.setShouldRanged(true);
                recruit.setTarget(crossbowEnemy);
                crossBowman = foundCrossBowman;
            } else {
                recruit.setShouldRanged(false);
                recruit.setTarget(null);
            }
        }

        helper.assertTrue(bowman != null && crossBowman != null,
                "Expected the mixed squad fixture to include both bow and crossbow recruits");

        RecruitRangedBowAttackGoal<BowmanEntity> bowGoal = new RecruitRangedBowAttackGoal<>(bowman, 1.0D, 20, 40, 15.0F, 4.0D);
        RecruitRangedCrossbowAttackGoal crossbowGoal = new RecruitRangedCrossbowAttackGoal(crossBowman, 4.0D);

        MessageCombatStance.dispatchToServer(
                owner,
                owner.getUUID(),
                RecruitsCommandGameTestSupport.TARGET_GROUP_UUID,
                CombatStance.SHIELD_WALL
        );

        helper.assertFalse(bowGoal.canUse(),
                "Expected shield-wall bowmen to refuse a target beyond the tight formation leash");
        helper.assertFalse(crossbowGoal.canUse(),
                "Expected shield-wall crossbowmen to refuse a target beyond the tight formation leash");

        helper.succeed();
    }
}
