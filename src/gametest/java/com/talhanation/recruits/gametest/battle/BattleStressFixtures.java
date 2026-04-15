package com.talhanation.recruits.gametest.battle;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.BowmanEntity;
import com.talhanation.recruits.entities.CrossBowmanEntity;
import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.recruits.entities.RecruitShieldmanEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BattleStressFixtures {
    public static final int PROFILING_WARMUP_TICKS = 40;
    public static final int BASELINE_DENSE_TIMEOUT_TICKS = 520;
    public static final int BASELINE_DENSE_RESOLVE_TICKS = 440;
    public static final int HEAVY_DENSE_TIMEOUT_TICKS = 580;
    public static final int HEAVY_DENSE_RESOLVE_TICKS = 450;
    public static final int FLOW_FIELD_BENCHMARK_TIMEOUT_TICKS = 260;
    public static final int FLOW_FIELD_BENCHMARK_CAPTURE_TICKS = 80;

    public static final StressScenario BASELINE_DENSE = new StressScenario(
            "baseline_dense_battle",
            BASELINE_DENSE_TIMEOUT_TICKS,
            BASELINE_DENSE_RESOLVE_TICKS,
            90,
            WinningSide.WEST,
            2,
            new ArenaBounds(new BlockPos(-24, 1, -24), new BlockPos(48, 6, 48)),
            List.of(
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 2), "West Shield 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(2, 2, 4), "West Recruit 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 8), "West Shield 2"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(4, 2, 6), "West Recruit 2"),
                    new RecruitSpec(ModEntityTypes.BOWMAN.get(), new BlockPos(5, 2, 3), "West Bowman"),
                    new RecruitSpec(ModEntityTypes.CROSSBOWMAN.get(), new BlockPos(5, 2, 7), "West Crossbow")
            ),
            List.of(
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(12, 2, 2), "East Shield 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(12, 2, 4), "East Recruit 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(12, 2, 8), "East Shield 2"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(10, 2, 6), "East Recruit 2"),
                    new RecruitSpec(ModEntityTypes.BOWMAN.get(), new BlockPos(9, 2, 3), "East Bowman"),
                    new RecruitSpec(ModEntityTypes.CROSSBOWMAN.get(), new BlockPos(9, 2, 7), "East Crossbow")
            ),
            0.0F,
            1.0F
    );

    public static final StressScenario HEAVY_DENSE = new StressScenario(
            "heavy_dense_battle",
            HEAVY_DENSE_TIMEOUT_TICKS,
            HEAVY_DENSE_RESOLVE_TICKS,
            110,
            WinningSide.WEST,
            3,
            new ArenaBounds(new BlockPos(-24, 1, -24), new BlockPos(48, 6, 48)),
            List.of(
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 2), "Heavy West Shield 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(2, 2, 4), "Heavy West Recruit 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 6), "Heavy West Shield 2"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(2, 2, 8), "Heavy West Recruit 2"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(4, 2, 3), "Heavy West Recruit 3"),
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(4, 2, 7), "Heavy West Shield 3"),
                    new RecruitSpec(ModEntityTypes.BOWMAN.get(), new BlockPos(6, 2, 4), "Heavy West Bowman"),
                    new RecruitSpec(ModEntityTypes.CROSSBOWMAN.get(), new BlockPos(6, 2, 6), "Heavy West Crossbow")
            ),
            List.of(
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(12, 2, 4), "Heavy East Recruit 1"),
                    new RecruitSpec(ModEntityTypes.BOWMAN.get(), new BlockPos(8, 2, 4), "Heavy East Bowman"),
                    new RecruitSpec(ModEntityTypes.CROSSBOWMAN.get(), new BlockPos(8, 2, 6), "Heavy East Crossbow")
            ),
            0.0F,
            0.5F
    );

    public static final FlowFieldBenchmarkScenario SAME_DESTINATION_LANE = new FlowFieldBenchmarkScenario(
            "same_destination_flow_field_lane",
            FLOW_FIELD_BENCHMARK_TIMEOUT_TICKS,
            FLOW_FIELD_BENCHMARK_CAPTURE_TICKS,
            new BlockPos(24, 2, 7),
            List.of(
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 2), "Flow Lane Shield 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(2, 2, 4), "Flow Lane Recruit 1"),
                    new RecruitSpec(ModEntityTypes.RECRUIT_SHIELDMAN.get(), new BlockPos(2, 2, 6), "Flow Lane Shield 2"),
                    new RecruitSpec(ModEntityTypes.RECRUIT.get(), new BlockPos(2, 2, 8), "Flow Lane Recruit 2"),
                    new RecruitSpec(ModEntityTypes.BOWMAN.get(), new BlockPos(2, 2, 10), "Flow Lane Bowman"),
                    new RecruitSpec(ModEntityTypes.CROSSBOWMAN.get(), new BlockPos(2, 2, 12), "Flow Lane Crossbow")
            )
    );

    private static final UUID BASELINE_WEST_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000351");
    private static final UUID BASELINE_EAST_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000352");
    private static final UUID HEAVY_WEST_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000353");
    private static final UUID HEAVY_EAST_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000354");
    private static final UUID BASELINE_WEST_GROUP = UUID.fromString("00000000-0000-0000-0000-000000000361");
    private static final UUID BASELINE_EAST_GROUP = UUID.fromString("00000000-0000-0000-0000-000000000362");
    private static final UUID HEAVY_WEST_GROUP = UUID.fromString("00000000-0000-0000-0000-000000000363");
    private static final UUID HEAVY_EAST_GROUP = UUID.fromString("00000000-0000-0000-0000-000000000364");
    private static final UUID FLOW_FIELD_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000365");
    private static final UUID FLOW_FIELD_GROUP = UUID.fromString("00000000-0000-0000-0000-000000000366");

    private BattleStressFixtures() {
    }

    public static ScenarioState spawnBaselineDenseScenario(GameTestHelper helper) {
        return spawnScenario(helper, BASELINE_DENSE, BASELINE_WEST_OWNER, BASELINE_EAST_OWNER, BASELINE_WEST_GROUP, BASELINE_EAST_GROUP);
    }

    public static ScenarioState spawnHeavyDenseScenario(GameTestHelper helper) {
        return spawnScenario(helper, HEAVY_DENSE, HEAVY_WEST_OWNER, HEAVY_EAST_OWNER, HEAVY_WEST_GROUP, HEAVY_EAST_GROUP);
    }

    public static FlowFieldBenchmarkState spawnFlowFieldBenchmarkScenario(GameTestHelper helper) {
        List<AbstractRecruitEntity> recruits = spawnSide(helper, SAME_DESTINATION_LANE.recruits(), FLOW_FIELD_OWNER);
        RecruitsBattleGameTestSupport.assignFormationCohort(recruits, FLOW_FIELD_GROUP);
        RecruitsBattleGameTestSupport.prepareSameDestinationMovement(recruits, helper.absolutePos(SAME_DESTINATION_LANE.targetPos()));
        return new FlowFieldBenchmarkState(SAME_DESTINATION_LANE, recruits);
    }

    public static ScenarioState spawnScenario(GameTestHelper helper, StressScenario scenario, UUID westOwnerId, UUID eastOwnerId, UUID westGroupId, UUID eastGroupId) {
        List<AbstractRecruitEntity> westRecruits = spawnSide(helper, scenario.westRecruits(), westOwnerId);
        List<AbstractRecruitEntity> eastRecruits = spawnSide(helper, scenario.eastRecruits(), eastOwnerId);

        RecruitsBattleGameTestSupport.assignFormationCohort(westRecruits, westGroupId);
        applyHealthCap(westRecruits, scenario.westHealthCap());
        applyHealthCap(eastRecruits, scenario.eastHealthCap());
        releaseForBattle(westRecruits);
        releaseForBattle(eastRecruits);

        RecruitsBattleGameTestSupport.setMutualTargets(westRecruits, eastRecruits);
        RecruitsBattleGameTestSupport.setMutualTargets(eastRecruits, westRecruits);
        return new ScenarioState(scenario, westRecruits, eastRecruits);
    }

    private static List<AbstractRecruitEntity> spawnSide(GameTestHelper helper, List<RecruitSpec> recruits, UUID ownerId) {
        List<AbstractRecruitEntity> spawned = new ArrayList<>();
        for (RecruitSpec recruitSpec : recruits) {
            spawned.add(RecruitsBattleGameTestSupport.spawnConfiguredRecruit(helper, recruitSpec.entityType(), recruitSpec.relativePos(), recruitSpec.customName(), ownerId));
        }
        return spawned;
    }

    private static void applyHealthCap(List<AbstractRecruitEntity> recruits, float maxHealth) {
        if (maxHealth <= 0.0F) {
            return;
        }

        for (AbstractRecruitEntity recruit : recruits) {
            recruit.setHealth(Math.min(recruit.getHealth(), maxHealth));
        }
    }

    private static void releaseForBattle(List<AbstractRecruitEntity> recruits) {
        for (AbstractRecruitEntity recruit : recruits) {
            recruit.setFollowState(3);
            recruit.setAggroState(1);
        }
    }

    public record StressScenario(
            String scenarioId,
            int timeoutTicks,
            int resolveDeadlineTicks,
            int progressProbeTicks,
            WinningSide expectedWinner,
            int minimumCombinedLossesAtProgressProbe,
            ArenaBounds arenaBounds,
            List<RecruitSpec> westRecruits,
            List<RecruitSpec> eastRecruits,
            float westHealthCap,
            float eastHealthCap
    ) {
    }

    public record ScenarioState(StressScenario scenario, List<AbstractRecruitEntity> westRecruits, List<AbstractRecruitEntity> eastRecruits) {
    }

    public record RecruitSpec(EntityType<? extends AbstractRecruitEntity> entityType, BlockPos relativePos, String customName) {
    }

    public record FlowFieldBenchmarkScenario(
            String scenarioId,
            int timeoutTicks,
            int captureTicks,
            BlockPos targetPos,
            List<RecruitSpec> recruits
    ) {
    }

    public record FlowFieldBenchmarkState(FlowFieldBenchmarkScenario scenario, List<AbstractRecruitEntity> recruits) {
    }

    public record ArenaBounds(BlockPos min, BlockPos max) {
        public boolean contains(GameTestHelper helper, BlockPos pos) {
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            int relativeX = pos.getX() - origin.getX();
            int relativeY = pos.getY() - origin.getY();
            int relativeZ = pos.getZ() - origin.getZ();
            return relativeX >= min.getX() && relativeX <= max.getX()
                    && relativeZ >= min.getZ() && relativeZ <= max.getZ();
        }
    }

    public enum WinningSide {
        WEST,
        EAST
    }
}
