package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.military.navigation.RecruitsPathNodeEvaluator;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.RecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModPathNodeEvaluatorGameTests {
    private static final BlockPos RECRUIT_POS = new BlockPos(1, 2, 1);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "path_node_evaluator")
    public static void recruitEvaluatorPreservesCorePathTypes(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        RecruitEntity recruit = BannerModGameTestSupport.spawnOwnedRecruit(helper, owner, RECRUIT_POS);
        ServerLevel level = helper.getLevel();

        BlockPos landPos = helper.absolutePos(new BlockPos(3, 2, 1));
        level.setBlockAndUpdate(landPos.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        assertPathType(helper, level, recruit, landPos, PathType.WALKABLE);
        assertPathType(helper, level, recruit, new BlockPos(4, 2, 1), Blocks.WATER.defaultBlockState(), PathType.WATER);
        assertPathType(helper, level, recruit, new BlockPos(5, 2, 1), Blocks.OAK_DOOR.defaultBlockState(), PathType.DOOR_WOOD_CLOSED);
        assertPathType(helper, level, recruit, new BlockPos(6, 2, 1), Blocks.OAK_FENCE.defaultBlockState(), PathType.FENCE);
        assertPathType(helper, level, recruit, new BlockPos(7, 2, 1), Blocks.LAVA.defaultBlockState(), PathType.LAVA);

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", batch = "path_node_evaluator")
    public static void recruitEvaluatorAppliesRecruitTraversalCosts(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        RecruitEntity recruit = BannerModGameTestSupport.spawnOwnedRecruit(helper, owner, RECRUIT_POS);

        RecruitsPathNodeEvaluator evaluator = prepareEvaluator(helper.getLevel(), recruit);
        evaluator.done();

        helper.assertTrue(recruit.getPathfindingMalus(PathType.WATER) == 128.0F, "Expected recruits to heavily avoid water.");
        helper.assertTrue(recruit.getPathfindingMalus(PathType.DOOR_WOOD_CLOSED) == 0.0F, "Expected recruits to pass closed wooden doors.");
        helper.assertTrue(recruit.getPathfindingMalus(PathType.FENCE) < 0.0F, "Expected fences to remain blocked for recruits.");
        helper.assertTrue(recruit.getPathfindingMalus(PathType.DAMAGE_FIRE) == 32.0F, "Expected fire hazards to remain costly.");
        helper.assertTrue(recruit.getPathfindingMalus(PathType.LAVA) < 0.0F, "Expected lava to remain blocked.");

        helper.succeed();
    }

    private static void assertPathType(GameTestHelper helper, ServerLevel level, RecruitEntity recruit, BlockPos relativePos,
                                       BlockState state, PathType expected) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        level.setBlockAndUpdate(absolutePos, state);

        assertPathType(helper, level, recruit, absolutePos, expected);
    }

    private static void assertPathType(GameTestHelper helper, ServerLevel level, RecruitEntity recruit, BlockPos absolutePos,
                                       PathType expected) {

        RecruitsPathNodeEvaluator evaluator = prepareEvaluator(level, recruit);
        PathType actual = evaluator.getPathType(new PathfindingContext(level, recruit), absolutePos.getX(), absolutePos.getY(), absolutePos.getZ());
        evaluator.done();

        helper.assertTrue(actual == expected, "Expected " + expected + " at " + absolutePos + " but got " + actual + ".");
    }

    private static RecruitsPathNodeEvaluator prepareEvaluator(ServerLevel level, RecruitEntity recruit) {
        BlockPos center = recruit.blockPosition();
        PathNavigationRegion region = new PathNavigationRegion(level, center.offset(-8, -4, -8), center.offset(8, 4, 8));
        RecruitsPathNodeEvaluator evaluator = new RecruitsPathNodeEvaluator();
        evaluator.setCanOpenDoors(true);
        evaluator.setCanPassDoors(true);
        evaluator.setCanFloat(true);
        evaluator.prepare(region, recruit);
        return evaluator;
    }
}
