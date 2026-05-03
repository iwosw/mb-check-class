package com.talhanation.bannermod.society;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentAssignmentState;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentMode;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRecord;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRole;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentRuntimeRoleSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentScheduleSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentScheduleWindowSeed;
import com.talhanation.bannermod.settlement.BannerModSettlementResidentServiceContract;
import com.talhanation.bannermod.settlement.goal.BannerModResidentGoalScheduler;
import com.talhanation.bannermod.settlement.goal.ResidentGoalContext;
import com.talhanation.bannermod.settlement.goal.ResidentTask;
import com.talhanation.bannermod.settlement.goal.impl.HideResidentGoal;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public final class NpcSocietyPhaseThreeGameTests {
    private static final long ACTIVE_TIME = 6000L;

    private NpcSocietyPhaseThreeGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void assaultMemoryPushesVillagerIntoHideWithoutFreshThreat(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID residentId = UUID.fromString("00000000-0000-0000-0000-000000043001");
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000043101");
        NpcSocietyProfile profile = NpcSocietyProfile.createDefault(residentId, ACTIVE_TIME)
                .withNeedState(5, 5, 5, 12, ACTIVE_TIME);

        seedProfile(level, profile);
        NpcMemoryAccess.rememberAssaultByPlayer(level, residentId, playerId, 92, ACTIVE_TIME);
        NpcSocietyProfile updated = NpcMemoryAccess.tickResidentState(level, NpcSocietyAccess.ensureResident(level, residentId, ACTIVE_TIME), ACTIVE_TIME);

        BannerModResidentGoalScheduler scheduler = BannerModResidentGoalScheduler.withDefaultGoals();
        ResidentGoalContext ctx = new ResidentGoalContext(villagerResident(residentId), null, ACTIVE_TIME, updated);
        scheduler.tick(ctx);

        requireTask(helper, scheduler, residentId, HideResidentGoal.ID.toString());
        helper.assertTrue(updated.fearScore() > updated.safetyNeed(), "Expected assault memory to dominate fear over baseline safety.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void familyMemoryPropagatesAcrossHousehold(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID homeUuid = UUID.fromString("00000000-0000-0000-0000-000000043201");
        UUID residentA = UUID.fromString("00000000-0000-0000-0000-000000043002");
        UUID residentB = UUID.fromString("00000000-0000-0000-0000-000000043003");
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000043102");

        seedProfile(level, NpcSocietyProfile.createSeeded(residentA, NpcLifeStage.ADULT, NpcSex.FEMALE, ACTIVE_TIME));
        seedProfile(level, NpcSocietyProfile.createSeeded(residentB, NpcLifeStage.ADULT, NpcSex.MALE, ACTIVE_TIME));
        NpcHouseholdAccess.reconcileResidentHome(level, residentA, homeUuid, 4, ACTIVE_TIME);
        NpcHouseholdAccess.reconcileResidentHome(level, residentB, homeUuid, 4, ACTIVE_TIME);
        NpcFamilyAccess.reconcileFamilyForResident(level, residentA, ACTIVE_TIME);
        NpcFamilyAccess.reconcileFamilyForResident(level, residentB, ACTIVE_TIME);

        NpcMemoryAccess.rememberAssaultByPlayer(level, residentA, playerId, 88, ACTIVE_TIME);

        NpcSocietyProfile spouse = NpcSocietyAccess.profileFor(level, residentB).orElseThrow();
        List<NpcMemorySummarySnapshot> memories = NpcMemoryAccess.summarySnapshots(level, residentB, ACTIVE_TIME);

        helper.assertTrue(spouse.fearScore() > 0, "Expected family member to gain fear from propagated assault memory.");
        helper.assertTrue(!memories.isEmpty(), "Expected propagated household/family memory to be visible in summaries.");
        helper.assertTrue("FAMILY".equals(memories.getFirst().scopeTag()) || "HOUSEHOLD".equals(memories.getFirst().scopeTag()),
                "Expected propagated memory scope to stay family- or household-scoped.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void protectionMemoryRaisesTrustAndGratitude(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID residentId = UUID.fromString("00000000-0000-0000-0000-000000043004");
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000043103");

        seedProfile(level, NpcSocietyProfile.createDefault(residentId, ACTIVE_TIME));
        NpcMemoryAccess.rememberProtectionByPlayer(level, residentId, playerId, 80, ACTIVE_TIME);
        NpcSocietyProfile updated = NpcSocietyAccess.profileFor(level, residentId).orElseThrow();

        helper.assertTrue(updated.trustScore() > 50, "Expected protection memory to raise trust above neutral baseline.");
        helper.assertTrue(updated.gratitudeScore() > 0, "Expected protection memory to create gratitude.");
        helper.assertTrue(updated.loyaltyScore() > 50, "Expected protection memory to improve loyalty.");
        helper.succeed();
    }

    private static ResidentTask requireTask(GameTestHelper helper,
                                            BannerModResidentGoalScheduler scheduler,
                                            UUID residentId,
                                            String expectedGoalId) {
        Optional<ResidentTask> task = scheduler.currentTask(residentId);
        helper.assertTrue(task.isPresent(), "Expected resident scheduler to publish a task.");
        helper.assertTrue(expectedGoalId.equals(task.get().goalId().toString()),
                "Expected task " + expectedGoalId + " but got " + task.get().goalId() + ".");
        return task.get();
    }

    private static void seedProfile(ServerLevel level, NpcSocietyProfile profile) {
        NpcSocietySavedData.get(level).runtime().seedResident(profile);
    }

    private static BannerModSettlementResidentRecord villagerResident(UUID residentId) {
        return new BannerModSettlementResidentRecord(
                residentId,
                BannerModSettlementResidentRole.VILLAGER,
                BannerModSettlementResidentScheduleSeed.SETTLEMENT_IDLE,
                BannerModSettlementResidentScheduleWindowSeed.DAYLIGHT_FLEX,
                BannerModSettlementResidentRuntimeRoleSeed.VILLAGE_LIFE,
                BannerModSettlementResidentServiceContract.notServiceActor(),
                BannerModSettlementResidentMode.SETTLEMENT_RESIDENT,
                null,
                null,
                null,
                BannerModSettlementResidentAssignmentState.NOT_APPLICABLE
        );
    }
}
