package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.events.RecruitLifecycleEvents;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.gametest.support.RecruitsCommandGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.util.FormationDimensionGuard;
import com.talhanation.bannermod.util.RuntimeProfilingCounters;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FORMATIONDIM-001 acceptance coverage.
 *
 * <p>Two scenarios are exercised:
 * <ol>
 *   <li>Leader spawns in Overworld with a 4-recruit formation, then "transitions"
 *       to the Nether (we simulate by creating the leader fake-player in the
 *       Nether level — {@code recruit.getOwner()} resolves only against the
 *       recruit's own level, so the recruits see a null leader exactly as they
 *       would after a portal traversal). The recruits must hold their formation
 *       positions rather than path toward stale anchors.</li>
 *   <li>Firing {@link PlayerEvent.PlayerChangedDimensionEvent} drives
 *       {@code RecruitLifecycleEvents.onPlayerChangedDimension} and the
 *       {@code formation.cross_dimension_orphan} counter must advance by exactly
 *       the cohort size left behind in the source dimension.</li>
 * </ol>
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModFormationDimensionGuardGameTests {

    private static final UUID FORMATION_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000901");
    private static final UUID ORPHAN_COUNTER_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000902");
    private static final UUID FORMATION_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000903");
    private static final UUID ORPHAN_GROUP_UUID = UUID.fromString("00000000-0000-0000-0000-000000000904");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitsHoldPositionWhenLeaderCrossesDimension(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null,
                "Expected Nether level to exist for cross-dimension formation guard test");

        // Spawn leader in Overworld next to the squad, then 4 recruits in formation cohort.
        BlockPos leaderPos = helper.absolutePos(new BlockPos(3, 2, 5));
        ServerPlayer overworldLeader = (ServerPlayer) BannerModDedicatedServerGameTestSupport
                .createPositionedFakeServerPlayer(overworld, FORMATION_OWNER_UUID, "formation-leader-ow", leaderPos);

        List<AbstractRecruitEntity> formation = spawnFormationCohort(helper, FORMATION_OWNER_UUID, FORMATION_GROUP_UUID);
        helper.assertTrue(formation.size() == 4,
                "Expected the cross-dimension hold scenario to spawn a 4-recruit formation cohort");

        // Capture starting positions before the leader leaves the dimension.
        List<Vec3> startingPositions = new ArrayList<>();
        for (AbstractRecruitEntity recruit : formation) {
            startingPositions.add(recruit.position());
            // Lock in the hold position to current location so any unguarded
            // formation goal would try to navigate (and we'd see it move).
            recruit.setHoldPos(recruit.position());
        }

        long counterBefore = orphanCounter();

        // Simulate the leader walking through a portal: remove the overworld
        // fake-player and create one with the same UUID in the nether.
        // recruit.getOwner() is keyed against the recruit's own level so the
        // overworld lookup will now return null — which is exactly what
        // happens after a portal traversal.
        overworldLeader.discard();
        BlockPos netherLeaderPos = new BlockPos(0, 64, 0);
        Player netherLeader = BannerModDedicatedServerGameTestSupport
                .createPositionedFakeServerPlayer(nether, FORMATION_OWNER_UUID, "formation-leader-nether", netherLeaderPos);
        helper.assertTrue(netherLeader.level() == nether,
                "Expected the nether-side fake leader to live in the Nether level");

        // Verify recruits no longer see the leader through their level's player lookup.
        for (AbstractRecruitEntity recruit : formation) {
            helper.assertTrue(recruit.getOwner() == null,
                    "Expected overworld recruit to lose owner reference once leader is in Nether");
        }

        // Drive several follow-goal canUse() iterations through FormationDimensionGuard
        // to model the per-tick guard. Each call must report hold-required.
        for (AbstractRecruitEntity recruit : formation) {
            for (int i = 0; i < 3; i++) {
                helper.assertTrue(
                        FormationDimensionGuard.shouldHoldDueToDimensionMismatch(recruit, recruit.getOwner()),
                        "Expected dimension guard to demand hold-position for orphaned formation recruit");
            }
        }

        // Verify recruits did not actually move (they have no leader to follow
        // and the goal would short-circuit; the explicit position check guards
        // against any future regression where a goal forgets the dim guard).
        for (int i = 0; i < formation.size(); i++) {
            Vec3 now = formation.get(i).position();
            Vec3 then = startingPositions.get(i);
            helper.assertTrue(now.distanceToSqr(then) < 1.0E-6D,
                    "Expected recruit " + i + " to hold position after leader cross-dimension transition");
        }

        long counterAfter = orphanCounter();
        helper.assertTrue(counterAfter > counterBefore,
                "Expected per-tick dim-guard checks to advance the cross-dimension orphan counter");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void playerChangedDimensionEventBumpsOrphanCounterByGroupSize(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel nether = overworld.getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null,
                "Expected Nether level to exist for orphan-counter event test");

        // Spawn the leader directly in the destination so the event handler's
        // "every other dimension" sweep counts exactly the orphaned cohort.
        BlockPos netherLeaderPos = new BlockPos(0, 64, 0);
        ServerPlayer netherLeader = (ServerPlayer) BannerModDedicatedServerGameTestSupport
                .createPositionedFakeServerPlayer(nether, ORPHAN_COUNTER_OWNER_UUID, "orphan-leader-nether", netherLeaderPos);

        // Spawn 4 recruits owned by the same UUID in the overworld — the cohort
        // that would be orphaned by the leader's portal traversal.
        List<AbstractRecruitEntity> cohort = spawnFormationCohort(helper, ORPHAN_COUNTER_OWNER_UUID, ORPHAN_GROUP_UUID);
        helper.assertTrue(cohort.size() == 4,
                "Expected orphan-counter scenario to register a 4-recruit cohort");

        long before = orphanCounter();

        // Fire the dimension-change event directly so we exercise the handler
        // wired in RecruitLifecycleEvents without depending on portal physics.
        ResourceKey<Level> from = Level.OVERWORLD;
        ResourceKey<Level> to = Level.NETHER;
        PlayerEvent.PlayerChangedDimensionEvent event = new PlayerEvent.PlayerChangedDimensionEvent(netherLeader, from, to);
        new RecruitLifecycleEvents().onPlayerChangedDimension(event);

        long after = orphanCounter();
        long delta = after - before;
        helper.assertTrue(delta == cohort.size(),
                "Expected PlayerChangedDimensionEvent to bump orphan counter by the orphaned cohort size; got delta=" + delta);

        helper.succeed();
    }

    private static List<AbstractRecruitEntity> spawnFormationCohort(GameTestHelper helper, UUID ownerUuid, UUID groupUuid) {
        List<AbstractRecruitEntity> cohort = new ArrayList<>();
        cohort.add(RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Formation Hold Recruit A",
                ownerUuid));
        cohort.add(RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FLANK_POS,
                "Formation Hold Recruit B",
                ownerUuid));
        cohort.add(RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_RANGED_LEFT_POS,
                "Formation Hold Recruit C",
                ownerUuid));
        cohort.add(RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_RANGED_RIGHT_POS,
                "Formation Hold Recruit D",
                ownerUuid));

        for (AbstractRecruitEntity recruit : cohort) {
            RecruitsCommandGameTestSupport.prepareForCommand(recruit, groupUuid);
        }
        RecruitsBattleGameTestSupport.assignFormationCohort(cohort, groupUuid);
        return cohort;
    }

    private static long orphanCounter() {
        Map<String, Long> snapshot = RuntimeProfilingCounters.snapshot();
        Long value = snapshot.get(FormationDimensionGuard.COUNTER_KEY);
        return value == null ? 0L : value;
    }
}
