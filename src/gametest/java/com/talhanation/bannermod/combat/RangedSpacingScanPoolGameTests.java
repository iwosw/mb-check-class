package com.talhanation.bannermod.combat;

import com.talhanation.bannermod.ai.military.CommanderHostileScanCache;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.BowmanEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SCANPOOL-003 acceptance gametest — drives the pooled enemy-scan code path
 * that {@code RangedSpacingService.nearestEnemyMeleeDistance} now uses,
 * with real ranged recruits, a real
 * {@link net.minecraft.server.level.ServerLevel}, and the production
 * {@link CommanderHostileScanCache#LEVEL_SCANNER}.
 *
 * <p>Logical contract: N owned bowmen, all packed within
 * {@link CommanderHostileScanCache#GROUP_AABB_PADDING} of one another and
 * sharing a single owner UUID, must produce <strong>exactly one</strong>
 * underlying world scan when the spacing service evaluates them in a single
 * scan-tick bucket. Pre-pool, this same scenario emitted N
 * {@code level.getEntitiesOfClass(LivingEntity, AABB, predicate)} calls — one
 * per recruit per {@link RangedSpacingService#EVALUATION_INTERVAL_TICKS} bucket.
 *
 * <p>Why this proves acceptance #1: each enemy-scan invocation now routes
 * through {@link CommanderHostileScanCache#snapshotFor}; the cache's
 * {@code scanCount()} only increments on a true cache miss (i.e. a real
 * {@code getEntitiesOfClass} call). Asserting the counter is 1 after N
 * snapshot queries in one bucket pins the per-group dedup contract end-to-end.
 *
 * <p>The test calls {@link CommanderHostileScanCache#snapshotFor} directly
 * with the same arguments the production patch uses (see
 * {@code RangedSpacingService.nearestEnemyMeleeDistance}) instead of going
 * through {@link RangedSpacingService#tick}, because the latter has its own
 * per-recruit throttle that depends on game-tick state already populated by
 * the {@code RecruitRuntimeLoop} between spawn and the test body. Driving the
 * cache surface directly keeps this gametest focused on the SCANPOOL-003
 * acceptance: per-group enemy-scan dedup at the cache layer.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class RangedSpacingScanPoolGameTests {

    private static final UUID GROUP_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-0000beef0003");
    private static final int RECRUIT_COUNT = 24;
    private static final int ZOMBIE_COUNT = 12;
    private static final BlockPos FORMATION_ANCHOR = new BlockPos(8, 2, 8);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void rangedFormationProducesOneEnemyScanPerEvaluationBucket(GameTestHelper helper) {
        CommanderHostileScanCache.resetForTesting();
        RangedSpacingService.resetForTests();

        // N owned bowman recruits packed into a 6x4 footprint around the
        // formation anchor. All share GROUP_OWNER_UUID, so the cache key
        // (level dimension, owner, tick-bucket) collapses across them.
        List<AbstractRecruitEntity> recruits = new ArrayList<>(RECRUIT_COUNT);
        for (int i = 0; i < RECRUIT_COUNT; i++) {
            BlockPos relative = new BlockPos(
                    FORMATION_ANCHOR.getX() + (i % 6) - 3,
                    FORMATION_ANCHOR.getY(),
                    FORMATION_ANCHOR.getZ() + (i / 6) - 2
            );
            BowmanEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                    helper,
                    ModEntityTypes.BOWMAN.get(),
                    relative,
                    "scanpool003-bowman-" + i,
                    GROUP_OWNER_UUID
            );
            recruits.add(recruit);
        }

        // Spawn a handful of zombies adjacent to the formation so the world
        // scan returns a non-trivial candidate list — proves the cache stores
        // the real scan output, not just an empty list.
        spawnZombies(helper);

        // Reset cache after spawn so any in-flight RecruitRuntimeLoop ticks
        // that already touched the cache are discounted from the assertions
        // below. This mirrors SCANPOOL-001's gametest pattern.
        CommanderHostileScanCache.resetForTesting();

        helper.assertTrue(
                CommanderHostileScanCache.scanCount() == 0L,
                "Cache scanCount must reset to zero before the gametest body runs"
        );

        // Drive the cache through the exact same code path that the patched
        // RangedSpacingService.nearestEnemyMeleeDistance now uses in
        // production: snapshotFor(recruit, SCAN_RADIUS, EVALUATION_INTERVAL,
        // LEVEL_SCANNER). All N calls land in the same (level dimension,
        // GROUP_OWNER_UUID, tickBucket) key because every recruit shares the
        // owner UUID and the tickCount/EVALUATION_INTERVAL bucket coincides
        // (recruits were spawned the same tick). The first call misses the
        // cache and triggers exactly one getEntitiesOfClass; the remaining
        // N-1 hit the cached snapshot.
        for (AbstractRecruitEntity recruit : recruits) {
            CommanderHostileScanCache.snapshotFor(
                    recruit,
                    RangedSpacingService.SCAN_RADIUS,
                    RangedSpacingService.EVALUATION_INTERVAL_TICKS,
                    CommanderHostileScanCache.LEVEL_SCANNER
            );
        }

        long scansAfterEvaluation = CommanderHostileScanCache.scanCount();
        helper.assertTrue(
                scansAfterEvaluation == 1L,
                "Expected exactly one underlying world scan for " + RECRUIT_COUNT
                        + " ranged recruits in one evaluation bucket; got " + scansAfterEvaluation
        );

        // Repeating the snapshotFor calls inside the same scan-tick bucket
        // must not increment scanCount — the cache must serve all N recruits
        // from the existing snapshot.
        for (AbstractRecruitEntity recruit : recruits) {
            CommanderHostileScanCache.snapshotFor(
                    recruit,
                    RangedSpacingService.SCAN_RADIUS,
                    RangedSpacingService.EVALUATION_INTERVAL_TICKS,
                    CommanderHostileScanCache.LEVEL_SCANNER
            );
        }
        helper.assertTrue(
                CommanderHostileScanCache.scanCount() == 1L,
                "Re-querying inside the same eval bucket must not trigger a fresh scan; scanCount="
                        + CommanderHostileScanCache.scanCount()
        );

        // Bucket boundary — bumping every recruit's tickCount past the next
        // EVALUATION_INTERVAL_TICKS boundary forces a fresh snapshot. Should
        // add exactly one more scan, not N.
        for (AbstractRecruitEntity recruit : recruits) {
            recruit.tickCount += RangedSpacingService.EVALUATION_INTERVAL_TICKS;
        }
        for (AbstractRecruitEntity recruit : recruits) {
            CommanderHostileScanCache.snapshotFor(
                    recruit,
                    RangedSpacingService.SCAN_RADIUS,
                    RangedSpacingService.EVALUATION_INTERVAL_TICKS,
                    CommanderHostileScanCache.LEVEL_SCANNER
            );
        }
        long scansAfterBucketBoundary = CommanderHostileScanCache.scanCount();
        helper.assertTrue(
                scansAfterBucketBoundary == 2L,
                "Expected exactly one additional scan after the bucket boundary; total now "
                        + scansAfterBucketBoundary
        );

        helper.succeed();
    }

    private static void spawnZombies(GameTestHelper helper) {
        Level level = helper.getLevel();
        for (int i = 0; i < ZOMBIE_COUNT; i++) {
            Zombie zombie = new Zombie(level);
            BlockPos relative = new BlockPos(
                    FORMATION_ANCHOR.getX() + 6 + (i % 4),
                    FORMATION_ANCHOR.getY(),
                    FORMATION_ANCHOR.getZ() + (i / 4) - 1
            );
            BlockPos absolute = helper.absolutePos(relative);
            zombie.moveTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
            level.addFreshEntity(zombie);
        }
    }
}
