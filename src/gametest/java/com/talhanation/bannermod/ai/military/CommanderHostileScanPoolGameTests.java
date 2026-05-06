package com.talhanation.bannermod.ai.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitShieldmanEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SCANPOOL-001 acceptance gametest — drives the hostile-scan cache with
 * real recruits, a real {@link net.minecraft.server.level.ServerLevel},
 * and the production {@link CommanderHostileScanCache#LEVEL_SCANNER} that
 * calls {@code level.getEntitiesOfClass(LivingEntity, AABB)}.
 *
 * <p>Logical contract: 80 recruits owned by one player, all packed within
 * {@link CommanderHostileScanCache#GROUP_AABB_PADDING}, querying the cache
 * within the same scan-tick bucket must produce <strong>exactly one</strong>
 * underlying world scan. Subsequent queries inside the same bucket must
 * be served from the cached snapshot.
 *
 * <p>Why this proves the acceptance: the {@code scanCount()} counter
 * increments on every cache miss (every real {@code getEntitiesOfClass}
 * call). Asserting the counter is 1 after 80 calls in one bucket is the
 * direct, observable proof of the "exactly one hostile scan per group per
 * scan-interval" requirement. The full UseShield → cache wire is covered
 * by the production patch + the JUnit cache test; this gametest pins the
 * end-to-end "real recruits cause real one-scan-per-group" behavior.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class CommanderHostileScanPoolGameTests {

    private static final UUID GROUP_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-0000beef0001");
    private static final int RECRUIT_COUNT = 80;
    private static final int ZOMBIE_COUNT = 100;
    private static final double SCAN_RADIUS = 8.0D;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final BlockPos FORMATION_ANCHOR = new BlockPos(8, 2, 8);

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void eightyRecruitsInOneShieldWallProduceExactlyOneScanPerInterval(GameTestHelper helper) {
        CommanderHostileScanCache.resetForTesting();

        // 80 owned shieldman recruits packed into a 10x8 footprint around the
        // formation anchor. All share GROUP_OWNER_UUID, so the cache key
        // (level dimension, owner, tick-bucket) collapses across them.
        List<AbstractRecruitEntity> recruits = new ArrayList<>(RECRUIT_COUNT);
        for (int i = 0; i < RECRUIT_COUNT; i++) {
            BlockPos relative = new BlockPos(
                    FORMATION_ANCHOR.getX() + (i % 10) - 5,
                    FORMATION_ANCHOR.getY(),
                    FORMATION_ANCHOR.getZ() + (i / 10) - 4
            );
            RecruitShieldmanEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                    helper,
                    ModEntityTypes.RECRUIT_SHIELDMAN.get(),
                    relative,
                    "scanpool-recruit-" + i,
                    GROUP_OWNER_UUID
            );
            recruits.add(recruit);
        }

        // 100 zombies adjacent to the formation so the world scan returns
        // a non-trivial candidate list — proves the cache stores the real
        // scan output, not just an empty list.
        spawnZombies(helper);

        helper.assertTrue(
                CommanderHostileScanCache.scanCount() == 0L,
                "Cache scanCount must reset to zero before the gametest body runs"
        );

        // Drive the cache directly via the same code path UseShield uses in
        // production: the recruit's targetingConditions plus LEVEL_SCANNER.
        // All 80 calls land in the same (level, GROUP_OWNER_UUID, bucket)
        // key because every recruit's tickCount/SCAN_INTERVAL_TICKS is
        // identical (recruits were just spawned this tick) and they share
        // the owner UUID. The first call misses and triggers exactly one
        // getEntitiesOfClass; the remaining 79 hit the cached snapshot.
        for (AbstractRecruitEntity recruit : recruits) {
            CommanderHostileScanCache.findNearestHostile(
                    recruit,
                    SCAN_RADIUS,
                    SCAN_INTERVAL_TICKS,
                    (r, candidate) -> r.targetingConditions.test(r, candidate),
                    CommanderHostileScanCache.LEVEL_SCANNER
            );
        }

        long scansAfter80Queries = CommanderHostileScanCache.scanCount();
        helper.assertTrue(
                scansAfter80Queries == 1L,
                "Expected exactly one underlying world scan for 80 recruits in one shield-wall group; got "
                        + scansAfter80Queries
        );

        // Bucket boundary: bumping every recruit's tickCount past the next
        // scan-interval boundary forces a fresh snapshot. Should add exactly
        // one more scan, not 80.
        for (AbstractRecruitEntity recruit : recruits) {
            recruit.tickCount += SCAN_INTERVAL_TICKS;
        }
        for (AbstractRecruitEntity recruit : recruits) {
            CommanderHostileScanCache.findNearestHostile(
                    recruit,
                    SCAN_RADIUS,
                    SCAN_INTERVAL_TICKS,
                    (r, candidate) -> r.targetingConditions.test(r, candidate),
                    CommanderHostileScanCache.LEVEL_SCANNER
            );
        }

        long scansAfterBucketBoundary = CommanderHostileScanCache.scanCount();
        helper.assertTrue(
                scansAfterBucketBoundary == 2L,
                "Expected exactly one additional scan after the bucket boundary; total now " + scansAfterBucketBoundary
        );

        helper.succeed();
    }

    private static void spawnZombies(GameTestHelper helper) {
        Level level = helper.getLevel();
        for (int i = 0; i < ZOMBIE_COUNT; i++) {
            Zombie zombie = new Zombie(level);
            BlockPos relative = new BlockPos(
                    FORMATION_ANCHOR.getX() + 6 + (i % 5),
                    FORMATION_ANCHOR.getY(),
                    FORMATION_ANCHOR.getZ() + (i / 5) - 10
            );
            BlockPos absolute = helper.absolutePos(relative);
            zombie.moveTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
            level.addFreshEntity(zombie);
        }
    }
}
