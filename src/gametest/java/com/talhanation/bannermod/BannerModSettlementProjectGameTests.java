package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingCategory;
import com.talhanation.bannermod.settlement.BannerModSettlementBuildingProfileSeed;
import com.talhanation.bannermod.settlement.growth.PendingProject;
import com.talhanation.bannermod.settlement.growth.ProjectBlocker;
import com.talhanation.bannermod.settlement.growth.ProjectKind;
import com.talhanation.bannermod.settlement.project.AssignmentPhase;
import com.talhanation.bannermod.settlement.project.BannerModBuildAreaProjectBridge;
import com.talhanation.bannermod.settlement.project.BannerModSettlementProjectRuntime;
import com.talhanation.bannermod.settlement.project.ProjectAssignment;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModSettlementProjectGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void settlementProjectBindsToExecutableBuildAreaTarget(GameTestHelper helper) {
        assertSettlementProjectBindsToExecutableBuildAreaTarget(helper);
    }

    static void assertSettlementProjectBindsToExecutableBuildAreaTarget(GameTestHelper helper) {
        WorkAreaIndex.instance().clearAllForTest();
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        BuildArea buildArea = BannerModGameTestSupport.spawnOwnedBuildArea(helper, player, new BlockPos(2, 2, 2));
        buildArea.setStructureNBT(BannerModGameTestSupport.createMinimalBuildTemplate());
        buildArea.setDone(false);

        RecruitsClaim claim = new RecruitsClaim("project-claim", null);
        ChunkPos buildAreaChunk = buildArea.chunkPosition();
        claim.setCenter(buildAreaChunk);
        claim.addChunk(buildAreaChunk);

        PendingProject project = new PendingProject(
                UUID.randomUUID(),
                ProjectKind.NEW_BUILDING,
                null,
                BannerModSettlementBuildingCategory.GENERAL,
                BannerModSettlementBuildingProfileSeed.GENERAL,
                100,
                level.getGameTime(),
                20,
                ProjectBlocker.NONE
        );

        Optional<ProjectAssignment> assignment = BannerModSettlementProjectRuntime.detachedForTests().tickClaim(
                level,
                claim.getUUID(),
                List.of(project),
                new BannerModBuildAreaProjectBridge.ClaimBuildAreaResolver(level, claim),
                level.getGameTime()
        );

        helper.assertTrue(assignment.isPresent(),
                "Expected settlement project creation to bind a live BuildArea instead of leaving a noop placeholder");
        ProjectAssignment resolved = assignment.get();
        helper.assertTrue(buildArea.getUUID().equals(resolved.buildAreaUuid()),
                "Expected project assignment to target the live BuildArea UUID");
        helper.assertTrue(resolved.phase() == AssignmentPhase.SEARCHING_BUILDER,
                "Expected template-backed BuildArea projects to be executable and search for a builder");
        helper.succeed();
    }
}
