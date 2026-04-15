package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.gametest.support.RecruitsFormationAsserts;
import com.talhanation.recruits.network.MessageFormationFollowMovement;
import com.talhanation.recruits.network.MessageMovement;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class FormationRecoveryGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 160)
    public static void packetDrivenFormationCommandsReachNearbyOwnedRecruits(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        UUID groupId = UUID.fromString("00000000-0000-0000-0000-000000000341");
        RecruitsBattleGameTestSupport.BattleSquad westPair = RecruitsBattleGameTestSupport.spawnRecoveryPair(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST, player.getUUID(), "Packet West");

        for (AbstractRecruitEntity recruit : westPair.recruits()) {
            recruit.setGroupUUID(groupId);
        }

        player.moveTo(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D, helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(), helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D, 0.0F, 0.0F);
        player.setYRot(0.0F);

        MessageFormationFollowMovement.dispatchToServer(player, player.getUUID(), groupId, 1);

        helper.runAfterDelay(10, () -> {
            if (westPair.recruits().stream().anyMatch(recruit -> !recruit.isInFormation || recruit.getFollowState() != 3)) {
                throw new IllegalArgumentException("Expected packet-driven formation command to place nearby owned recruits into formation");
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "formation_recovery_field", timeoutTicks = 220, required = false)
    public static void packetDrivenRecoveryRestoresHoldIntentAfterCombat(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        UUID groupId = UUID.fromString("00000000-0000-0000-0000-000000000342");
        RecruitsBattleGameTestSupport.BattleSquad westPair = RecruitsBattleGameTestSupport.spawnRecoveryPair(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST, player.getUUID(), "Recovery West");
        RecruitsBattleGameTestSupport.BattleSquad eastPair = RecruitsBattleGameTestSupport.spawnRecoveryPair(helper, RecruitsBattleGameTestSupport.SquadAnchor.EAST, UUID.fromString("00000000-0000-0000-0000-000000000343"), "Recovery East");

        for (AbstractRecruitEntity recruit : westPair.recruits()) {
            recruit.setGroupUUID(groupId);
        }
        for (AbstractRecruitEntity recruit : eastPair.recruits()) {
            recruit.setHealth(Math.min(recruit.getHealth(), 4.0F));
        }

        player.moveTo(helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getX() + 0.5D, helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getY(), helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor()).getZ() + 0.5D, 0.0F, 0.0F);
        player.setYRot(0.0F);

        MessageFormationFollowMovement.dispatchToServer(player, player.getUUID(), groupId, 1);
        RecruitsBattleGameTestSupport.setMutualTargets(westPair.recruits(), eastPair.recruits());
        RecruitsBattleGameTestSupport.setMutualTargets(eastPair.recruits(), westPair.recruits());

        helper.runAfterDelay(80, () -> {
            MessageMovement.dispatchToServer(player, player.getUUID(), groupId, 2, 1, false);

            helper.runAfterDelay(10, () -> {
                List<AbstractRecruitEntity> survivors = westPair.recruits().stream().filter(AbstractRecruitEntity::isAlive).toList();
                if (survivors.isEmpty()) {
                    throw new IllegalArgumentException("Expected at least one west-side recruit to survive recovery scenario");
                }

                Vec3 anchor = RecruitsBattleGameTestSupport.formationAnchor(helper, RecruitsBattleGameTestSupport.SquadAnchor.WEST);
                RecruitsFormationAsserts.assertStableSpacing(survivors, 1.0D, 6.5D, anchor);
                for (AbstractRecruitEntity recruit : survivors) {
                    if (recruit.getFollowState() != 3 || recruit.getHoldPos() == null) {
                        throw new IllegalArgumentException("Expected survivors to recover bounded return-to-position intent after combat");
                    }
                }
                helper.succeed();
            });
        });
    }
}
