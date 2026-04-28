package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModClaimProtectionGameTests {

    private static final UUID FRIENDLY_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002601");
    private static final UUID HOSTILE_PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000002602");
    private static final String FRIENDLY_TEAM_ID = "phase260413_claims_friendly";
    private static final String HOSTILE_TEAM_ID = "phase260413_claims_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileLavaPlacementAndSpreadCannotReachProtectedClaim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos claimAnchor = helper.absolutePos(new BlockPos(2, 2, 2));
        ChunkPos claimedChunk = new ChunkPos(claimAnchor);
        BlockPos protectedPos = new BlockPos(claimedChunk.getMaxBlockX(), claimAnchor.getY(), claimAnchor.getZ());
        BlockPos spreadTargetPos = protectedPos.west();
        BlockPos sourcePos = protectedPos.east();
        ServerPlayer owner = createPlayer(level, claimAnchor, FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, sourcePos, HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);

        BannerModDedicatedServerGameTestSupport.seedClaim(level, protectedPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(sourcePos, Blocks.STONE.defaultBlockState());
        com.talhanation.bannermod.events.ClaimEvents claimEvents = new com.talhanation.bannermod.events.ClaimEvents();

        BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                BlockSnapshot.create(level.dimension(), level, protectedPos),
                Blocks.LAVA.defaultBlockState(),
                hostile
        );
        claimEvents.onBlockPlaceEvent(placeEvent);

        helper.assertTrue(placeEvent.isCanceled(),
                "Expected hostile lava placement into a protected border block to be denied even when the player starts outside the claim");

        level.setBlockAndUpdate(sourcePos, Blocks.LAVA.defaultBlockState());
        BlockEvent.FluidPlaceBlockEvent spreadEvent = new BlockEvent.FluidPlaceBlockEvent(
                level,
                spreadTargetPos,
                sourcePos,
                Blocks.LAVA.defaultBlockState()
        );
        claimEvents.onFluidPlaceBlockEvent(spreadEvent);

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
        new com.talhanation.bannermod.events.ClaimEvents().onBlockInteract(interactEvent);

        helper.assertTrue(interactEvent.isCanceled(),
                "Expected hostile chest interaction to be denied when the targeted container block entity is inside a friendly claim");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileGenericUseAndBucketClickAreDeniedInsideFriendlyClaim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 2)), FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, helper.absolutePos(new BlockPos(3, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);
        BlockPos craftingPos = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos bucketTargetPos = helper.absolutePos(new BlockPos(5, 2, 2));

        BannerModDedicatedServerGameTestSupport.seedClaim(level, craftingPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(craftingPos, Blocks.CRAFTING_TABLE.defaultBlockState());
        level.setBlockAndUpdate(bucketTargetPos, Blocks.STONE.defaultBlockState());
        hostile.setItemInHand(InteractionHand.MAIN_HAND, Items.STICK.getDefaultInstance());

        com.talhanation.bannermod.events.ClaimEvents claimEvents = new com.talhanation.bannermod.events.ClaimEvents();
        PlayerInteractEvent.RightClickBlock genericUseEvent = new PlayerInteractEvent.RightClickBlock(
                hostile,
                InteractionHand.MAIN_HAND,
                craftingPos,
                BlockHitResult.miss(Vec3.atCenterOf(craftingPos), net.minecraft.core.Direction.NORTH, craftingPos)
        );
        claimEvents.onBlockInteract(genericUseEvent);
        helper.assertTrue(genericUseEvent.isCanceled(),
                "Expected hostile right-click use on a non-container usable block inside a friendly claim to be denied");

        hostile.setItemInHand(InteractionHand.MAIN_HAND, Items.WATER_BUCKET.getDefaultInstance());
        PlayerInteractEvent.RightClickBlock bucketUseEvent = new PlayerInteractEvent.RightClickBlock(
                hostile,
                InteractionHand.MAIN_HAND,
                bucketTargetPos,
                BlockHitResult.miss(Vec3.atCenterOf(bucketTargetPos), net.minecraft.core.Direction.NORTH, bucketTargetPos)
        );
        claimEvents.onBlockInteract(bucketUseEvent);
        helper.assertTrue(bucketUseEvent.isCanceled(),
                "Expected hostile bucket placement clicks inside a friendly claim to be denied before fluid can be placed");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileBorderMutationCannotPlaceOrBreakProtectedBlockFromClaimEdge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos claimAnchor = helper.absolutePos(new BlockPos(2, 2, 2));
        ChunkPos claimedChunk = new ChunkPos(claimAnchor);
        BlockPos protectedPos = new BlockPos(claimedChunk.getMaxBlockX(), claimAnchor.getY(), claimAnchor.getZ());
        ServerPlayer owner = createPlayer(level, claimAnchor, FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, protectedPos.east(), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);

        BannerModDedicatedServerGameTestSupport.seedClaim(level, protectedPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        level.setBlockAndUpdate(protectedPos, Blocks.STONE.defaultBlockState());

        for (int attempt = 0; attempt < 3; attempt++) {
            BlockState placedState = Blocks.OAK_PLANKS.defaultBlockState();
            BlockEvent.EntityPlaceEvent placeEvent = new BlockEvent.EntityPlaceEvent(
                    BlockSnapshot.create(level.dimension(), level, protectedPos),
                    placedState,
                    hostile
            );
            new com.talhanation.bannermod.events.ClaimEvents().onBlockPlaceEvent(placeEvent);
            helper.assertTrue(placeEvent.isCanceled(),
                    "Expected hostile border block placement attempts to stay denied even when repeated from the claim edge");

            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, protectedPos, level.getBlockState(protectedPos), hostile);
            new com.talhanation.bannermod.events.ClaimEvents().onBlockBreakEvent(breakEvent);
            helper.assertTrue(breakEvent.isCanceled(),
                    "Expected hostile border block break attempts to stay denied even when repeated from the claim edge");
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileAttackCannotHitOwnerInsideFriendlyClaim(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer owner = createPlayer(level, claimPos, FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer hostile = createPlayer(level, helper.absolutePos(new BlockPos(3, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);

        BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());

        AttackEntityEvent attackEvent = new AttackEntityEvent(hostile, owner);
        new com.talhanation.bannermod.events.ClaimEvents().onAttackEntity(attackEvent);

        helper.assertTrue(attackEvent.isCanceled(),
                "Expected hostile direct attacks against entities inside a friendly claim to be denied outside siege state");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void overworldClaimDoesNotProtectMatchingNetherCoordinates(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "Expected Nether level to exist for dimension-aware claim protection test");
        BlockPos claimPos = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer owner = createPlayer(overworld, claimPos, FRIENDLY_OWNER_UUID, "claim-owner", FRIENDLY_TEAM_ID);
        ServerPlayer overworldHostile = createPlayer(overworld, helper.absolutePos(new BlockPos(3, 2, 2)), HOSTILE_PLAYER_UUID, "claim-hostile", HOSTILE_TEAM_ID);
        ServerPlayer netherHostile = createPlayer(nether, claimPos, HOSTILE_PLAYER_UUID, "claim-hostile-nether", HOSTILE_TEAM_ID);

        BannerModDedicatedServerGameTestSupport.seedClaim(overworld, claimPos, FRIENDLY_TEAM_ID, owner.getUUID(), owner.getScoreboardName());
        overworld.setBlockAndUpdate(claimPos, Blocks.CRAFTING_TABLE.defaultBlockState());
        nether.setBlockAndUpdate(claimPos, Blocks.CRAFTING_TABLE.defaultBlockState());

        PlayerInteractEvent.RightClickBlock overworldUse = new PlayerInteractEvent.RightClickBlock(
                overworldHostile,
                InteractionHand.MAIN_HAND,
                claimPos,
                BlockHitResult.miss(Vec3.atCenterOf(claimPos), net.minecraft.core.Direction.NORTH, claimPos)
        );
        PlayerInteractEvent.RightClickBlock netherUse = new PlayerInteractEvent.RightClickBlock(
                netherHostile,
                InteractionHand.MAIN_HAND,
                claimPos,
                BlockHitResult.miss(Vec3.atCenterOf(claimPos), net.minecraft.core.Direction.NORTH, claimPos)
        );

        com.talhanation.bannermod.events.ClaimEvents claimEvents = new com.talhanation.bannermod.events.ClaimEvents();
        claimEvents.onBlockInteract(overworldUse);
        claimEvents.onBlockInteract(netherUse);

        helper.assertTrue(overworldUse.isCanceled(),
                "Expected hostile Overworld interaction inside the protected claim to be denied");
        helper.assertFalse(netherUse.isCanceled(),
                "Expected matching Nether X/Z interaction to ignore the Overworld claim");
        helper.succeed();
    }

    private static ServerPlayer createPlayer(ServerLevel level, BlockPos spawnPos, UUID playerId, String name, String teamId) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createPositionedFakeServerPlayer(level, playerId, name, spawnPos);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, teamId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, teamId, player);
        return player;
    }
}
