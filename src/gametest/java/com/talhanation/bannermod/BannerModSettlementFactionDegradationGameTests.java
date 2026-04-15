package com.talhanation.bannermod;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.CropArea;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Main.MOD_ID)
public class BannerModSettlementFactionDegradationGameTests {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000001021");
    private static final UUID CLAIM_HOLDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000001022");
    private static final String OWNER_TEAM_ID = "phase10_owner";
    private static final String CLAIM_HOLDER_TEAM_ID = "phase10_claimholder";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void claimLossDegradesSettlementWithoutTransferringOwnership(GameTestHelper helper) {
        SettlementSetup setup = createFriendlySettlement(helper, new BlockPos(2, 2, 2));
        UUID originalAreaOwner = setup.cropArea.getPlayerUUID();
        UUID originalWorkerOwner = setup.worker.getOwnerUUID();
        String originalTeamId = setup.cropArea.getTeamStringID();

        BannerModDedicatedServerGameTestSupport.removeClaim(setup.level, setup.claim);

        helper.assertTrue(setup.cropArea.getSettlementBinding().status() == BannerModSettlementBinding.Status.UNCLAIMED,
                "Expected removing the live claim to degrade the settlement footprint to UNCLAIMED");
        helper.assertFalse(setup.cropArea.canWorkHere(setup.worker),
                "Expected claim loss to stop civilian throughput through the shared settlement operation gate");
        helper.assertTrue(originalAreaOwner.equals(setup.cropArea.getPlayerUUID()),
                "Expected claim loss to preserve the work-area owner UUID instead of silently clearing or transferring it");
        helper.assertTrue(originalWorkerOwner.equals(setup.worker.getOwnerUUID()),
                "Expected claim loss to preserve the worker owner UUID instead of transferring control");
        helper.assertTrue(originalTeamId.equals(setup.cropArea.getTeamStringID()),
                "Expected claim loss to preserve the stored settlement faction identity even after throughput degrades");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void factionMismatchDegradesSettlementWithoutSilentRebinding(GameTestHelper helper) {
        SettlementSetup setup = createFriendlySettlement(helper, new BlockPos(2, 2, 2));
        UUID originalAreaOwner = setup.cropArea.getPlayerUUID();
        UUID originalWorkerOwner = setup.worker.getOwnerUUID();
        String originalTeamId = setup.cropArea.getTeamStringID();

        BannerModDedicatedServerGameTestSupport.swapClaimFaction(
                setup.level,
                setup.claim,
                CLAIM_HOLDER_TEAM_ID,
                CLAIM_HOLDER_UUID,
                "claim-holder"
        );

        BannerModSettlementBinding.Binding binding = setup.cropArea.getSettlementBinding();
        helper.assertTrue(binding.status() == BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
                "Expected swapping the live claim owner faction to degrade the settlement footprint to DEGRADED_MISMATCH");
        helper.assertTrue(CLAIM_HOLDER_TEAM_ID.equals(binding.claimFactionId()),
                "Expected the degraded binding to point at the new live claim holder rather than the original settlement faction");
        helper.assertFalse(setup.cropArea.canWorkHere(setup.worker),
                "Expected faction mismatch to stop civilian throughput through the shared settlement operation gate");
        helper.assertTrue(originalAreaOwner.equals(setup.cropArea.getPlayerUUID()),
                "Expected faction mismatch to preserve the work-area owner UUID instead of silently transferring it");
        helper.assertTrue(originalWorkerOwner.equals(setup.worker.getOwnerUUID()),
                "Expected faction mismatch to preserve the worker owner UUID instead of silently transferring it");
        helper.assertTrue(originalTeamId.equals(setup.cropArea.getTeamStringID()),
                "Expected faction mismatch to preserve the stored settlement faction identity rather than rebinding it to the new claim holder");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void degradedSettlementKeepsRecoveryAuthorityWithOriginalOwner(GameTestHelper helper) {
        SettlementSetup setup = createFriendlySettlement(helper, new BlockPos(2, 2, 2));
        Player claimHolder = createPlayer(helper, setup.level, CLAIM_HOLDER_UUID, "claim-holder", CLAIM_HOLDER_TEAM_ID);

        BannerModDedicatedServerGameTestSupport.swapClaimFaction(
                setup.level,
                setup.claim,
                CLAIM_HOLDER_TEAM_ID,
                CLAIM_HOLDER_UUID,
                claimHolder.getScoreboardName()
        );

        helper.assertTrue(setup.cropArea.getSettlementBinding().status() == BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
                "Expected the recovery authority check to run after the settlement has already degraded to a mismatch state");
        helper.assertFalse(setup.worker.recoverControl(claimHolder),
                "Expected the new claim holder to remain unable to recover worker control without explicit ownership authority");
        helper.assertTrue(setup.cropArea.isBeingWorkedOn(),
                "Expected denied recovery from the new claim holder to preserve the existing work-area binding");
        helper.assertTrue(setup.worker.getCurrentWorkArea() == setup.cropArea,
                "Expected denied recovery from the new claim holder to avoid silently rebinding or clearing the work area");
        helper.assertTrue(setup.worker.recoverControl(setup.owner),
                "Expected the original explicit owner to retain recovery authority even after the settlement degrades");

        helper.succeed();
    }

    private static SettlementSetup createFriendlySettlement(GameTestHelper helper, BlockPos workAreaPos) {
        ServerLevel level = helper.getLevel();
        Player owner = createPlayer(helper, level, OWNER_UUID, "settlement-owner", OWNER_TEAM_ID);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, OWNER_TEAM_ID, OWNER_UUID, owner.getScoreboardName());
        RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(
                level,
                helper.absolutePos(workAreaPos),
                OWNER_TEAM_ID,
                OWNER_UUID,
                owner.getScoreboardName()
        );

        CropArea cropArea = BannerModGameTestSupport.spawnOwnedCropArea(helper, owner, workAreaPos);
        cropArea.setTeamStringID(OWNER_TEAM_ID);
        FarmerEntity worker = BannerModGameTestSupport.spawnOwnedFarmer(helper, owner, workAreaPos.offset(2, 0, 0));
        BannerModDedicatedServerGameTestSupport.joinTeam(level, OWNER_TEAM_ID, worker);
        worker.currentCropArea = cropArea;
        cropArea.setBeingWorkedOn(true);

        helper.assertTrue(cropArea.getSettlementBinding().status() == BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                "Expected the degradation tests to begin from a friendly-claim settlement setup");
        helper.assertTrue(cropArea.canWorkHere(worker),
                "Expected the degradation tests to begin from an operational settlement before mutating the live claim");

        return new SettlementSetup(level, owner, claim, cropArea, worker);
    }

    private static Player createPlayer(GameTestHelper helper, ServerLevel level, UUID playerId, String name, String teamId) {
        Player player = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, playerId, name);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, teamId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, teamId, player);
        BlockPos spawnPos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        return player;
    }

    private record SettlementSetup(ServerLevel level, Player owner, RecruitsClaim claim, CropArea cropArea, FarmerEntity worker) {
    }
}
