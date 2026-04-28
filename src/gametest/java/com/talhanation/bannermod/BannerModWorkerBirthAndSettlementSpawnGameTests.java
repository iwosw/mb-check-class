package com.talhanation.bannermod;

import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.events.WorkersVillagerEvents;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModWorkerBirthAndSettlementSpawnGameTests {

    private static final UUID FRIENDLY_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000003001");
    private static final UUID HOSTILE_LEADER_UUID = UUID.fromString("00000000-0000-0000-0000-000000003002");
    private static final String FRIENDLY_TEAM_ID = "phase30_friendly";
    private static final String HOSTILE_TEAM_ID = "phase30_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void friendlyClaimBirthCreatesOwnedSettlementWorker(GameTestHelper helper) {
        configurePhase30ForTests();

        try {
            ServerLevel level = helper.getLevel();
            Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, leader);

            BlockPos villagerPos = helper.absolutePos(new BlockPos(2, 2, 2));
            BannerModDedicatedServerGameTestSupport.seedClaim(level, villagerPos, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            Villager villager = BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(2, 2, 2), "phase30-birth-villager");

            AbstractWorkerEntity worker = WorkersVillagerEvents.attemptBirthWorkerSpawn(level, villager);

            helper.assertTrue(worker != null, "Expected friendly-claim birth conversion to create a settlement worker through the live VillagerEvents seam");
            helper.assertTrue(worker != null && FRIENDLY_LEADER_UUID.equals(worker.getOwnerUUID()), "Expected the settlement-born worker to inherit the claim leader as owner");
            helper.assertTrue(worker != null && worker.getTeam() != null && FRIENDLY_TEAM_ID.equals(worker.getTeam().getName()), "Expected the settlement-born worker to join the friendly claim team");
            helper.assertTrue(BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, worker.blockPosition(), FRIENDLY_TEAM_ID).status() == BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                    "Expected the live shared settlement binding seam to remain FRIENDLY_CLAIM for the spawned worker");
        } finally {
            clearPhase30TestOverrides();
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void claimSpawnCreatesOneWorkerThenRespectsCooldown(GameTestHelper helper) {
        configurePhase30ForTests();

        try {
            ServerLevel level = helper.getLevel();
            Player leader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, UUID.fromString("00000000-0000-0000-0000-000000003003"), "phase30-spawn-leader");
            BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID + "_cooldown", leader.getUUID(), "phase30-spawn-leader");
            BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID + "_cooldown", leader);

            BlockPos firstPos = helper.absolutePos(new BlockPos(2, 2, 2));
            RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(level, firstPos, FRIENDLY_TEAM_ID + "_cooldown", leader.getUUID(), "phase30-spawn-leader");
            Villager firstVillager = BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(2, 2, 2), "phase30-first-spawn-villager");
            Villager secondVillager = BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(4, 2, 2), "phase30-second-spawn-villager");

            AbstractWorkerEntity firstWorker = WorkersVillagerEvents.attemptSettlementWorkerSpawn(level, firstVillager);
            AbstractWorkerEntity secondWorker = WorkersVillagerEvents.attemptSettlementWorkerSpawn(level, secondVillager);

            helper.assertTrue(firstWorker != null, "Expected friendly claim autonomous spawning to create one worker through the runtime seam");
            helper.assertTrue(secondWorker == null, "Expected the configured cooldown to block a second autonomous spawn in the same claim immediately after the first");
            helper.assertTrue(BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, claim.getCenter().getWorldPosition(), FRIENDLY_TEAM_ID + "_cooldown").status() == BannerModSettlementBinding.Status.FRIENDLY_CLAIM,
                    "Expected the cooldown-bounded spawn scenario to stay rooted in a friendly claim binding");
        } finally {
            clearPhase30TestOverrides();
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void hostileOrUnclaimedSettlementsNeverCreateWorkers(GameTestHelper helper) {
        configurePhase30ForTests();

        try {
            ServerLevel level = helper.getLevel();
            Player friendlyLeader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            Player hostileLeader = BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, HOSTILE_LEADER_UUID, "phase30-hostile-leader");
            BannerModDedicatedServerGameTestSupport.ensureFaction(level, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            BannerModDedicatedServerGameTestSupport.ensureFaction(level, HOSTILE_TEAM_ID, HOSTILE_LEADER_UUID, "phase30-hostile-leader");
            BannerModDedicatedServerGameTestSupport.joinTeam(level, FRIENDLY_TEAM_ID, friendlyLeader);
            BannerModDedicatedServerGameTestSupport.joinTeam(level, HOSTILE_TEAM_ID, hostileLeader);

            BlockPos hostilePos = helper.absolutePos(new BlockPos(2, 2, 2));
            BannerModDedicatedServerGameTestSupport.seedClaim(level, hostilePos, FRIENDLY_TEAM_ID, FRIENDLY_LEADER_UUID, "phase30-friendly-leader");
            Villager hostileVillager = BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(2, 2, 2), "phase30-hostile-villager");
            BannerModDedicatedServerGameTestSupport.joinTeam(level, HOSTILE_TEAM_ID, hostileVillager);

            AbstractWorkerEntity hostileWorker = WorkersVillagerEvents.attemptBirthWorkerSpawn(level, hostileVillager);
            BannerModSettlementBinding.Binding hostileBinding = BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, hostilePos, HOSTILE_TEAM_ID);

            BlockPos unclaimedPos = helper.absolutePos(new BlockPos(18, 2, 2));
            Villager unclaimedVillager = BannerModGameTestSupport.spawnVillagerWithMemories(helper, new BlockPos(18, 2, 2), "phase30-unclaimed-villager");
            AbstractWorkerEntity unclaimedWorker = WorkersVillagerEvents.attemptSettlementWorkerSpawn(level, unclaimedVillager);
            BannerModSettlementBinding.Binding unclaimedBinding = BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, unclaimedPos, FRIENDLY_TEAM_ID);

            helper.assertTrue(hostileWorker == null, "Expected hostile-claim villager conversion to be denied before any worker entity is created");
            helper.assertTrue(hostileBinding.status() == BannerModSettlementBinding.Status.DEGRADED_MISMATCH,
                    "Expected hostile claim conversion denial to resolve through the live settlement binding seam as a faction mismatch");
            helper.assertTrue(unclaimedWorker == null, "Expected unclaimed settlement spawning to be denied before any worker entity is created");
            helper.assertTrue(unclaimedBinding.status() == BannerModSettlementBinding.Status.UNCLAIMED,
                    "Expected the live settlement binding seam to report UNCLAIMED for a claimless settlement spawn attempt");
        } finally {
            clearPhase30TestOverrides();
        }

        helper.succeed();
    }

    // Forge's ConfigValue caches the first .get() result and never invalidates that cache when
    // .set() is called afterwards (see WorkersServerConfig#TEST_OVERRIDES). Use the override
    // seam so each test's expected Phase-30 config applies regardless of the order or any
    // sibling test's prior .set()/.get() pattern.
    private static void configurePhase30ForTests() {
        WorkersServerConfig.setTestOverride(WorkersServerConfig.WorkerBirthEnabled, true);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.ClaimBasedSettlementSpawnEnabled, true);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnMinimumVillagers, 1);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnWorkerCap, 8);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnCooldownDays, 1);
    }

    private static void clearPhase30TestOverrides() {
        WorkersServerConfig.setTestOverride(WorkersServerConfig.WorkerBirthEnabled, null);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.ClaimBasedSettlementSpawnEnabled, null);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnMinimumVillagers, null);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnWorkerCap, null);
        WorkersServerConfig.setTestOverride(WorkersServerConfig.SettlementSpawnCooldownDays, null);
    }
}
