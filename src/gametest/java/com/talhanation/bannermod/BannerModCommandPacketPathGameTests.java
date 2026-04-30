package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentLog;
import com.talhanation.bannermod.army.command.MovementCommandState;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.CommandEvents;
import com.talhanation.bannermod.gametest.support.PacketGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.network.messages.military.MessageFaceCommand;
import com.talhanation.bannermod.network.messages.military.MessageFollowGui;
import com.talhanation.bannermod.network.messages.military.MessageFormationMapMoveOrder;
import com.talhanation.bannermod.network.messages.military.MessageMovement;
import com.talhanation.bannermod.network.messages.military.MessageRangedFire;
import com.talhanation.bannermod.network.messages.military.MessageSaveFormationFollowMovement;
import com.talhanation.bannermod.network.messages.military.MessageUpkeepPos;
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
public class BannerModCommandPacketPathGameTests {

    private static final UUID FACE_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000801");
    private static final UUID FACE_OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000802");
    private static final UUID RANGED_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000803");
    private static final UUID RANGED_OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000804");
    private static final UUID UPKEEP_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000805");
    private static final UUID UPKEEP_OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000806");
    private static final UUID MOVEMENT_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000807");
    private static final UUID GUI_MOVEMENT_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000808");
    private static final UUID MAP_MOVE_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000809");
    private static final UUID FORMATION_MAP_MOVE_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000810");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void facePacketPathAcceptsOwnerAndRejectsSpoofedOutsider(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, FACE_OWNER_UUID, "face-owner", helper, -45.0F);
        ServerPlayer outsider = commandSender(level, FACE_OUTSIDER_UUID, "face-outsider", helper, 90.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, FACE_OWNER_UUID, "Face Packet Recruit");

        MessageFaceCommand.dispatchToServer(owner, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, 0, false);

        helper.assertTrue(recruit.getFollowState() == 3,
                "Expected the owner face packet path to place the recruit into face/hold state");
        helper.assertTrue(recruit.rotateTicks == 40 && recruit.ownerRot == owner.getYRot(),
                "Expected the owner face packet path to apply sender rotation to the recruit");

        recruit.setFollowState(0);
        recruit.rotateTicks = 0;
        recruit.ownerRot = 0.0F;

        MessageFaceCommand.dispatchToServer(outsider, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, 0, false);

        helper.assertTrue(recruit.getFollowState() == 0 && recruit.rotateTicks == 0 && recruit.ownerRot == 0.0F,
                "Expected a spoofed outsider face packet to leave the owner recruit unchanged");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void rangedFirePacketPathAcceptsOwnerAndRejectsSpoofedOutsider(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, RANGED_OWNER_UUID, "ranged-owner", helper, -45.0F);
        ServerPlayer outsider = commandSender(level, RANGED_OUTSIDER_UUID, "ranged-outsider", helper, -45.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, RANGED_OWNER_UUID, "Ranged Packet Recruit");

        MessageRangedFire.dispatchToServer(owner, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true);

        helper.assertTrue(recruit.getShouldRanged(),
                "Expected the owner ranged-fire packet path to enable ranged fire on the recruit");

        recruit.setShouldRanged(false);

        MessageRangedFire.dispatchToServer(outsider, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, true);

        helper.assertFalse(recruit.getShouldRanged(),
                "Expected a spoofed outsider ranged-fire packet to leave ranged fire disabled");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void upkeepPacketPathAcceptsOwnerAndRejectsSpoofedOutsider(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, UPKEEP_OWNER_UUID, "upkeep-owner", helper, -45.0F);
        ServerPlayer outsider = commandSender(level, UPKEEP_OUTSIDER_UUID, "upkeep-outsider", helper, -45.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, UPKEEP_OWNER_UUID, "Upkeep Packet Recruit");
        BlockPos upkeepPos = helper.absolutePos(RecruitsBattleGameTestSupport.WEST_FLANK_POS);

        MessageUpkeepPos.dispatchToServer(owner, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, upkeepPos);

        helper.assertTrue(upkeepPos.equals(recruit.getUpkeepPos()),
                "Expected the owner upkeep packet path to assign the recruit upkeep position");

        recruit.clearUpkeepPos();

        MessageUpkeepPos.dispatchToServer(outsider, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, upkeepPos);

        helper.assertTrue(recruit.getUpkeepPos() == null,
                "Expected a spoofed outsider upkeep packet to leave recruit upkeep unchanged");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void movementPacketPathCoversStatesZeroThroughEightViaDispatcher(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, MOVEMENT_OWNER_UUID, "movement-owner", helper, -45.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, MOVEMENT_OWNER_UUID, "Movement Packet Recruit");
        CommandIntentLog.instance().clearFor(owner.getUUID());

        for (int state = 0; state <= 8; state++) {
            recruit.setFollowState(0);
            recruit.isInFormation = true;
            recruit.clearMovePos();
            recruit.setShouldMovePos(false);

            MessageMovement.dispatchToServer(owner, owner.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, state, 0, false);

            CommandIntentLog.Entry entry = CommandIntentLog.instance().recentFor(owner.getUUID()).get(0);
            helper.assertTrue(entry.intent() instanceof CommandIntent.Movement,
                    "Expected movement packet state " + state + " to enter the command-intent dispatcher");
            CommandIntent.Movement movement = (CommandIntent.Movement) entry.intent();
            helper.assertTrue(movement.movementState() == state && movement.targetPos() == null && entry.actorCount() == 1,
                    "Expected movement packet state " + state + " to preserve state and actor count");
            helper.assertFalse(recruit.isInFormation,
                    "Expected non-formation movement state " + state + " to clear formation membership");
            if (state == MovementCommandState.HOLD_OWNER_POSITION) {
                helper.assertTrue(recruit.getFollowState() == MovementCommandState.BACK_TO_POSITION,
                        "Expected hold-owner movement state to preserve the legacy hold-at-owner follow state");
            } else if (state <= MovementCommandState.PROTECT) {
                helper.assertTrue(recruit.getFollowState() == state,
                        "Expected movement state " + state + " to set the matching recruit follow state");
            }
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void guiMovementPacketPathUsesDispatcher(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, GUI_MOVEMENT_OWNER_UUID, "gui-movement-owner", helper, -45.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, GUI_MOVEMENT_OWNER_UUID, "Gui Movement Recruit");
        CommandIntentLog.instance().clearFor(owner.getUUID());

        MessageFollowGui.dispatchToServer(owner, recruit.getUUID(), MovementCommandState.FOLLOW);

        CommandIntentLog.Entry entry = CommandIntentLog.instance().recentFor(owner.getUUID()).get(0);
        helper.assertTrue(entry.intent() instanceof CommandIntent.Movement,
                "Expected GUI movement packet to enter the command-intent dispatcher");
        CommandIntent.Movement movement = (CommandIntent.Movement) entry.intent();
        helper.assertTrue(movement.movementState() == MovementCommandState.FOLLOW && movement.formation() == 0 && entry.actorCount() == 1,
                "Expected GUI movement packet to dispatch a single-recruit non-formation movement intent");
        helper.assertTrue(recruit.getFollowState() == MovementCommandState.FOLLOW,
                "Expected GUI movement packet to preserve follow-state semantics");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void formationMapMovePacketPreservesExplicitTargetViaDispatcher(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, MAP_MOVE_OWNER_UUID, "map-move-owner", helper, -45.0F);
        AbstractRecruitEntity recruit = commandRecruit(helper, MAP_MOVE_OWNER_UUID, "Map Move Recruit");
        BlockPos target = helper.absolutePos(new BlockPos(7, 2, 7));
        int expectedY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
        BlockPos expectedMoveTarget = new BlockPos(target.getX(), expectedY, target.getZ());
        CommandIntentLog.instance().clearFor(owner.getUUID());

        MessageFormationMapMoveOrder.dispatchToServer(owner, recruit.getUUID(), null, target);

        CommandIntentLog.Entry entry = CommandIntentLog.instance().recentFor(owner.getUUID()).get(0);
        helper.assertTrue(entry.intent() instanceof CommandIntent.Movement,
                "Expected formation map move packet to enter the command-intent dispatcher");
        CommandIntent.Movement movement = (CommandIntent.Movement) entry.intent();
        helper.assertTrue(movement.movementState() == MovementCommandState.MOVE_TO_POSITION && movement.targetPos() != null,
                "Expected map move packet to preserve explicit move-to-position target");
        helper.assertTrue(expectedMoveTarget.equals(recruit.getMovePos()) && recruit.getShouldMovePos(),
                "Expected explicit map target to set recruit move position without a player pick lookup");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "vanilla011_movement_regression")
    public static void formationMapMovePacketAppliesSavedFormationViaDispatcher(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, FORMATION_MAP_MOVE_OWNER_UUID, "formation-map-move-owner", helper, -45.0F);
        AbstractRecruitEntity first = commandRecruit(helper, FORMATION_MAP_MOVE_OWNER_UUID, "Formation Map Move Recruit A");
        AbstractRecruitEntity second = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FLANK_POS,
                "Formation Map Move Recruit B",
                FORMATION_MAP_MOVE_OWNER_UUID
        );
        RecruitsCommandGameTestSupport.prepareForCommand(second, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        BlockPos target = helper.absolutePos(new BlockPos(8, 2, 8));
        int expectedY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
        Vec3 expectedCenter = Vec3.atCenterOf(new BlockPos(target.getX(), expectedY, target.getZ()));
        CommandEvents.saveFormation(owner, 1);
        CommandIntentLog.instance().clearFor(owner.getUUID());

        MessageFormationMapMoveOrder.dispatchToServer(owner, first.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, target);

        CommandIntentLog.Entry entry = CommandIntentLog.instance().recentFor(owner.getUUID()).get(0);
        helper.assertTrue(entry.intent() instanceof CommandIntent.Movement,
                "Expected saved-formation map move to enter the command-intent dispatcher");
        CommandIntent.Movement movement = (CommandIntent.Movement) entry.intent();
        helper.assertTrue(movement.movementState() == MovementCommandState.MOVE_TO_POSITION
                        && movement.formation() == 1
                        && movement.targetPos() != null
                        && entry.actorCount() == 2,
                "Expected map move to preserve saved formation, explicit target, and group actor count");
        helper.assertTrue(first.isInFormation && second.isInFormation,
                "Expected explicit target movement with saved formation to apply formation membership");
        helper.assertTrue(first.getFollowState() == MovementCommandState.BACK_TO_POSITION
                        && second.getFollowState() == MovementCommandState.BACK_TO_POSITION,
                "Expected formation movement to set recruits to return-to-formation follow state");
        helper.assertTrue(first.getHoldPos().distanceToSqr(expectedCenter) < 64.0D
                        && second.getHoldPos().distanceToSqr(expectedCenter) < 64.0D,
                "Expected formation slots to be placed near the explicit map target");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "vanilla011_movement_regression")
    public static void saveFormationAndMapMovePacketsUseWirePathAndSavedPreferences(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = commandSender(level, FORMATION_MAP_MOVE_OWNER_UUID, "formation-wire-owner", helper, -45.0F);
        AbstractRecruitEntity first = commandRecruit(helper, FORMATION_MAP_MOVE_OWNER_UUID, "Formation Wire Recruit A");
        AbstractRecruitEntity second = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FLANK_POS,
                "Formation Wire Recruit B",
                FORMATION_MAP_MOVE_OWNER_UUID
        );
        RecruitsCommandGameTestSupport.prepareForCommand(second, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        RecruitsBattleGameTestSupport.assignFormationCohort(List.of(first, second), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        BlockPos target = helper.absolutePos(new BlockPos(9, 2, 9));
        int expectedY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
        Vec3 expectedCenter = Vec3.atCenterOf(new BlockPos(target.getX(), expectedY, target.getZ()));
        CommandIntentLog.instance().clearFor(owner.getUUID());

        PacketGameTestSupport.dispatchServerbound(owner,
                new MessageSaveFormationFollowMovement(owner.getUUID(), List.of(RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, UUID.randomUUID()), 1),
                MessageSaveFormationFollowMovement::new);

        helper.assertTrue(CommandEvents.getSavedFormation(owner) == 1,
                "Expected save-formation packet to persist the sender's saved formation");
        helper.assertTrue(CommandEvents.getSavedUUIDList(owner, "ActiveGroups").equals(List.of(RecruitsCommandGameTestSupport.TARGET_GROUP_UUID)),
                "Expected save-formation packet to persist only owned active groups");

        PacketGameTestSupport.dispatchServerbound(owner,
                new MessageFormationMapMoveOrder(first.getUUID(), RecruitsCommandGameTestSupport.TARGET_GROUP_UUID, target),
                MessageFormationMapMoveOrder::new);

        CommandIntentLog.Entry entry = CommandIntentLog.instance().recentFor(owner.getUUID()).get(0);
        helper.assertTrue(entry.intent() instanceof CommandIntent.Movement,
                "Expected wire-decoded map move packet to enter the command-intent dispatcher");
        CommandIntent.Movement movement = (CommandIntent.Movement) entry.intent();
        helper.assertTrue(movement.movementState() == MovementCommandState.MOVE_TO_POSITION
                        && movement.formation() == 1
                        && movement.targetPos() != null
                        && entry.actorCount() == 2,
                "Expected wire-decoded map move packet to preserve saved formation, explicit target, and actor count");
        helper.assertTrue(first.isInFormation && second.isInFormation,
                "Expected saved-formation wire path to apply formation membership");
        helper.assertTrue(first.getFollowState() == MovementCommandState.BACK_TO_POSITION
                        && second.getFollowState() == MovementCommandState.BACK_TO_POSITION,
                "Expected saved-formation wire path to preserve formation follow-state semantics");
        helper.assertTrue(first.getHoldPos().distanceToSqr(expectedCenter) < 64.0D
                        && second.getHoldPos().distanceToSqr(expectedCenter) < 64.0D,
                "Expected saved-formation wire path to place formation slots near the explicit target");
        helper.succeed();
    }

    private static ServerPlayer commandSender(ServerLevel level, UUID playerId, String name, GameTestHelper helper, float yRot) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(
                level,
                playerId,
                name
        );
        BlockPos pos = helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor());
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yRot, 0.0F);
        player.setYRot(yRot);
        return player;
    }

    private static AbstractRecruitEntity commandRecruit(GameTestHelper helper, UUID ownerId, String name) {
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.bannermod.registry.military.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                name,
                ownerId
        );
        RecruitsCommandGameTestSupport.prepareForCommand(recruit, RecruitsCommandGameTestSupport.TARGET_GROUP_UUID);
        return recruit;
    }
}
