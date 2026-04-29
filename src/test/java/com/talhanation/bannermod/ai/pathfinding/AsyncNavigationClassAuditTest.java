package com.talhanation.bannermod.ai.pathfinding;

import com.talhanation.bannermod.ai.military.navigation.RecruitPathNavigation;
import com.talhanation.bannermod.ai.military.navigation.RecruitsHorsePathNavigation;
import com.talhanation.bannermod.ai.military.navigation.SailorPathNavigation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PERF-001 lock. Locks in the navigation-class selection for every custom-mob navigation
 * referenced by the BannerMod entity hierarchy, so a future "this mob uses sync vanilla
 * navigation" regression breaks at unit-test time instead of at MP scale.
 *
 * <p>Audit findings as of 2026-04-27:
 *
 * <ul>
 *   <li>{@code AsyncPathfinderMob} (root of the BannerMod entity hierarchy) defaults
 *       {@code createNavigation} to {@link AsyncGroundPathNavigation}, which extends the
 *       async {@link AsyncPathNavigation} base. Every {@code AbstractInventoryEntity}
 *       subclass — {@code AbstractCitizenEntity}, {@code AbstractRecruitEntity}, and through
 *       it every recruit subtype (RecruitEntity, RecruitShieldmanEntity, BowmanEntity,
 *       CrossBowmanEntity, NomadEntity, ScoutEntity, MessengerEntity, CaptainEntity,
 *       CommanderEntity, HorsemanEntity, AssassinEntity, AssassinLeaderEntity,
 *       VillagerNobleEntity, AbstractStrategicFireRecruitEntity), plus
 *       {@code AbstractWorkerEntity} and its seven worker subtypes (FarmerEntity,
 *       FishermanEntity, MinerEntity, BuilderEntity, LumberjackEntity, AnimalFarmerEntity,
 *       MerchantEntity), and {@code CitizenEntity} — sits inside this async hierarchy.</li>
 *   <li>{@code AbstractRecruitEntity.createNavigation} overrides the default with
 *       {@link RecruitPathNavigation}, which itself extends {@link AsyncGroundPathNavigation}
 *       and toggles between {@code AsyncPathfinder} (when
 *       {@code RecruitsServerConfig.UseAsyncPathfinding} is on) and a sync fallback
 *       {@code PathFinder} for the actual node solve. Either way, the navigation object is
 *       async-driven through {@link AsyncPathNavigation}.</li>
 *   <li>The Forge-mixin'd {@code MobMixin#createNavigation} swaps an {@code AbstractHorse}'s
 *       navigation for {@link RecruitsHorsePathNavigation} when the horse is being ridden by
 *       a recruit; {@code RecruitsHorsePathNavigation} also extends
 *       {@link AsyncGroundPathNavigation}.</li>
 *   <li>{@code CaptainEntity#getNavigation} swaps in {@link SailorPathNavigation} when the
 *       captain is riding a {@code Boat}; {@code SailorPathNavigation} extends
 *       {@link AsyncWaterBoundPathNavigation}, which is itself an
 *       {@link AsyncPathNavigation} subtype.</li>
 *   <li>No BannerMod worker navigation override remains in production; the worker hierarchy
 *       goes through {@code RecruitPathNavigation} via {@code AbstractChunkLoaderEntity →
 *       BowmanEntity → … → AbstractRecruitEntity}.</li>
 *   <li>{@code FishingBobberEntity} (Projectile) and {@code AbstractWorkAreaEntity} (Entity)
 *       have no {@code PathNavigation} at all — they don't navigate.</li>
 * </ul>
 *
 * <p>Net result: every BannerMod custom-mob navigation goes through {@link AsyncPathNavigation}
 * and the async path-finding pipeline. There is no remaining "sync vanilla navigation in a
 * custom mob" gap to close.
 */
class AsyncNavigationClassAuditTest {

    @Test
    void recruitPathNavigationIsAsync() {
        assertTrue(AsyncPathNavigation.class.isAssignableFrom(RecruitPathNavigation.class),
                "RecruitPathNavigation must extend AsyncPathNavigation so MP-scale recruit fights stay non-blocking.");
    }

    @Test
    void recruitsHorsePathNavigationIsAsync() {
        assertTrue(AsyncPathNavigation.class.isAssignableFrom(RecruitsHorsePathNavigation.class),
                "RecruitsHorsePathNavigation must extend AsyncPathNavigation so mounted recruits stay non-blocking.");
    }

    @Test
    void sailorPathNavigationIsAsync() {
        assertTrue(AsyncPathNavigation.class.isAssignableFrom(SailorPathNavigation.class),
                "SailorPathNavigation must extend AsyncPathNavigation so captains in boats stay non-blocking.");
    }

    @Test
    void asyncGroundPathNavigationIsAsync() {
        assertTrue(AsyncPathNavigation.class.isAssignableFrom(AsyncGroundPathNavigation.class),
                "AsyncGroundPathNavigation is the default for every AsyncPathfinderMob; must stay async.");
    }

    @Test
    void asyncWaterBoundPathNavigationIsAsync() {
        assertTrue(AsyncPathNavigation.class.isAssignableFrom(AsyncWaterBoundPathNavigation.class),
                "AsyncWaterBoundPathNavigation is the base of SailorPathNavigation; must stay async.");
    }

}
