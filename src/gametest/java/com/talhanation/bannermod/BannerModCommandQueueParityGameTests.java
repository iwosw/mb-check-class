package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.army.command.CommandIntentQueueRuntime;
import com.talhanation.bannermod.army.command.MovementCommandState;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.AbstractStrategicFireRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModCommandQueueParityGameTests {
    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000901");
    private static final UUID MOUNT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000902");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void queuedCommandVariantsMatchImmediateFirstOrderBehavior(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, helper);

        assertMovementParity(helper, owner);
        assertFormationMovementParity(helper, owner);
        assertFaceParity(helper, owner);
        assertAttackParity(helper, owner);
        assertAggroParity(helper, owner);
        assertStanceParity(helper, owner);
        assertStrategicFireParity(helper, owner);
        assertSiegeMachineParity(helper, owner);

        CommandIntentQueueRuntime.instance().clearAllForTest();
        helper.succeed();
    }

    private static void assertMovementParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Movement");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Movement");
        Vec3 target = Vec3.atCenterOf(helper.absolutePos(new BlockPos(7, 2, 7)));

        CommandIntentDispatcher.dispatch(owner, movement(false, target), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, movement(true, target), List.of(queued));

        helper.assertTrue(immediate.getFollowState() == queued.getFollowState()
                        && immediate.getShouldMovePos() == queued.getShouldMovePos()
                        && immediate.getMovePos().equals(queued.getMovePos()),
                "Expected queued movement to match immediate move-to-position state");
    }

    private static void assertFormationMovementParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Formation Movement");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Formation Movement");
        Vec3 target = Vec3.atCenterOf(helper.absolutePos(new BlockPos(8, 2, 8)));

        CommandIntentDispatcher.dispatch(owner,
                new CommandIntent.Movement(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false,
                        MovementCommandState.MOVE_TO_POSITION, 1, false, target),
                List.of(immediate));
        CommandIntentDispatcher.dispatch(owner,
                new CommandIntent.Movement(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true,
                        MovementCommandState.MOVE_TO_POSITION, 1, false, target),
                List.of(queued));

        helper.assertTrue(immediate.isInFormation == queued.isInFormation
                        && immediate.getFollowState() == queued.getFollowState()
                        && immediate.getHoldPos().distanceToSqr(queued.getHoldPos()) < 1.0D
                        && immediate.getHoldPos().distanceToSqr(target) < 64.0D
                        && queued.getHoldPos().distanceToSqr(target) < 64.0D,
                "Expected queued formation movement to match immediate formation placement state");
    }

    private static void assertFaceParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Face");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Face");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Face(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, 0, false), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Face(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, 0, false), List.of(queued));

        helper.assertTrue(immediate.getFollowState() == queued.getFollowState()
                        && immediate.rotateTicks == queued.rotateTicks
                        && immediate.ownerRot == queued.ownerRot,
                "Expected queued face command to match immediate rotation state");
    }

    private static void assertAttackParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Attack");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Attack");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Attack(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Attack(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID), List.of(queued));

        helper.assertTrue((immediate.getTarget() != null) == (queued.getTarget() != null)
                        && immediate.getFollowState() == queued.getFollowState(),
                "Expected queued attack command to match immediate target/follow state");
    }

    private static void assertAggroParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Aggro");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Aggro");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Aggro(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, 2, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, false), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.Aggro(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, 2, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, false), List.of(queued));

        helper.assertTrue(immediate.getState() == queued.getState() && queued.getState() == 2,
                "Expected queued aggro command to match immediate aggro state");
    }

    private static void assertStanceParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Stance");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Stance");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.CombatStanceChange(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, CombatStance.SHIELD_WALL, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.CombatStanceChange(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, CombatStance.SHIELD_WALL, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID), List.of(queued));

        helper.assertTrue(immediate.getCombatStance() == queued.getCombatStance()
                        && queued.getCombatStance() == CombatStance.SHIELD_WALL
                        && immediate.getShouldBlock() == queued.getShouldBlock(),
                "Expected queued stance command to match immediate stance/shield state");
    }

    private static void assertStrategicFireParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractStrategicFireRecruitEntity immediate = bowman(helper, RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS, "Immediate Strategic Fire");
        AbstractStrategicFireRecruitEntity queued = bowman(helper, RecruitsBattleGameTestSupport.WEST_RANGED_RIGHT_POS, "Queued Strategic Fire");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.StrategicFire(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.StrategicFire(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true), List.of(queued));

        helper.assertTrue(immediate.getShouldStrategicFire() == queued.getShouldStrategicFire()
                        && queued.getShouldStrategicFire(),
                "Expected queued strategic fire command to match immediate firing state");
    }

    private static void assertSiegeMachineParity(GameTestHelper helper, ServerPlayer owner) {
        CommandIntentQueueRuntime.instance().clearAllForTest();
        AbstractRecruitEntity immediate = recruit(helper, RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS, "Immediate Siege Machine");
        AbstractRecruitEntity queued = recruit(helper, RecruitsBattleGameTestSupport.WEST_FLANK_POS, "Queued Siege Machine");

        CommandIntentDispatcher.dispatch(owner, new CommandIntent.SiegeMachine(owner.level().getGameTime(), CommandIntentPriority.NORMAL, false, MOUNT_UUID, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, false), List.of(immediate));
        CommandIntentDispatcher.dispatch(owner, new CommandIntent.SiegeMachine(owner.level().getGameTime(), CommandIntentPriority.NORMAL, true, MOUNT_UUID, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, false), List.of(queued));

        helper.assertTrue(immediate.getShouldMount() == queued.getShouldMount()
                        && queued.getShouldMount()
                        && MOUNT_UUID.equals(immediate.getMountUUID())
                        && MOUNT_UUID.equals(queued.getMountUUID()),
                "Expected queued siege-machine command to match immediate mount state");
    }

    private static CommandIntent.Movement movement(boolean queued, Vec3 target) {
        return new CommandIntent.Movement(0L, CommandIntentPriority.NORMAL, queued,
                MovementCommandState.MOVE_TO_POSITION, 0, false, target);
    }

    private static ServerPlayer commandSender(ServerLevel level, GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level,
                OWNER_UUID,
                "queue-parity-owner"
        );
        BlockPos pos = helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor());
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, -90.0F, 0.0F);
        player.setYRot(-90.0F);
        return player;
    }

    private static AbstractRecruitEntity recruit(GameTestHelper helper, BlockPos pos, String name) {
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                pos,
                name,
                OWNER_UUID
        );
        RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        return recruit;
    }

    private static AbstractStrategicFireRecruitEntity bowman(GameTestHelper helper, BlockPos pos, String name) {
        AbstractStrategicFireRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.BOWMAN.get(),
                pos,
                name,
                OWNER_UUID
        );
        RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        return recruit;
    }
}
