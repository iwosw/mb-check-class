package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModClaimProtectionGameTests {

    private static final UUID FRIENDLY_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002601");
    private static final UUID HOSTILE_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002602");
    private static final String FRIENDLY_TEAM_ID = "phase260413_claims_friendly";
    private static final String HOSTILE_TEAM_ID = "phase260413_claims_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileLavaPlacementAndSpreadCannotReachProtectedClaim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 2)), FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, helper.absolutePos(new BlockPos(15, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);
        BlockPos protectedPos = helper.absolutePos(new BlockPos(14, 2, 2));
        BlockPos spreadTargetPos = helper.absolutePos(new BlockPos(13, 2, 2));
        BlockPos sourcePos = helper.absolutePos(new BlockPos(15, 2, 2));

        BannerModDedicatedServerGameTestSupport.seedClaim(level, protectedPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(sourcePos, Blocks.STONE.defaultBlockState());

        BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                BlockSnapshot.create(level.dimension(), level, sourcePos),
                Blocks.LAVA.defaultBlockState(),
                hostile
        );
        MinecraftForge.EVENT_BUS.post(placeEvent);

        helper.assertTrue(placeEvent.isCanceled(),
                "Expected hostile lava placement from the claim edge to be denied before lava can source into a protected claim");

        level.setBlockAndUpdate(sourcePos, Blocks.LAVA.defaultBlockState());
        BlockEvent.FluidPlaceBlockEvent spreadEvent = new BlockEvent.FluidPlaceBlockEvent(
                level,
                spreadTargetPos,
                protectedPos,
                Blocks.LAVA.defaultBlockState()
        );
        MinecraftForge.EVENT_BUS.post(spreadEvent);

        helper.assertTrue(spreadEvent.isCanceled(),
                "Expected hostile lava spread into a protected claim to be denied even when the source starts outside the claim");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileContainerUseCannotOpenChestInsideFriendlyClaim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 2)), FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, helper.absolutePos(new BlockPos(3, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);
        BlockPos chestPos = helper.absolutePos(new BlockPos(4, 2, 2));

        BannerModDedicatedServerGameTestSupport.seedClaim(level, chestPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        chest.setItem(0, Items.DIAMOND.getDefaultInstance());

        PlayerInteractEvent.RightClickBlock interactEvent = new PlayerInteractEvent.RightClickBlock(
                hostile,
                InteractionHand.MAIN_HAND,
                chestPos,
                BlockHitResult.miss(Vec3.atCenterOf(chestPos), net.minecraft.core.Direction.NORTH, chestPos)
        );
        MinecraftForge.EVENT_BUS.post(interactEvent);

        helper.assertTrue(interactEvent.isCanceled(),
                "Expected hostile chest interaction to be denied when the targeted container block entity is inside a friendly claim");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileBorderMutationCannotPlaceOrBreakProtectedBlockFromClaimEdge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 2)), FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, helper.absolutePos(new BlockPos(15, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);
        BlockPos protectedPos = helper.absolutePos(new BlockPos(15, 2, 2));

        BannerModDedicatedServerGameTestSupport.seedClaim(level, protectedPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(protectedPos, Blocks.STONE.defaultBlockState());

        for (int attempt = 0; attempt < 3; attempt++) {
            BlockState placedState = Blocks.OAK_PLANKS.defaultBlockState();
            BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                    BlockSnapshot.create(level.dimension(), level, protectedPos),
                    placedState,
                    hostile
            );
            MinecraftForge.EVENT_BUS.post(placeEvent);
            helper.assertTrue(placeEvent.isCanceled(),
                    "Expected hostile border block placement attempts to stay denied even when repeated from the claim edge");

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, protectedPos, level.getBlockState(protectedPos), hostile);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            helper.assertTrue(breakEvent.isCanceled(),
                    "Expected hostile border block break attempts to stay denied even when repeated from the claim edge");
        }

        helper.succeed();
    }

    private static ServerPlayer createPlayer(ServerLevel level, BlockPos spawnPos, UUID playerId, String name, String teamId) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createPositionedFakeServerPlayer(level, playerId, name, spawnPos);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, teamId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, teamId, player);
        return player;
    }
}
